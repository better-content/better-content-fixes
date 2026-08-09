package io.github.bcfixes.prestige;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Small, server-authoritative first pass of the persistent prestige tree. */
public final class PrestigePerks {
    private static final String OWNED_MAGIC = "BC_PRESTIGE_PERKS_V1";
    private static final String VOTE_MAGIC = "BC_PRESTIGE_VOTE_V1";
    private static final String STAGED_MAGIC = "BC_PRESTIGE_PERK_STAGE_V1";

    public record Node(String id, String title, String branch, List<String> prerequisites, String effect) {}
    public record Vote(List<String> ranked, String biome, String season, String hour, String mode,
                       String seedProposal, String seedEndorsement) {}
    public record Snapshot(Set<String> owned, List<String> projected, List<String> ranked,
                           String biome, String season, String hour, String mode,
                           String seedProposal, String seedEndorsement, int available) {}

    private static final List<Node> NODES = List.of(
            node("misremembered_country", "Misremembered Country", "Place", List.of(), "Expanded exact-biome voting"),
            node("borrowed_season", "Borrowed Season", "Place", List.of("misremembered_country"), "Starting season vote"),
            node("chosen_hour", "Chosen Hour", "Place", List.of("misremembered_country"), "Starting hour vote"),
            node("remembered_number", "Remembered Number", "Place", List.of("borrowed_season", "chosen_hour"), "Exact seed proposals"),
            node("named_vessel", "Named Vessel", "Vessel", List.of(), "Wayfinder class kit"),
            node("sustaining_vessel", "Sustaining Vessel", "Vessel", List.of("named_vessel"), "Food and water kits"),
            node("ranging_vessel", "Ranging Vessel", "Vessel", List.of("named_vessel"), "Route and animal kits"),
            node("market_vessel", "Market Vessel", "Vessel", List.of("sustaining_vessel", "ranging_vessel"), "Market kit"),
            node("divisible_self", "Divisible Self", "Embark", List.of(), "Point-buy onboarding"),
            node("ancestral_survey", "Ancestral Survey", "Embark", List.of("divisible_self"), "Survey supplies"),
            node("iron_afterimage", "Iron Afterimage", "Embark", List.of("divisible_self"), "Rail supplies"),
            node("broader_cache", "Broader Cache", "Embark", List.of("ancestral_survey", "iron_afterimage"), "Larger embark budget")
    );

    private PrestigePerks() {}

    private static Node node(String id, String title, String branch, List<String> prerequisites, String effect) {
        return new Node(id, title, branch, prerequisites, effect);
    }

    public static List<Node> nodes() { return NODES; }

    public static Path root(MinecraftServer server) { return PrestigeService.state(server); }
    public static Path ownedPath(MinecraftServer server) { return root(server).resolve("perks-v1.tsv"); }
    public static Path stagedPath(MinecraftServer server) { return root(server).resolve("staged-perks-v1.tsv"); }
    private static Path votesPath(MinecraftServer server, long generation, UUID player) {
        return root(server).resolve("votes/generation-" + generation).resolve(player + ".tsv");
    }

    public static Set<String> owned(MinecraftServer server) throws IOException {
        Path path = ownedPath(server);
        if (!Files.isRegularFile(path)) return Set.of();
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        if (lines.size() < 2 || !lines.get(0).equals(OWNED_MAGIC) || !lines.get(1).startsWith("lineage\t")) {
            throw new IllegalArgumentException("invalid prestige perk ledger");
        }
        Set<String> result = new LinkedHashSet<>();
        for (int i = 2; i < lines.size(); i++) {
            String id = lines.get(i);
            if (!id.startsWith("perk\t")) throw new IllegalArgumentException("invalid prestige perk entry");
            id = id.substring(5);
            find(id);
            if (!result.add(id)) throw new IllegalArgumentException("duplicate prestige perk: " + id);
        }
        return Set.copyOf(result);
    }

    public static void writeOwned(MinecraftServer server, Set<String> perks) throws IOException {
        String lineage = PrestigeService.lineage(server).lineageId();
        List<String> lines = new ArrayList<>(List.of(OWNED_MAGIC, "lineage\t" + lineage));
        NODES.stream().map(Node::id).filter(perks::contains).forEach(id -> lines.add("perk\t" + id));
        atomic(ownedPath(server), lines);
    }

