package io.github.bcfixes.prestige;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

public final class PrestigeContracts {
    public static final String LINEAGE_MAGIC = "BC_PRESTIGE_LINEAGE_V1";
    public static final String STAGED_MAGIC = "BC_PRESTIGE_STAGED_V1";
    public static final String RESET_MAGIC = "BC_PRESTIGE_RESET_V1";
    public static final String SUCCESSOR_MAGIC = "BC_PRESTIGE_SUCCESSOR_V1";
    public static final String HEALTH_MAGIC = "BC_PRESTIGE_HEALTH_V1";
    public static final String SHUTDOWN_MAGIC = "BC_PRESTIGE_SHUTDOWN_V1";

    private static final Pattern ID = Pattern.compile("[a-z0-9][a-z0-9_-]{0,63}");
    private static final Pattern WORLD = Pattern.compile("[A-Za-z0-9._-]{1,128}");
    private static final Set<String> IMPACT_PROFILES = Set.of("any");

    private PrestigeContracts() {}

    public record Lineage(String lineageId, long prestigeCount) {}
    public record Staged(String lineageId, long successorSeed, String impactProfile, String worldName) {}
    public record Reset(
            String lineageId,
            String transactionId,
            String worldName,
            long oldSeed,
            long successorSeed,
            String impactProfile
    ) {}
    public record Successor(
            String lineageId,
            String transactionId,
            long successorSeed,
            String impactProfile,
            int attempt
    ) {}

    public static String newTransactionId() {
        return "transaction-" + UUID.randomUUID().toString().replace("-", "");
    }

    public static void validateId(String label, String value) {
        if (value == null || !ID.matcher(value).matches()) {
            throw new IllegalArgumentException(label + " must match " + ID.pattern());
        }
    }

    public static void validateWorldName(String value) {
        if (value == null || !WORLD.matcher(value).matches() || value.equals(".") || value.equals("..")) {
            throw new IllegalArgumentException("world name is outside the supported prototype contract");
        }
    }

    public static void validateImpactProfile(String value) {
        if (!IMPACT_PROFILES.contains(value)) {
            throw new IllegalArgumentException("impact profile is not allowlisted: " + value);
        }
    }

    public static Lineage readLineage(Path path) throws IOException {
        Map<String, String> fields = read(path, LINEAGE_MAGIC, List.of("lineage", "prestige_count"));
        String lineage = fields.get("lineage");
        validateId("lineage ID", lineage);
        long count = parseNonNegativeLong("prestige_count", fields.get("prestige_count"));
        return new Lineage(lineage, count);
    }

    public static Staged readStaged(Path path) throws IOException {
        Map<String, String> fields = read(path, STAGED_MAGIC, List.of("lineage", "successor_seed", "impact", "world"));
        String lineage = fields.get("lineage");
        String impact = fields.get("impact");
        String world = fields.get("world");
        validateId("lineage ID", lineage);
        validateImpactProfile(impact);
        validateWorldName(world);
        return new Staged(lineage, parseLong("successor_seed", fields.get("successor_seed")), impact, world);
    }

    public static Reset readReset(Path path) throws IOException {
        Map<String, String> fields = read(path, RESET_MAGIC,
                List.of("state", "lineage", "transaction", "world", "old_seed", "successor_seed", "impact"));
        if (!fields.get("state").equals("committed")) throw new IllegalArgumentException("reset state is not committed");
        String lineage = fields.get("lineage");
        String transaction = fields.get("transaction");
        String world = fields.get("world");
        String impact = fields.get("impact");
        validateId("lineage ID", lineage);
        validateId("transaction ID", transaction);
        validateWorldName(world);
        validateImpactProfile(impact);
        return new Reset(lineage, transaction, world,
                parseLong("old_seed", fields.get("old_seed")),
                parseLong("successor_seed", fields.get("successor_seed")), impact);
    }

    public static Successor readSuccessor(Path path) throws IOException {
        Map<String, String> fields = read(path, SUCCESSOR_MAGIC,
                List.of("lineage", "transaction", "successor_seed", "impact", "attempt"));
        String lineage = fields.get("lineage");
        String transaction = fields.get("transaction");
        String impact = fields.get("impact");
        validateId("lineage ID", lineage);
        validateId("transaction ID", transaction);
        validateImpactProfile(impact);
        long attempt = parseNonNegativeLong("attempt", fields.get("attempt"));
        if (attempt < 1 || attempt > 3) throw new IllegalArgumentException("attempt must be in 1..3");
        return new Successor(lineage, transaction,
                parseLong("successor_seed", fields.get("successor_seed")), impact, (int) attempt);
    }

