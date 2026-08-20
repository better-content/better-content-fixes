package com.bettercontent.bettercontentfixes;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

final class CombatRollResourceTest {
    @Test
    void localPlayerCompatibilityMixinStaysClientOnly() throws IOException {
        JsonObject config = JsonParser.parseReader(Files.newBufferedReader(
                Path.of("src/main/resources/better_content_fixes.mixins.json"))).getAsJsonObject();

        assertTrue(config.getAsJsonArray("client").toString().contains("minecraft.LocalPlayerMixin"));
        assertTrue(!config.getAsJsonArray("mixins").toString().contains("minecraft.LocalPlayerMixin"));
    }

    @Test
    void buildPinsTheSameCombatRollReleaseAsTheModpack() throws IOException {
        String build = Files.readString(Path.of("build.gradle.kts"));

        assertTrue(build.contains("curse.maven:combat-roll-678036:5625925"));
    }
}