    public static Vote vote(MinecraftServer server, ServerPlayer player) throws IOException {
        long generation = PrestigeService.lineage(server).generation();
        Path path = votesPath(server, generation, player.getUUID());
        if (!Files.isRegularFile(path)) return new Vote(List.of(), "", "", "", "", "", "");
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        if ((lines.size() != 8 && lines.size() != 10) || !lines.get(0).equals(VOTE_MAGIC)) throw new IllegalArgumentException("invalid prestige ballot");
        String voteLineage = requirePrefix(lines.get(1), "lineage");
        String voteGeneration = requirePrefix(lines.get(2), "generation");
        PrestigeContracts.Lineage currentLineage = PrestigeService.lineage(server);
        if (!voteLineage.equals(currentLineage.lineageId()) || !voteGeneration.equals(Long.toString(currentLineage.generation()))) {
            throw new IllegalArgumentException("prestige ballot belongs to a different lineage generation");
        }
        List<String> ranked = split(requirePrefix(lines.get(3), "ranked"));
        for (String id : ranked) find(id);
        String proposal = lines.size() == 10 ? requirePrefix(lines.get(8), "seed_proposal") : "";
        String endorsement = lines.size() == 10 ? requirePrefix(lines.get(9), "seed_endorsement") : "";
        return new Vote(ranked, requirePrefix(lines.get(4), "biome"), requirePrefix(lines.get(5), "season"),
                requirePrefix(lines.get(6), "hour"), requirePrefix(lines.get(7), "mode"), proposal, endorsement);
    }

    public static void toggleRank(ServerPlayer player, String id) throws IOException {
        Node node = find(id);
        MinecraftServer server = player.server;
        if (Files.exists(controlPath(server).resolve("staged-request-v2.tsv"))
                || Files.exists(controlPath(server).resolve("reset-request-v2.tsv"))) {
            throw new IllegalStateException("prestige voting is locked");
        }
        PrestigeContracts.Lineage lineage = PrestigeService.lineage(server);
        Set<String> owned = owned(server);
        if (owned.contains(id)) throw new IllegalArgumentException("perk is already owned");
        Vote current = vote(server, player);
        List<String> ranked = new ArrayList<>(current.ranked());
        if (ranked.remove(id)) {
            writeVote(server, player, new Vote(ranked, current.biome(), current.season(), current.hour(), current.mode(),
                    current.seedProposal(), current.seedEndorsement()));
            return;
        }
        int limit = Math.toIntExact(Math.min(lineage.unspentPoints(), NODES.size()));
        if (ranked.size() >= limit) throw new IllegalArgumentException("your ballot is full");
        for (String prerequisite : node.prerequisites()) {
            if (!owned.contains(prerequisite) && !ranked.contains(prerequisite)) {
                if (ranked.size() >= limit) throw new IllegalArgumentException("rank the prerequisite first");
                ranked.add(prerequisite);
            }
        }
        ranked.add(id);
        writeVote(server, player, new Vote(ranked, current.biome(), current.season(), current.hour(), current.mode(),
                current.seedProposal(), current.seedEndorsement()));
    }

    public static void setSetting(ServerPlayer player, String axis, String value) throws IOException {
        if (Files.exists(controlPath(player.server).resolve("staged-request-v2.tsv"))
                || Files.exists(controlPath(player.server).resolve("reset-request-v2.tsv"))) {
            throw new IllegalStateException("prestige voting is locked");
        }
        if (!List.of("biome", "season", "hour", "mode").contains(axis)) throw new IllegalArgumentException("unknown prestige setting");
        if (value.isBlank() || value.contains("\t") || value.contains("\n") || value.contains("\r")) {
            throw new IllegalArgumentException("setting value is malformed");
        }
        if (axis.equals("biome") && !PrestigeService.allowedBiomes(player.server).contains(value)) {
            throw new IllegalArgumentException("biome is not allowlisted");
        }
        if (axis.equals("season") && !List.of("spring", "summer", "autumn", "winter").contains(value)) {
            throw new IllegalArgumentException("season must be spring, summer, autumn, or winter");
        }
        if (axis.equals("hour") && !List.of("dawn", "noon", "dusk", "midnight").contains(value)) {
            throw new IllegalArgumentException("hour must be dawn, noon, dusk, or midnight");
        }
        if (axis.equals("mode") && !List.of("none", "class", "embark").contains(value)) {
            throw new IllegalArgumentException("mode must be none, class, or embark");
        }
        Vote current = vote(player.server, player);
        String biome = axis.equals("biome") ? value : current.biome();
        String season = axis.equals("season") ? value : current.season();
        String hour = axis.equals("hour") ? value : current.hour();
        String mode = axis.equals("mode") ? value : current.mode();
        writeVote(player.server, player, new Vote(current.ranked(), biome, season, hour, mode,
                current.seedProposal(), current.seedEndorsement()));
    }

    public static void proposeSeed(ServerPlayer player, String value) throws IOException {
        if (Files.exists(controlPath(player.server).resolve("staged-request-v2.tsv"))
                || Files.exists(controlPath(player.server).resolve("reset-request-v2.tsv"))) {
            throw new IllegalStateException("prestige voting is locked");
        }
        long seed;
        try { seed = Long.parseLong(value); } catch (NumberFormatException error) { throw new IllegalArgumentException("seed must be a signed 64-bit integer"); }
        Vote current = vote(player.server, player);
        writeVote(player.server, player, new Vote(current.ranked(), current.biome(), current.season(), current.hour(), current.mode(),
                Long.toString(seed), Long.toString(seed)));
    }

