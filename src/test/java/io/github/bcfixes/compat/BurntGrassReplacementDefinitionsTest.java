package io.github.bcfixes.compat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

final class BurntGrassReplacementDefinitionsTest {
    @Test
    void parseEntryDerivesPackOwnedTargetWhenMissing() {
        BurntGrassReplacementDefinitions.Entry entry = BurntGrassReplacementDefinitions.parseEntry(jsonObject("""
                {
                  "source": "minecraft:grass_block"
                }
                """));

        assertEquals(id("minecraft:grass_block"), entry.sourceId());
        assertEquals(id("bcfixes:burnt_grass_block"), entry.targetId());
    }

    @Test
    void parseEntryKeepsExplicitTarget() {
        BurntGrassReplacementDefinitions.Entry entry = BurntGrassReplacementDefinitions.parseEntry(jsonObject("""
                {
                  "source": "minecraft:grass_block",
                  "target": "burnt:burnt_grass"
                }
                """));

        assertEquals(id("burnt:burnt_grass"), entry.targetId());
    }

    @Test
    void parseEntryRejectsInvalidIds() {
        assertThrows(IllegalArgumentException.class, () -> BurntGrassReplacementDefinitions.parseEntry(jsonObject("""
                {
                  "source": "not a valid id"
                }
                """)));

        assertThrows(IllegalArgumentException.class, () -> BurntGrassReplacementDefinitions.parseEntry(jsonObject("""
                {
                  "source": "minecraft:grass_block",
                  "target": "still not valid"
                }
                """)));
    }

    @Test
    void parseEntriesPreservesResourceOrder() {
        var entries = BurntGrassReplacementDefinitions.parseEntries(jsonObject("""
                {
                  "values": [
                    { "source": "minecraft:grass_block" },
                    { "source": "minecraft:mycelium", "target": "burnt:burnt_grass" }
                  ]
                }
                """));

        assertEquals(2, entries.size());
        assertEquals(id("minecraft:grass_block"), entries.get(0).sourceId());
        assertEquals(id("minecraft:mycelium"), entries.get(1).sourceId());
    }

    private static JsonObject jsonObject(String json) {
        return JsonParser.parseString(json).getAsJsonObject();
    }

    private static ResourceLocation id(String value) {
        return ResourceLocation.parse(value);
    }
}
