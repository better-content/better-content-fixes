package com.bettercontent.bettercontentfixes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

final class TconCompatResourceTest {
    @Test
    void craftingStationMixinStaysConditionallyRegistered() throws IOException {
        JsonObject config = JsonParser.parseReader(Files.newBufferedReader(
                Path.of("src/main/resources/better_content_fixes.mixins.json"))).getAsJsonObject();

        assertEquals(
                "com.bettercontent.bettercontentfixes.mixin.BetterContentMixinPlugin",
                config.get("plugin").getAsString());
        assertTrue(config.getAsJsonArray("mixins").toString()
                .contains("tconstruct.CraftingStationPolymorphMixin"));
    }

    @Test
    void buildPinsThePackTconAndPolymorphReleases() throws IOException {
        String build = Files.readString(Path.of("build.gradle.kts"));
        String modsToml = Files.readString(Path.of("src/main/resources/META-INF/mods.toml"));

        assertTrue(build.contains("curse.maven:mantle-74924:7563777"));
        assertTrue(build.contains("curse.maven:tinkers-construct-74072:7449219"));
        assertTrue(build.contains("curse.maven:polymorph-388800:6450982"));
        assertTrue(modsToml.contains("modId=\"tconstruct\""));
        assertTrue(modsToml.contains("versionRange=\"[3.11.2.166,3.12)\""));
        assertTrue(modsToml.contains("modId=\"polymorph\""));
        assertTrue(modsToml.contains("versionRange=\"[0.49.10,0.50)\""));
    }
}
