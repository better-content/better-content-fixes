package io.github.btmfixes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.btmfixes.compat.RegolithFarmlandDefinitions;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

final class RegolithFarmlandPaletteResourceTest {
    private static final Path BTMFIXES_ROOT = Path.of(".");

    @Test
    void regolithFarmlandEntrySetStaysUniqueAndComplete() {
        final List<RegolithFarmlandDefinitions.Entry> entries = RegolithFarmlandDefinitions.entries();
        final Set<String> grassyRegolithIds = entries.stream()
                .map(entry -> entry.grassyRegolithId().toString())
                .collect(Collectors.toSet());
        final Set<String> farmlandIds = entries.stream()
                .map(entry -> entry.farmlandId().toString())
                .collect(Collectors.toSet());

        assertEquals(entries.size(), grassyRegolithIds.size(), "duplicate grassy regolith sources in farmland table");
        assertEquals(entries.size(), farmlandIds.size(), "duplicate farmland ids in farmland table");
        assertEquals(17, entries.size(), "regolith farmland palette drifted from the authored Unearthed surface set");
        assertFalse(entries.isEmpty(), "regolith farmland table must not be empty");
    }

    @Test
    void customEntriesHaveGeneratedAssets() {
        for (RegolithFarmlandDefinitions.Entry entry : RegolithFarmlandDefinitions.entries()) {
            final String path = entry.farmlandId().getPath();
            assertExists(BTMFIXES_ROOT.resolve("src/main/resources/assets/btmfixes/blockstates/" + path + ".json"));
            assertExists(BTMFIXES_ROOT.resolve("src/main/resources/assets/btmfixes/models/block/" + path + ".json"));
            assertExists(BTMFIXES_ROOT.resolve("src/main/resources/assets/btmfixes/models/block/" + path + "_moist.json"));
            assertExists(BTMFIXES_ROOT.resolve("src/main/resources/assets/btmfixes/models/item/" + path + ".json"));
            assertExists(BTMFIXES_ROOT.resolve("src/main/resources/assets/btmfixes/textures/block/" + path + ".png"));
            assertExists(BTMFIXES_ROOT.resolve("src/main/resources/assets/btmfixes/textures/block/" + path + "_moist.png"));
            assertExists(BTMFIXES_ROOT.resolve("src/main/resources/data/btmfixes/loot_tables/blocks/" + path + ".json"));
        }
    }

    private static void assertExists(final Path path) {
        assertTrue(Files.exists(path), "missing generated asset " + path);
    }
}
