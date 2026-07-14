package io.github.bcfixes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.bcfixes.compat.BurntGrassReplacementDefinitions;
import java.io.IOException;
import java.util.ArrayList;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

final class BurntGrassPaletteResourceTest {
    private static final Path BCFIXES_ROOT = Path.of(".");
    private static final Path WORKSPACE_ROOT = BCFIXES_ROOT.resolve("../../..").normalize();

    @Test
    void replacementTableCoversBurntTagAndMissingUnearthedOvergrownBlocks() throws IOException {
        Set<String> expectedSources = new LinkedHashSet<>(readTagValues(
                WORKSPACE_ROOT.resolve("kubejs/data/burnt/tags/blocks/grass_blocks.json")));
        expectedSources.add("unearthed:overgrown_andesite");
        expectedSources.add("unearthed:overgrown_diorite");
        expectedSources.add("unearthed:overgrown_granite");

        Set<String> actualSources = BurntGrassReplacementDefinitions.entries().stream()
                .map(entry -> entry.sourceId().toString())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        assertEquals(expectedSources, actualSources, "burnt replacement table drifted from pack burnable-grass sources");
    }

    @Test
    void customEntriesHaveGeneratedAssets() {
        for (BurntGrassReplacementDefinitions.Entry entry : BurntGrassReplacementDefinitions.entries()) {
            if (!entry.targetId().getNamespace().equals(BetterContentFixes.MOD_ID)) {
                continue;
            }
            final String path = entry.targetId().getPath();
            assertExists(BCFIXES_ROOT.resolve("src/main/resources/assets/bcfixes/blockstates/" + path + ".json"));
            assertExists(BCFIXES_ROOT.resolve("src/main/resources/assets/bcfixes/models/block/" + path + ".json"));
            assertExists(BCFIXES_ROOT.resolve("src/main/resources/assets/bcfixes/models/item/" + path + ".json"));
            assertExists(BCFIXES_ROOT.resolve("src/main/resources/assets/bcfixes/textures/block/" + path + "_side.png"));
            assertExists(BCFIXES_ROOT.resolve("src/main/resources/assets/bcfixes/textures/block/" + path + "_bottom.png"));
            assertExists(BCFIXES_ROOT.resolve("src/main/resources/data/bcfixes/loot_tables/blocks/" + path + ".json"));
        }
    }

    @Test
    void replacementTargetsAreUniquePerSource() {
        List<BurntGrassReplacementDefinitions.Entry> entries = BurntGrassReplacementDefinitions.entries();
        Set<String> sources = entries.stream().map(entry -> entry.sourceId().toString()).collect(Collectors.toSet());
        assertEquals(entries.size(), sources.size(), "duplicate source ids in burnt replacement table");
        assertFalse(entries.isEmpty(), "burnt replacement table must not be empty");
    }

    private static List<String> readTagValues(final Path path) throws IOException {
        JsonObject root = JsonParser.parseReader(Files.newBufferedReader(path)).getAsJsonObject();
        JsonArray values = root.getAsJsonArray("values");
        List<String> resolved = new ArrayList<>(values.size());
        values.forEach(element -> resolved.add(element.getAsString()));
        return resolved;
    }

    private static void assertExists(final Path path) {
        assertTrue(Files.exists(path), "missing generated asset " + path);
    }
}
