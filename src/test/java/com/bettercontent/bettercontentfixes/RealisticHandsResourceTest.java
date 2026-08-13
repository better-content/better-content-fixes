package com.bettercontent.bettercontentfixes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

final class RealisticHandsResourceTest {
    private static final Path ROOT = Path.of("src/main/resources/data/better_content_fixes");
    private static final Path QUARANTINE = Path.of("quarantine/realistic-hands-exhaustive-policy");

    @Test
    void runtimePolicyContainsOnlyTheNoTreePunchingGate() throws IOException {
        final Path blockTag = ROOT.resolve("tags/blocks/realistic_hands/axe.json");
        final Path itemTag = ROOT.resolve("tags/items/realistic_hands/tools/axe.json");

        assertEquals(Set.of("#minecraft:logs"), readValues(blockTag));
        assertEquals(Set.of("#forge:tools/axes"), readValues(itemTag));
        assertEquals(Set.of("axe.json"), fileNames(blockTag.getParent()));
        assertEquals(Set.of("axe.json"), fileNames(itemTag.getParent()));
    }

    @Test
    void exhaustivePolicyRemainsQuarantinedOutsideRuntimeResources() throws IOException {
        assertTrue(Files.exists(QUARANTINE.resolve("README.md")));
        assertTrue(fileNames(QUARANTINE.resolve("resources/tags/blocks")).size() >= 9);
        assertTrue(fileNames(QUARANTINE.resolve("resources/tags/items")).size() >= 5);
        assertTrue(fileNames(QUARANTINE.resolve("java")).contains("RealisticHandsKnifeLootModifier.java"));
    }

    private static Set<String> readValues(final Path path) throws IOException {
        final JsonObject root = JsonParser.parseReader(Files.newBufferedReader(path)).getAsJsonObject();
        final JsonArray values = root.getAsJsonArray("values");
        final Set<String> result = new LinkedHashSet<>();
        values.forEach(element -> result.add(element.getAsString()));
        return result;
    }

    private static Set<String> fileNames(final Path directory) throws IOException {
        try (var files = Files.list(directory)) {
            final Set<String> names = new LinkedHashSet<>();
            files.filter(Files::isRegularFile).forEach(path -> names.add(path.getFileName().toString()));
            return names;
        }
    }
}
