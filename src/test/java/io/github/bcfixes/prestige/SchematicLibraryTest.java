package io.github.bcfixes.prestige;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SchematicLibraryTest {
    @TempDir Path serverRoot;

    @Test
    void publicationIsOptInAttributedDeduplicatedAndRemovable() throws Exception {
        Path upload = serverRoot.resolve("schematics/uploaded/Builder/house.nbt");
        Files.createDirectories(upload.getParent());
        CompoundTag structure = new CompoundTag();
        structure.put("size", new ListTag());
        structure.put("palette", new ListTag());
        structure.put("blocks", new ListTag());
        NbtIo.writeCompressed(structure, upload.toFile());

        assertEquals(java.util.List.of("house.nbt"), SchematicLibrary.ownUploads(serverRoot, "Builder"));
        assertEquals(java.util.List.of(), SchematicLibrary.list(serverRoot));
        assertThrows(IllegalArgumentException.class,
                () -> SchematicLibrary.publish(serverRoot, "Intruder", false, "Builder", "house.nbt", 0));

        var first = SchematicLibrary.publish(serverRoot, "Builder", false, "Builder", "house.nbt", 0);
        var second = SchematicLibrary.publish(serverRoot, "Builder", false, "Builder", "house.nbt", 1);
        assertEquals(first.id(), second.id());
        try (var objects = Files.list(serverRoot.resolve(".prestige/schematics/objects"))) {
            assertEquals(1, objects.count());
        }
        assertArrayEquals(Files.readAllBytes(upload), SchematicLibrary.download(serverRoot, first.id()));
        SchematicLibrary.remove(serverRoot, first.id());
        assertEquals(java.util.List.of(), SchematicLibrary.list(serverRoot));
    }

    @Test
    void publicationRejectsTraversalAndMalformedPayloads() throws Exception {
        Path uploadRoot = serverRoot.resolve("schematics/uploaded/Builder");
        Files.createDirectories(uploadRoot);
        Files.writeString(uploadRoot.resolve("broken.nbt"), "not gzip");
        assertThrows(IllegalArgumentException.class,
                () -> SchematicLibrary.publish(serverRoot, "Builder", false, "Builder", "../broken.nbt", 0));
        assertThrows(IllegalArgumentException.class,
                () -> SchematicLibrary.publish(serverRoot, "Builder", false, "Builder", "broken.nbt", 0));
    }
}
