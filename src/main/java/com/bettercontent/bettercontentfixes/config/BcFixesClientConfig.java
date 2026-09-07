package com.bettercontent.bettercontentfixes.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class BcFixesClientConfig {
    public static final int CURRENT_KEYMAP_PROFILE_VERSION = 1;
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue PARCOOL_DIRECTIONAL_DOUBLE_TAP_DODGE;
    public static final ForgeConfigSpec.IntValue PARCOOL_DOUBLE_TAP_WINDOW_TICKS;
    public static final ForgeConfigSpec.BooleanValue PARCOOL_REPLACE_FORWARD_DOUBLE_TAP_SPRINT;
    public static final ForgeConfigSpec.BooleanValue EPIC_FIGHT_HIDE_FIRST_PERSON_LIMBS;
    public static final ForgeConfigSpec.BooleanValue APPLY_RECOMMENDED_KEYMAP_MIGRATION;
    public static final ForgeConfigSpec.IntValue KEYMAP_PROFILE_VERSION;

    static {
        final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("parcool");
        PARCOOL_DIRECTIONAL_DOUBLE_TAP_DODGE = builder
                .comment("Dodges through ParCool when the same movement direction is tapped twice.")
                .define("directionalDoubleTapDodge", true);
        PARCOOL_DOUBLE_TAP_WINDOW_TICKS = builder
                .comment("Maximum inclusive client-tick distance between directional presses.")
                .defineInRange("doubleTapWindowTicks", 7, 2, 20);
        PARCOOL_REPLACE_FORWARD_DOUBLE_TAP_SPRINT = builder
                .comment(
                        "Disables vanilla forward double-tap sprint while directional ParCool dodge is enabled.",
                        "The configured sprint key continues to work normally.")
                .define("replaceForwardDoubleTapSprint", true);
        builder.pop();

        builder.push("epicFight");
        EPIC_FIGHT_HIDE_FIRST_PERSON_LIMBS = builder
                .comment(
                        "Hides the local player's arm and leg geometry in Epic Fight first person.",
                        "Held-item rendering and Epic Fight weapon animation layers remain enabled.")
                .define("hideFirstPersonLimbs", true);
        builder.pop();

        builder.push("controls");
        APPLY_RECOMMENDED_KEYMAP_MIGRATION = builder
                .comment("Migrates unchanged legacy pack bindings to the current recommended control profile once.")
                .define("applyRecommendedKeymapMigration", true);
        KEYMAP_PROFILE_VERSION = builder
                .comment("Internal version marker for completed recommended-keymap migrations.")
                .defineInRange("keymapProfileVersion", 0, 0, CURRENT_KEYMAP_PROFILE_VERSION);
        builder.pop();

        SPEC = builder.build();
    }

    private BcFixesClientConfig() {
    }

    public static boolean parcoolDirectionalDoubleTapDodge() {
        return !SPEC.isLoaded() || PARCOOL_DIRECTIONAL_DOUBLE_TAP_DODGE.get();
    }

    public static int parcoolDoubleTapWindowTicks() {
        return SPEC.isLoaded() ? PARCOOL_DOUBLE_TAP_WINDOW_TICKS.get() : 7;
    }

    public static boolean replaceForwardDoubleTapSprint() {
        return parcoolDirectionalDoubleTapDodge()
                && (!SPEC.isLoaded() || PARCOOL_REPLACE_FORWARD_DOUBLE_TAP_SPRINT.get());
    }

    public static boolean hideFirstPersonLimbs() {
        return !SPEC.isLoaded() || EPIC_FIGHT_HIDE_FIRST_PERSON_LIMBS.get();
    }

    public static boolean shouldMigrateRecommendedKeymap() {
        return SPEC.isLoaded()
                && APPLY_RECOMMENDED_KEYMAP_MIGRATION.get()
                && KEYMAP_PROFILE_VERSION.get() < CURRENT_KEYMAP_PROFILE_VERSION;
    }
}
