package io.github.bcfixes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.bcfixes.compat.RegolithFarmlandDefinitions;
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
            assertExists(BCFIXES_ROOT.resolve("src/main/resources/assets/bcfixes/blockstates/" + path + ".json"));
            assertExists(BCFIXES_ROOT.resolve("src/main/resources/assets/bcfixes/models/block/" + path + ".json"));
            assertExists(BCFIXES_ROOT.resolve("src/main/resources/assets/bcfixes/models/block/" + path + "_moist.json"));
            assertExists(BCFIXES_ROOT.resolve("src/main/resources/assets/bcfixes/models/item/" + path + ".json"));
            assertExists(BCFIXES_ROOT.resolve("src/main/resources/assets/bcfixes/textures/block/" + path + ".png"));
            assertExists(BCFIXES_ROOT.resolve("src/main/resources/assets/bcfixes/textures/block/" + path + "_moist.png"));
            assertExists(BCFIXES_ROOT.resolve("src/main/resources/data/bcfixes/loot_tables/blocks/" + path + ".json"));
        }
    }

    private static void assertExists(final Path path) {
        assertTrue(Files.exists(path), "missing generated asset " + path);
    }
}
