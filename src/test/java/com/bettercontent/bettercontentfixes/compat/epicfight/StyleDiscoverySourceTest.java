package com.bettercontent.bettercontentfixes.compat.epicfight;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

final class StyleDiscoverySourceTest {
    @Test
    void discoveryUsesAttackStartCapabilityAndPlayerWorldPersistence() throws IOException {
        final Path root = Path.of("src/main/java/com/bettercontent/bettercontentfixes");
        final String mixin = Files.readString(root.resolve("mixin/epicfight/ServerPlayerPatchMixin.java"));
        final String discovery = Files.readString(root.resolve("compat/epicfight/StyleDiscovery.java"));
        final String mixins = Files.readString(Path.of("src/main/resources/better_content_fixes.mixins.json"));

        assertTrue(mixin.contains("EventType.ANIMATION_BEGIN_EVENT"));
        assertTrue(mixin.contains("instanceof yesman.epicfight.api.animation.types.AttackAnimation"));
        assertTrue(mixin.contains("StyleDiscovery.observeAttackStart("));
        assertTrue(discovery.contains("patch.getHoldingItemCapability"));
        assertTrue(discovery.contains("patch.getAttackingHand()"));
        assertTrue(discovery.contains("player.getItemInHand(hand).copy()"));
        assertTrue(discovery.contains("capability.getStyle(patch)"));
        assertTrue(discovery.contains("player.getPersistentData()"));
        assertTrue(discovery.contains("displayClientMessage") && discovery.contains("attack with this weapon"));
        assertTrue(mixins.contains("\"epicfight.ServerPlayerPatchMixin\""));
    }
}
