package com.bettercontent.bettercontentfixes;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

final class MovementPresentationResourceTest {
    private static final Path MIXIN_CONFIG = Path.of("src/main/resources/better_content_fixes.mixins.json");
    private static final Path SOURCE_ROOT = Path.of("src/main/java/com/bettercontent/bettercontentfixes");

    @Test
    void clientMixinsArePackagedOnTheClientSide() throws IOException {
        final JsonObject config = JsonParser.parseReader(Files.newBufferedReader(MIXIN_CONFIG)).getAsJsonObject();
        final String client = config.getAsJsonArray("client").toString();

        assertTrue(client.contains("parcool.DodgeMixin"));
        assertFalse(client.contains("minecraft.LocalPlayerSprintMixin"));
        assertTrue(client.contains("epicfight.FirstPersonRendererMixin"));
        assertTrue(client.contains("epicfight.WearableItemLayerMixin"));
        assertFalse(client.contains("epicfightfirstperson"));
    }

    @Test
    void optionalCompatibilityIsPinnedToInspectedVersions() throws IOException {
        final String plugin = Files.readString(SOURCE_ROOT.resolve("mixin/BetterContentMixinPlugin.java"));
        final String metadata = Files.readString(Path.of("src/main/resources/META-INF/mods.toml"));

        assertTrue(plugin.contains("hasVersion(mods, \"parcool\", \"3.4.3.3\")"));
        assertTrue(plugin.contains("hasVersion(mods, \"epicfight\", \"20.14.17\")"));
        assertTrue(metadata.contains("modId=\"parcool\""));
        assertTrue(metadata.contains("modId=\"pingwheel\""));
        assertFalse(plugin.contains("epicfight_first_person_model"));
        assertFalse(metadata.contains("epicfight_first_person_model"));
    }

    @Test
    void directionalDoubleTapDodgeIsDisabledWithoutReplacingVanillaSprint() throws IOException {
        final String mixin = Files.readString(SOURCE_ROOT.resolve("mixin/parcool/DodgeMixin.java"));
        final String config = Files.readString(SOURCE_ROOT.resolve("config/BcFixesClientConfig.java"));

        assertTrue(mixin.contains("return Boolean.FALSE"));
        assertFalse(config.contains("directionalDoubleTapDodge"));
        assertFalse(config.contains("doubleTapWindowTicks"));
        assertFalse(config.contains("replaceForwardDoubleTapSprint"));
        assertFalse(Files.exists(SOURCE_ROOT.resolve("client/ParCoolDirectionalDodgeClient.java")));
        assertFalse(Files.exists(SOURCE_ROOT.resolve("client/DirectionalDoubleTapTracker.java")));
        assertFalse(Files.exists(SOURCE_ROOT.resolve("client/VanillaDoubleTapSprintSuppressor.java")));
    }

    @Test
    void firstPersonPolicyHidesLimbsWithoutTouchingHeldItemRendering() throws IOException {
        final String visibility = Files.readString(SOURCE_ROOT.resolve("client/FirstPersonLimbVisibility.java"));
        final String renderer = Files.readString(SOURCE_ROOT.resolve("mixin/epicfight/FirstPersonRendererMixin.java"));
        final String armorRenderer = Files.readString(
                SOURCE_ROOT.resolve("mixin/epicfight/WearableItemLayerMixin.java"));

        assertTrue(visibility.contains("leftArm.setHidden(true)"));
        assertTrue(visibility.contains("rightArm.setHidden(true)"));
        assertTrue(visibility.contains("leftLeg.setHidden(true)"));
        assertTrue(visibility.contains("rightLeg.setHidden(true)"));
        assertTrue(visibility.contains("leftSleeve.setHidden(true)"));
        assertTrue(visibility.contains("rightSleeve.setHidden(true)"));
        assertTrue(visibility.contains("leftPants.setHidden(true)"));
        assertTrue(visibility.contains("rightPants.setHidden(true)"));
        assertTrue(visibility.contains("hideArmorLimbs"));
        assertTrue(renderer.contains("hidePlayerLimbs"));
        assertTrue(renderer.contains("at = @At(\"TAIL\")"));
        assertTrue(armorRenderer.contains("WearableItemLayer.class"));
        assertTrue(armorRenderer.contains("firstPersonModel"));
        assertTrue(armorRenderer.contains("WearableItemLayer;renderArmor"));
        assertTrue(armorRenderer.contains("hideFirstPersonArmorLimbsBeforeDraw"));
        assertTrue(!visibility.contains("PatchedItemInHandLayer"));
        assertTrue(!renderer.contains("PatchedItemInHandLayer"));
        assertTrue(!armorRenderer.contains("PatchedItemInHandLayer"));
        assertFalse(Files.exists(SOURCE_ROOT.resolve(
                "mixin/epicfightfirstperson/FirstPersonBodyRendererMixin.java")));
        assertFalse(Files.exists(SOURCE_ROOT.resolve(
                "mixin/epicfightfirstperson/FirstPersonWearableItemLayerMixin.java")));
    }
}