    public static Snapshot snapshot(ServerPlayer player) throws IOException {
        MinecraftServer server = player.server;
        PrestigeContracts.Lineage lineage = PrestigeService.lineage(server);
        Set<String> owned = owned(server);
        Vote vote = vote(server, player);
        List<String> projected = projected(server);
        return new Snapshot(owned, projected, vote.ranked(), vote.biome(), vote.season(), vote.hour(), vote.mode(),
                vote.seedProposal(), vote.seedEndorsement(), Math.toIntExact(lineage.unspentPoints()));
    }

    public static List<String> projected(MinecraftServer server) throws IOException {
        Set<String> selected = new LinkedHashSet<>(owned(server));
        Set<String> baselineOwned = Set.copyOf(selected);
        long points = PrestigeService.lineage(server).unspentPoints();
        for (int round = 0; round < points; round++) {
            String winner = null;
            int best = 0;
            for (Node node : NODES) {
                if (selected.contains(node.id()) || !selected.containsAll(node.prerequisites())) continue;
                int score = score(server, node.id());
                if (score > best) { best = score; winner = node.id(); }
            }
            if (winner == null) break;
            selected.add(winner);
        }
        return NODES.stream().map(Node::id).filter(selected::contains).filter(id -> !baselineOwned.contains(id)).toList();
    }

    private static int score(MinecraftServer server, String id) throws IOException {
        long generation = PrestigeService.lineage(server).generation();
        Path directory = root(server).resolve("votes/generation-" + generation);
        if (!Files.isDirectory(directory)) return 0;
        int score = 0;
        try (var paths = Files.list(directory)) {
            for (Path path : paths.filter(Files::isRegularFile).toList()) {
                List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
                if (lines.size() < 4 || !lines.get(0).equals(VOTE_MAGIC)) continue;
                List<String> ranked = split(requirePrefix(lines.get(3), "ranked"));
                int index = ranked.indexOf(id);
                if (index >= 0) score += Math.max(1, (int) Math.min(32, PrestigeService.lineage(server).unspentPoints()) - index);
            }
        }
        return score;
    }

    public static void stage(MinecraftServer server) throws IOException {
        List<String> winners = projected(server);
        PrestigeContracts.Lineage lineage = PrestigeService.lineage(server);
        List<String> lines = new ArrayList<>(List.of(STAGED_MAGIC, "lineage\t" + lineage.lineageId(),
                "generation\t" + lineage.generation(), "base_unspent\t" + lineage.unspentPoints()));
        winners.forEach(id -> lines.add("perk\t" + id));
        atomic(stagedPath(server), lines);
    }

    public static void promoteIfSuccess(MinecraftServer server) throws IOException {
        Path path = stagedPath(server);
        if (!Files.isRegularFile(path)) return;
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        if (lines.size() < 4 || !lines.get(0).equals(STAGED_MAGIC)) return;
        long base = Long.parseLong(requirePrefix(lines.get(2), "generation"));
        if (PrestigeService.lineage(server).generation() <= base) return;
        Set<String> next = new LinkedHashSet<>(owned(server));
        for (int i = 4; i < lines.size(); i++) {
            String id = requirePrefix(lines.get(i), "perk");
            find(id);
            next.add(id);
        }
        writeOwned(server, next);
        Files.deleteIfExists(path);
    }

    public static void cancelStage(MinecraftServer server) throws IOException { Files.deleteIfExists(stagedPath(server)); }

    private static void writeVote(MinecraftServer server, ServerPlayer player, Vote vote) throws IOException {
        long generation = PrestigeService.lineage(server).generation();
        List<String> lines = List.of(VOTE_MAGIC, "lineage\t" + PrestigeService.lineage(server).lineageId(),
                "generation\t" + generation, "ranked\t" + String.join(",", vote.ranked()),
                "biome\t" + safe(vote.biome()), "season\t" + safe(vote.season()),
                "hour\t" + safe(vote.hour()), "mode\t" + safe(vote.mode()),
                "seed_proposal\t" + safe(vote.seedProposal()), "seed_endorsement\t" + safe(vote.seedEndorsement()));
        atomic(votesPath(server, generation, player.getUUID()), lines);
    }

    private static String safe(String value) { return value == null ? "" : value.replace('\t', '_').replace('\n', '_').replace('\r', '_'); }
    private static Path controlPath(MinecraftServer server) { return root(server).resolve("control"); }
    private static List<String> split(String value) { return value == null || value.isBlank() ? List.of() : List.of(value.split(",")); }
    private static String requirePrefix(String line, String key) {
        String prefix = key + "\t";
        if (!line.startsWith(prefix)) throw new IllegalArgumentException("invalid prestige ballot field " + key);
        return line.substring(prefix.length());
    }
    private static Node find(String id) { return NODES.stream().filter(node -> node.id().equals(id)).findFirst().orElseThrow(() -> new IllegalArgumentException("unknown prestige perk: " + id)); }
    private static void atomic(Path path, List<String> lines) throws IOException {
        Files.createDirectories(path.getParent());
        Path partial = path.resolveSibling(path.getFileName() + ".partial");
        Files.deleteIfExists(partial);
        Files.writeString(partial, String.join("\n", lines) + "\n", StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
        Files.move(partial, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
    }
}
