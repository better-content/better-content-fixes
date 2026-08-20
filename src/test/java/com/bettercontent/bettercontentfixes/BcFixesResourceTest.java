package com.bettercontent.bettercontentfixes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

final class BcFixesResourceTest {
    @Test
    void mixinConfigTargetsExistingMixinClasses() throws IOException {
        Path configPath = Path.of("src/main/resources/better_content_fixes.mixins.json");
        JsonObject config = JsonParser.parseReader(Files.newBufferedReader(configPath)).getAsJsonObject();
        String packageName = config.get("package").getAsString();
        JsonArray mixins = config.getAsJsonArray("mixins");
        JsonArray clientMixins = config.getAsJsonArray("client");

        assertTrue(mixins != null && mixins.size() > 0, "expected packaged mixin targets");
        assertTrue(clientMixins != null && clientMixins.size() > 0, "expected packaged client mixin targets");
        assertMixinClassesExist(packageName, mixins);
        assertMixinClassesExist(packageName, clientMixins);
        assertTrue(clientMixins.contains(new JsonPrimitive("thefleshthathates.BiomeMusicMixin")),
                "TFTH proximity music suppression must remain client-only and packaged");
        assertTrue(clientMixins.contains(new JsonPrimitive("weather2.FogAdjusterMixin")),
                "Weather2 shader-fog compatibility must remain client-only and packaged");
        assertTrue(mixins.contains(new JsonPrimitive("forge.VanillaInventoryCodeHooksMixin")),
                "Forge hopper extraction bridge must remain a common mixin");
        assertTrue(mixins.contains(new JsonPrimitive("sophisticatedstorage.StorageBlockEntityMixin")),
                "Sophisticated Storage barrel bridge must remain a common mixin");
    }

    @Test
    void mixinConfigKeepsOptionalTargetsNonRequired() throws IOException {
        JsonObject config = JsonParser.parseReader(Files.newBufferedReader(
                Path.of("src/main/resources/better_content_fixes.mixins.json"))).getAsJsonObject();

        assertTrue(!config.get("required").getAsBoolean(), "optional compatibility mixins must stay non-required");
        assertTrue(config.getAsJsonObject("injectors").get("defaultRequire").getAsInt() == 0,
                "optional compatibility injectors should not require target matches");
    }

    @Test
    void fluidGeneratedBlocksAllowlistDefaultsToObsidianOnly() throws IOException {
        Path tagPath = Path.of("src/main/resources/data/better_content_fixes/tags/blocks/allowed_fluid_generated_blocks.json");
        JsonObject tag = JsonParser.parseReader(Files.newBufferedReader(tagPath)).getAsJsonObject();
        JsonArray values = tag.getAsJsonArray("values");

        assertEquals(1, values.size(), "fluid-generated allowlist should stay narrow by default");
        assertEquals("minecraft:obsidian", values.get(0).getAsString(),
                "obsidian should remain the only default allowed fluid-generated block");
    }

    @Test
    void rainCollectorShipsAllVisibleLevelsAndWoodTierRecipe() throws IOException {
        Path statePath = Path.of("src/main/resources/assets/better_content_fixes/blockstates/rain_collector.json");
        JsonObject state = JsonParser.parseReader(Files.newBufferedReader(statePath)).getAsJsonObject();
        String serialized = state.toString();
        for (int level = 1; level <= 4; level++) {
            assertTrue(serialized.contains("rain_collector_water_" + level), "missing visible level " + level);
            assertTrue(Files.exists(Path.of("src/main/resources/assets/better_content_fixes/models/block/rain_collector_water_" + level + ".json")));
        }

        JsonObject recipe = JsonParser.parseReader(Files.newBufferedReader(
                Path.of("src/main/resources/data/better_content_fixes/recipes/rain_collector.json"))).getAsJsonObject();
        assertEquals("minecraft:planks", recipe.getAsJsonObject("key").getAsJsonObject("#").get("tag").getAsString());
        assertEquals("better_content_fixes:rain_collector", recipe.getAsJsonObject("result").get("item").getAsString());
    }

    private static void assertMixinClassesExist(String packageName, JsonArray mixins) {
        mixins.forEach(element -> {
            String relativeClass = element.getAsString().replace('.', '/') + ".java";
            Path classPath = Path.of("src/main/java", packageName.replace('.', '/'), relativeClass);
            assertTrue(Files.exists(classPath), "missing mixin class " + classPath);
        });
    }
}
