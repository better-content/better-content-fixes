package com.bettercontent.bettercontentfixes.client.hud;

import net.minecraft.resources.ResourceLocation;

public final class DynamicHudRenderState {
    private static ResourceLocation activeOverlay;
    private static float alpha = 1.0F;

    private DynamicHudRenderState() {
    }

    public static void begin(final ResourceLocation overlay, final float overlayAlpha) {
        activeOverlay = overlay;
        alpha = clamp(overlayAlpha);
    }

    public static boolean isActive(final ResourceLocation overlay) {
        return activeOverlay != null && activeOverlay.equals(overlay);
    }

    public static boolean hasActiveOverlay() {
        return activeOverlay != null;
    }

    public static void clear() {
        activeOverlay = null;
        alpha = 1.0F;
    }

    public static float multiplyAlpha(final float requestedAlpha) {
        return requestedAlpha * alpha;
    }

    private static float clamp(final float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }
}
