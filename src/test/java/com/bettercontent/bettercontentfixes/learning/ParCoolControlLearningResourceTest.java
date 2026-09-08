package com.bettercontent.bettercontentfixes.learning;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

final class ParCoolControlLearningResourceTest {
    private static final Path SOURCE_ROOT = Path.of(
            "src/main/java/com/bettercontent/bettercontentfixes");

    @Test
    void usesTheAuthoritativeServerStartBoundaryAndPersistentEpisode() throws IOException {
        final String source = Files.readString(SOURCE_ROOT.resolve("learning/ParCoolControlLearning.java"));
        assertTrue(source.contains("ParCoolActionEvent.Start.Post"));
        assertTrue(source.contains("instanceof ServerPlayer"));
        assertTrue(source.contains("Player.PERSISTED_NBT_TAG"));
        assertTrue(source.contains("custom_control_used"));
        assertTrue(source.contains("custom_control_mastered"));
        assertTrue(source.contains("distinct_second"));
    }

    @Test
    void optionalBridgeTargetsOnlyThePublicThreadsApi() throws IOException {
        final String bridge = Files.readString(SOURCE_ROOT.resolve("learning/ThreadSignalsBridge.java"));
        assertTrue(bridge.contains("com.bettercontent.threads.api.ThreadSignals"));
        assertTrue(bridge.contains("ServerPlayer.class, String.class, String.class, String.class"));
        assertTrue(bridge.contains("activeCorrelation"));
        assertTrue(!bridge.contains("com.bettercontent.threads.ThreadSignals"));
    }
}
