package com.bettercontent.bettercontentfixes.client.hud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

final class DynamicHudRenderStateTest {
    private static final ResourceLocation HEALTH = new ResourceLocation("minecraft", "player_health");

    @AfterEach
    void clear() {
        DynamicHudRenderState.clear();
    }

    @Test
    void multipliesAnOverlayRequestedAlpha() {
        DynamicHudRenderState.begin(HEALTH, 0.4F);

        assertTrue(DynamicHudRenderState.hasActiveOverlay());
        assertTrue(DynamicHudRenderState.isActive(HEALTH));
        assertEquals(0.3F, DynamicHudRenderState.multiplyAlpha(0.75F), 0.0001F);
    }

    @Test
    void clampsAndClearsOverlayState() {
        DynamicHudRenderState.begin(HEALTH, -2.0F);
        assertEquals(0.0F, DynamicHudRenderState.multiplyAlpha(1.0F));

        DynamicHudRenderState.clear();
        assertFalse(DynamicHudRenderState.hasActiveOverlay());
        assertFalse(DynamicHudRenderState.isActive(HEALTH));
        assertEquals(0.75F, DynamicHudRenderState.multiplyAlpha(0.75F));
    }
}
