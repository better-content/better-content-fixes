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
        assertTrue(clientMixins.contains(new JsonPrimitive("pneumaticcraft.PneumaticCraftRecipeTypeMixin")),
                "PneumaticCraft recipe-level recovery must remain client-only and packaged");
        assertTrue(mixins.contains(new JsonPrimitive("forge.VanillaInventoryCodeHooksMixin")),
                "Forge hopper extraction bridge must remain a common mixin");
        assertTrue(mixins.contains(new JsonPrimitive("sophisticatedstorage.StorageBlockEntityMixin")),
                "Sophisticated Storage barrel bridge must remain a common mixin");
        assertTrue(mixins.contains(new JsonPrimitive("minecraft.FeatureSorterMixin")),
                "repeated worldgen features must be removed before feature-order sorting");
        assertTrue(mixins.contains(new JsonPrimitive("thirst.AddLootTableModifierMixin")),
                "Thirst nested chest loot must bypass recursive global modifiers");
        assertTrue(mixins.contains(new JsonPrimitive("rehooked.HookEntityMixin")),
                "ReHooked hook entities must carry synchronized mob targets");
        assertTrue(mixins.contains(new JsonPrimitive("rehooked.SPlayerHookHandlerMixin")),
                "ReHooked server handlers must apply weight-based mob tugging");
        assertTrue(mixins.contains(new JsonPrimitive("epicfightvs.ColliderMixin")),
                "Epic Fight single colliders must be transformed on mounted ships");
        assertTrue(mixins.contains(new JsonPrimitive("epicfightvs.MultiColliderMixin")),
                "Epic Fight multi-colliders must be transformed on mounted ships");
        assertTrue(clientMixins.contains(new JsonPrimitive("epicfightvs.CameraMixin")),
                "the ship-aware Epic Fight camera bridge must remain client-only");
        assertTrue(clientMixins.contains(new JsonPrimitive("epicfightvs.EpicFightCameraApiMixin")),
                "the ship-aware Epic Fight camera ray bridge must remain client-only");
        assertTrue(mixins.contains(new JsonPrimitive("adpother.coldsweat.HearthBlockEntityMixin")),
                "Cold Sweat fuel consumption must retain its AdPother callback");
        assertTrue(mixins.contains(new JsonPrimitive("adpother.littlelogistics.SteamLocomotiveEntityMixin")),
                "steam locomotive fuel consumption must retain its AdPother callback");
        assertTrue(mixins.contains(new JsonPrimitive("adpother.littlelogistics.SteamTugEntityMixin")),
                "steam tug fuel consumption must retain its AdPother callback");
        assertTrue(mixins.contains(new JsonPrimitive("valkyrienskies.VibrationSystemTickerMixin")),
                "Valkyrien Skies sculk destinations must be transformed to world space");
        assertTrue(mixins.contains(new JsonPrimitive("jsonthings.VanillaPackResourcesBuilderMixin")),
                "vanilla pack root discovery must stay limited to built-in pack types");
        assertTrue(mixins.contains(new JsonPrimitive("rbp.BlockDefinitionCatalogFactoryMixin")),
                "Realistic Block Physics definitions must be filtered to usable registered states");
        assertTrue(clientMixins.contains(new JsonPrimitive("adpother.LevelRendererMixin")),
                "acid-rain texture selection must follow vanilla precipitation bindings");
        assertTrue(clientMixins.contains(new JsonPrimitive("sodiumdynamiclights.SodiumDynamicLightsMixin")),
                "dynamic-light resource registration must be scheduled on the client thread");
    }

    @Test
    void voidWormRemovalShipsAsAnAlexsMobsConditionalBiomeModifier() throws IOException {
        JsonObject modifier = JsonParser.parseReader(Files.newBufferedReader(Path.of(
                "src/main/resources/data/better_content_fixes/forge/biome_modifier/remove_void_worm_spawns.json"
        ))).getAsJsonObject();

        assertEquals("better_content_fixes:void_worm_spawn_removal", modifier.get("type").getAsString());
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
    void featureSorterMixinTargetsTheProductionSrgMethodWithoutARefmap() throws IOException {
        String source = Files.readString(Path.of(
                "src/main/java/com/bettercontent/bettercontentfixes/mixin/minecraft/FeatureSorterMixin.java"));

        assertTrue(source.contains("@WrapMethod(method = \"m_220603_\", remap = false)"),
                "the production JAR has no refmap, so the feature sorter hook must use its 1.20.1 SRG name");
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

    private static void assertMixinClassesExist(String packageName, JsonArray mixins) {
        mixins.forEach(element -> {
            String relativeClass = element.getAsString().replace('.', '/') + ".java";
            Path classPath = Path.of("src/main/java", packageName.replace('.', '/'), relativeClass);
            assertTrue(Files.exists(classPath), "missing mixin class " + classPath);
        });
    }
}
