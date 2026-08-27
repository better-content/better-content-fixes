package com.bettercontent.bettercontentfixes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bettercontent.bettercontentfixes.compat.RegolithFarmlandDefinitions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

final class RegolithFarmlandPaletteResourceTest {
    private static final Path BCFIXES_ROOT = Path.of(".");

    @Test
    void regolithFarmlandEntrySetStaysUniqueAndComplete() {
        final List<RegolithFarmlandDefinitions.Entry> entries = RegolithFarmlandDefinitions.entries();
        final Set<String> grassyRegolithIds = entries.stream()
                .map(entry -> entry.grassyRegolithId().toString())
                .collect(Collectors.toSet());
        final Set<String> plainRegolithIds = entries.stream()
                .map(entry -> entry.plainRegolithId().toString())
                .collect(Collectors.toSet());
        final Set<String> farmlandIds = entries.stream()
                .map(entry -> entry.farmlandId().toString())
                .collect(Collectors.toSet());

        assertEquals(entries.size(), grassyRegolithIds.size(), "duplicate grassy regolith sources in farmland table");
        assertEquals(entries.size(), plainRegolithIds.size(), "duplicate plain regolith sources in farmland table");
        assertEquals(entries.size(), farmlandIds.size(), "duplicate farmland ids in farmland table");
        assertEquals(17, entries.size(), "regolith farmland palette drifted from the authored Unearthed surface set");
        assertFalse(entries.isEmpty(), "regolith farmland table must not be empty");
    }

    @Test
    void customEntriesHaveGeneratedAssets() {
        for (RegolithFarmlandDefinitions.Entry entry : RegolithFarmlandDefinitions.entries()) {
            final String path = entry.farmlandId().getPath();
            assertExists(BCFIXES_ROOT.resolve("src/main/resources/assets/better_content_fixes/blockstates/" + path + ".json"));
            assertExists(BCFIXES_ROOT.resolve("src/main/resources/assets/better_content_fixes/models/block/" + path + ".json"));
            assertExists(BCFIXES_ROOT.resolve("src/main/resources/assets/better_content_fixes/models/block/" + path + "_moist.json"));
            assertExists(BCFIXES_ROOT.resolve("src/main/resources/assets/better_content_fixes/models/item/" + path + ".json"));
            assertExists(BCFIXES_ROOT.resolve("src/main/resources/assets/better_content_fixes/textures/block/" + path + ".png"));
            assertExists(BCFIXES_ROOT.resolve("src/main/resources/assets/better_content_fixes/textures/block/" + path + "_moist.png"));
            assertExists(BCFIXES_ROOT.resolve("src/main/resources/data/better_content_fixes/loot_tables/blocks/" + path + ".json"));
        }
    }

    @Test
    void definingModContributesEveryRegolithFarmlandToCommonTag() throws IOException {
        final Path tagPath = BCFIXES_ROOT.resolve("src/main/resources/data/c/tags/blocks/farmland.json");
        final JsonObject tag = JsonParser.parseReader(Files.newBufferedReader(tagPath)).getAsJsonObject();
        final JsonArray values = tag.getAsJsonArray("values");
        final List<String> taggedIds = values.asList().stream()
                .map(element -> element.getAsString())
                .toList();

        assertFalse(tag.get("replace").getAsBoolean(), "common farmland contributions must merge");
        assertEquals(RegolithFarmlandDefinitions.entries().size(), taggedIds.size(),
                "common farmland tag must contain only the complete owned palette");
        for (RegolithFarmlandDefinitions.Entry entry : RegolithFarmlandDefinitions.entries()) {
            assertEquals(1, taggedIds.stream().filter(entry.farmlandId().toString()::equals).count(),
                    "common farmland tag must contain exactly one " + entry.farmlandId());
        }
    }

    private static void assertExists(final Path path) {
        assertTrue(Files.exists(path), "missing generated asset " + path);
    }
}
