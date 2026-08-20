package com.bettercontent.bettercontentfixes.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class BcFixesClientConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue COMBAT_ROLL_DIRECTIONAL_DOUBLE_TAP_ENABLED;
    public static final ForgeConfigSpec.IntValue COMBAT_ROLL_DOUBLE_TAP_WINDOW_TICKS;
    public static final ForgeConfigSpec.BooleanValue COMBAT_ROLL_REPLACE_FORWARD_DOUBLE_TAP_SPRINT;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("combatRoll");
        COMBAT_ROLL_DIRECTIONAL_DOUBLE_TAP_ENABLED = builder
                .comment("Rolls when the same configured movement direction is tapped twice.")
                .define("directionalDoubleTapEnabled", true);
        COMBAT_ROLL_DOUBLE_TAP_WINDOW_TICKS = builder
                .comment("Maximum inclusive number of client ticks between the first and second directional press.")
                .defineInRange("doubleTapWindowTicks", 7, 2, 20);
        COMBAT_ROLL_REPLACE_FORWARD_DOUBLE_TAP_SPRINT = builder
                .comment(
                        "Disables vanilla forward double-tap sprint while directional double-tap rolling is enabled.",
                        "The configured sprint key continues to work normally.")
                .define("replaceForwardDoubleTapSprint", true);
        builder.pop();

        SPEC = builder.build();
    }

    private BcFixesClientConfig() {
    }

    public static boolean combatRollDirectionalDoubleTapEnabled() {
        return COMBAT_ROLL_DIRECTIONAL_DOUBLE_TAP_ENABLED.get();
    }

    public static int combatRollDoubleTapWindowTicks() {
        return COMBAT_ROLL_DOUBLE_TAP_WINDOW_TICKS.get();
    }

    public static boolean combatRollReplaceForwardDoubleTapSprint() {
        return COMBAT_ROLL_DIRECTIONAL_DOUBLE_TAP_ENABLED.get()
                && COMBAT_ROLL_REPLACE_FORWARD_DOUBLE_TAP_SPRINT.get();
    }
}
