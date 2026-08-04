package io.github.bcfixes.prestige;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;

public final class SchematicLibrary {
    public static final long MAX_BYTES = 256_000L;
    private static final String ENTRY_MAGIC = "BC_PRESTIGE_SCHEMATIC_V1";

    public record Entry(String id, String sha256, String author, String originalName, long size, long generation) {}

    private SchematicLibrary() {}

    public static List<String> ownUploads(MinecraftServer server, String author) throws IOException {
        return ownUploads(serverRoot(server), author);
    }

    static List<String> ownUploads(Path serverRoot, String author) throws IOException {
        PrestigeContracts.validateAuthor(author);
        Path root = uploadedRoot(serverRoot);
        Path authorRoot = root.resolve(author).normalize();
        if (!authorRoot.startsWith(root) || !Files.isDirectory(authorRoot, LinkOption.NOFOLLOW_LINKS)) return List.of();
        try (var paths = Files.list(authorRoot)) {
            return paths.filter(path -> Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS) && !Files.isSymbolicLink(path))
                    .map(path -> path.getFileName().toString())
                    .filter(SchematicLibrary::safeFileName)
                    .sorted().toList();
        }
    }

    public static List<String> allUploads(MinecraftServer server) throws IOException {
        Path root = uploadedRoot(serverRoot(server));
        if (!Files.isDirectory(root, LinkOption.NOFOLLOW_LINKS)) return List.of();
        List<String> result = new ArrayList<>();
        try (var authors = Files.list(root)) {
            for (Path authorPath : authors.filter(path -> Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)
                    && !Files.isSymbolicLink(path)).sorted().toList()) {
                String author = authorPath.getFileName().toString();
                try { PrestigeContracts.validateAuthor(author); }
                catch (IllegalArgumentException ignored) { continue; }
                for (String name : ownUploads(serverRoot(server), author)) result.add(author + "/" + name);
            }
        }
        return List.copyOf(result);
    }

    public static Entry publish(MinecraftServer server, String actingPlayer, boolean operator,
                                String author, String fileName, long generation) throws IOException {
        return publish(serverRoot(server), actingPlayer, operator, author, fileName, generation);
    }

    static Entry publish(Path serverRoot, String actingPlayer, boolean operator,
                         String author, String fileName, long generation) throws IOException {
        PrestigeContracts.validateAuthor(actingPlayer);
        PrestigeContracts.validateAuthor(author);
        if (!operator && !actingPlayer.equals(author)) throw new IllegalArgumentException("players may publish only their own uploads");
        if (!safeFileName(fileName)) throw new IllegalArgumentException("unsafe schematic filename");
        Path uploadRoot = uploadedRoot(serverRoot);
        Path source = uploadRoot.resolve(author).resolve(fileName).normalize();
        if (!source.startsWith(uploadRoot) || !Files.isRegularFile(source, LinkOption.NOFOLLOW_LINKS)
                || Files.isSymbolicLink(source)) throw new IllegalArgumentException("schematic upload is not a regular server file");
        long size = Files.size(source);
        if (size < 2 || size > MAX_BYTES) throw new IllegalArgumentException("schematic is outside the size limit");
        try (InputStream input = Files.newInputStream(source)) {
            if (input.read() != 0x1f || input.read() != 0x8b) throw new IllegalArgumentException("schematic is not gzip encoded");
        }
        CompoundTag rootTag;
        try { rootTag = NbtIo.readCompressed(source.toFile()); }
        catch (Exception error) { throw new IllegalArgumentException("schematic NBT is unreadable", error); }
        if (rootTag == null || !rootTag.contains("size", Tag.TAG_LIST)
                || !rootTag.contains("palette", Tag.TAG_LIST) || !rootTag.contains("blocks", Tag.TAG_LIST)) {
            throw new IllegalArgumentException("schematic is not a StructureTemplate payload");
        }
        String digest = sha256(source);
        String id = digest.substring(0, 16) + "-" + author.toLowerCase(Locale.ROOT);
        Path library = libraryRoot(serverRoot);
        Path objects = library.resolve("objects");
        Path entries = library.resolve("entries");
        Files.createDirectories(objects);
        Files.createDirectories(entries);
        Path object = objects.resolve(digest + ".nbt");
        if (!Files.exists(object)) {
            Path partial = object.resolveSibling(object.getFileName() + ".partial");
            Files.copy(source, partial, StandardCopyOption.COPY_ATTRIBUTES);
            if (!sha256(partial).equals(digest)) {
                Files.deleteIfExists(partial);
                throw new IOException("schematic changed while publishing");
            }
            atomicMove(partial, object);
        }
        Entry entry = new Entry(id, digest, author, fileName, size, generation);
        writeEntry(entries.resolve(id + ".tsv"), entry);
        return entry;
    }

    public static List<Entry> list(MinecraftServer server) throws IOException {
        return list(serverRoot(server));
    }

    static List<Entry> list(Path serverRoot) throws IOException {
        Path entries = libraryRoot(serverRoot).resolve("entries");
        if (!Files.isDirectory(entries, LinkOption.NOFOLLOW_LINKS)) return List.of();
        List<Entry> result = new ArrayList<>();
        try (var paths = Files.list(entries)) {
            for (Path path : paths.sorted(Comparator.comparing(p -> p.getFileName().toString())).toList()) {
                if (Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS) && !Files.isSymbolicLink(path)) result.add(readEntry(path));
            }
        }
        return List.copyOf(result);
    }

    public static byte[] download(MinecraftServer server, String id) throws IOException {
        return download(serverRoot(server), id);
    }

    static byte[] download(Path serverRoot, String id) throws IOException {
        PrestigeContracts.validateId("schematic entry ID", id);
        Path entryPath = libraryRoot(serverRoot).resolve("entries").resolve(id + ".tsv").normalize();
        Entry entry = readEntry(entryPath);
        Path object = libraryRoot(serverRoot).resolve("objects").resolve(entry.sha256() + ".nbt").normalize();
        if (!Files.isRegularFile(object, LinkOption.NOFOLLOW_LINKS) || Files.isSymbolicLink(object)
                || Files.size(object) != entry.size() || !sha256(object).equals(entry.sha256())) {
            throw new IOException("published schematic object failed integrity verification");
        }
        return Files.readAllBytes(object);
    }

    public static void remove(MinecraftServer server, String id) throws IOException {
        remove(serverRoot(server), id);
    }

    static void remove(Path serverRoot, String id) throws IOException {
        PrestigeContracts.validateId("schematic entry ID", id);
        Path entries = libraryRoot(serverRoot).resolve("entries").normalize();
        Path target = entries.resolve(id + ".tsv").normalize();
        if (!target.startsWith(entries)) throw new IllegalArgumentException("entry path escaped catalog");
        Files.deleteIfExists(target);
    }

    private static void writeEntry(Path path, Entry entry) throws IOException {
        String encodedName = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(entry.originalName().getBytes(StandardCharsets.UTF_8));
        String payload = String.join("\n", ENTRY_MAGIC, "id\t" + entry.id(), "sha256\t" + entry.sha256(),
                "author\t" + entry.author(), "name_b64\t" + encodedName, "size\t" + entry.size(),
                "generation\t" + entry.generation()) + "\n";
        Files.createDirectories(path.getParent());
        Path partial = path.resolveSibling(path.getFileName() + ".partial");
        Files.writeString(partial, payload, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
        atomicMove(partial, path);
    }

    private static Entry readEntry(Path path) throws IOException {
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        if (lines.size() != 7 || !lines.get(0).equals(ENTRY_MAGIC)) throw new IllegalArgumentException("invalid schematic entry");
        String id = field(lines.get(1), "id");
        String digest = field(lines.get(2), "sha256");
        String author = field(lines.get(3), "author");
        String encodedName = field(lines.get(4), "name_b64");
        String name = new String(Base64.getUrlDecoder().decode(encodedName), StandardCharsets.UTF_8);
        long size = Long.parseLong(field(lines.get(5), "size"));
        long generation = Long.parseLong(field(lines.get(6), "generation"));
        PrestigeContracts.validateId("schematic entry ID", id);
        PrestigeContracts.validateAuthor(author);
        if (!digest.matches("[0-9a-f]{64}") || !safeFileName(name) || size < 2 || size > MAX_BYTES || generation < 0) {
            throw new IllegalArgumentException("invalid schematic entry fields");
        }
        return new Entry(id, digest, author, name, size, generation);
    }

    private static String field(String line, String key) {
        String prefix = key + "\t";
        if (!line.startsWith(prefix) || line.indexOf('\t', prefix.length()) >= 0) throw new IllegalArgumentException("invalid " + key + " field");
        return line.substring(prefix.length());
    }

    private static boolean safeFileName(String name) {
        return name != null && name.matches("[A-Za-z0-9._ -]{1,120}\\.nbt") && !name.equals(".nbt")
                && !name.startsWith(".") && !name.contains("..") && !name.contains("/") && !name.contains("\\");
    }

    private static Path serverRoot(MinecraftServer server) {
        return server.getServerDirectory().toPath().toAbsolutePath().normalize();
    }

    private static Path uploadedRoot(Path serverRoot) { return serverRoot.resolve("schematics/uploaded").normalize(); }
    private static Path libraryRoot(Path serverRoot) { return serverRoot.resolve(".prestige/schematics").normalize(); }

    private static String sha256(Path path) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream input = Files.newInputStream(path)) {
                byte[] buffer = new byte[65536];
                int count;
                while ((count = input.read(buffer)) >= 0) if (count > 0) digest.update(buffer, 0, count);
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException impossible) { throw new AssertionError(impossible); }
    }

    private static void atomicMove(Path source, Path target) throws IOException {
        try { Files.move(source, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
        catch (java.nio.file.AtomicMoveNotSupportedException error) {
            Files.deleteIfExists(source);
            throw new IOException("schematic filesystem does not support atomic publication", error);
        }
    }
}