    public static String readShutdownTransaction(Path path) throws IOException {
        Map<String, String> fields = read(path, SHUTDOWN_MAGIC, List.of("transaction"));
        String transaction = fields.get("transaction");
        validateId("transaction ID", transaction);
        return transaction;
    }

    public static void writeStaged(Path path, Staged staged) throws IOException {
        validateId("lineage ID", staged.lineageId());
        validateImpactProfile(staged.impactProfile());
        validateWorldName(staged.worldName());
        writeAtomic(path, List.of(
                STAGED_MAGIC,
                "lineage\t" + staged.lineageId(),
                "successor_seed\t" + staged.successorSeed(),
                "impact\t" + staged.impactProfile(),
                "world\t" + staged.worldName()
        ));
    }

    public static void writeReset(Path path, Reset reset) throws IOException {
        validateId("lineage ID", reset.lineageId());
        validateId("transaction ID", reset.transactionId());
        validateWorldName(reset.worldName());
        validateImpactProfile(reset.impactProfile());
        writeAtomic(path, List.of(
                RESET_MAGIC,
                "state\tcommitted",
                "lineage\t" + reset.lineageId(),
                "transaction\t" + reset.transactionId(),
                "world\t" + reset.worldName(),
                "old_seed\t" + reset.oldSeed(),
                "successor_seed\t" + reset.successorSeed(),
                "impact\t" + reset.impactProfile()
        ));
    }

    public static void writeHealth(Path path, Successor successor, long actualSeed, String spawnBiome, String worldName) throws IOException {
        validateWorldName(worldName);
        if (spawnBiome == null || !spawnBiome.matches("[a-z0-9_.-]+:[a-z0-9_./-]+")) {
            throw new IllegalArgumentException("spawn biome is not a resource location");
        }
        String status = actualSeed == successor.successorSeed() ? "healthy" : "seed_mismatch";
        writeAtomic(path, List.of(
                HEALTH_MAGIC,
                "lineage\t" + successor.lineageId(),
                "transaction\t" + successor.transactionId(),
                "successor_seed\t" + successor.successorSeed(),
                "actual_seed\t" + actualSeed,
                "impact\t" + successor.impactProfile(),
                "spawn_biome\t" + spawnBiome,
                "world\t" + worldName,
                "level_dat\ttrue",
                "status\t" + status
        ));
    }

    private static Map<String, String> read(Path path, String magic, List<String> keys) throws IOException {
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        if (lines.size() != keys.size() + 1 || !lines.get(0).equals(magic)) {
            throw new IllegalArgumentException("invalid or unsupported contract: " + path.getFileName());
        }
        java.util.LinkedHashMap<String, String> result = new java.util.LinkedHashMap<>();
        for (int index = 0; index < keys.size(); index++) {
            String line = lines.get(index + 1);
            int tab = line.indexOf('\t');
            if (tab <= 0 || line.indexOf('\t', tab + 1) >= 0 || !line.substring(0, tab).equals(keys.get(index))) {
                throw new IllegalArgumentException("invalid " + keys.get(index) + " contract field");
            }
            String value = line.substring(tab + 1);
            if (value.isBlank() || value.indexOf('\r') >= 0 || value.indexOf('\n') >= 0) {
                throw new IllegalArgumentException("blank or multiline " + keys.get(index) + " contract field");
            }
            result.put(keys.get(index), value);
        }
        return Map.copyOf(result);
    }

    private static long parseLong(String label, String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException error) {
            throw new IllegalArgumentException(label + " is not a signed 64-bit integer", error);
        }
    }

    private static long parseNonNegativeLong(String label, String value) {
        long parsed = parseLong(label, value);
        if (parsed < 0) throw new IllegalArgumentException(label + " is negative");
        return parsed;
    }

    private static void writeAtomic(Path path, List<String> lines) throws IOException {
        Files.createDirectories(path.getParent());
        Path partial = path.resolveSibling(path.getFileName() + ".partial");
        if (Files.exists(partial)) throw new IOException("partial contract already exists: " + partial.getFileName());
        Files.writeString(partial, String.join("\n", new ArrayList<>(lines)) + "\n",
                StandardCharsets.UTF_8, java.nio.file.StandardOpenOption.CREATE_NEW);
        try {
            Files.move(partial, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException error) {
            Files.deleteIfExists(partial);
            throw new IOException("contract filesystem does not support atomic publication", error);
        }
    }
}
