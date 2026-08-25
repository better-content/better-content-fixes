package com.bettercontent.bettercontentfixes.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class BcFixesClientConfig {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue DYNAMIC_HUD_ENABLED;
    public static final ForgeConfigSpec.DoubleValue DYNAMIC_HUD_HOLD_SECONDS;
    public static final ForgeConfigSpec.DoubleValue DYNAMIC_HUD_FADE_SECONDS;
    public static final ForgeConfigSpec.DoubleValue DYNAMIC_HUD_DANGER_FRACTION;

    static {
        final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("dynamicHud");
        DYNAMIC_HUD_ENABLED = builder
                .comment(
                        "Fades inactive survival HUD elements while leaving chat, crosshair, status effects, boss bars, and full-screen effects unchanged.",
                        "Sneak temporarily reveals every managed element; releasing sneak restarts the hold period.")
                .define("enabled", true);
        DYNAMIC_HUD_HOLD_SECONDS = builder
                .comment("Seconds a changed HUD element stays fully visible before fading.")
                .defineInRange("holdSeconds", 5.0D, 0.0D, 60.0D);
        DYNAMIC_HUD_FADE_SECONDS = builder
                .comment("Seconds used for the linear fade from fully visible to hidden.")
                .defineInRange("fadeSeconds", 0.5D, 0.0D, 10.0D);
        DYNAMIC_HUD_DANGER_FRACTION = builder
                .comment(
                        "Health, hunger, and thirst remain visible at or below this fraction of their maximum.",
                        "Air remains visible whenever it is not full; severe Cold Sweat temperatures remain visible independently.")
                .defineInRange("dangerFraction", 1.0D / 3.0D, 0.0D, 1.0D);
        builder.pop();

        SPEC = builder.build();
    }

    private BcFixesClientConfig() {
    }

    public static boolean dynamicHudEnabled() {
        return DYNAMIC_HUD_ENABLED.get();
    }

    public static int dynamicHudHoldTicks() {
        return secondsToTicks(DYNAMIC_HUD_HOLD_SECONDS.get());
    }

    public static int dynamicHudFadeTicks() {
        return secondsToTicks(DYNAMIC_HUD_FADE_SECONDS.get());
    }

    public static double dynamicHudDangerFraction() {
        return DYNAMIC_HUD_DANGER_FRACTION.get();
    }

    static int secondsToTicks(final double seconds) {
        return (int) Math.round(seconds * 20.0D);
    }
}
