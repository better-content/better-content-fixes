package io.github.btmfixes.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class BtmFixesConfig {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue DYNAMIC_TREES_SEASON_CONTEXT_CONCURRENT_MAP;
    public static final ForgeConfigSpec.BooleanValue HYLE_SAFE_TERTIARY_SELECTION;
    public static final ForgeConfigSpec.BooleanValue APOTHEOSIS_SKIP_OFF_THREAD_TOOLTIPS;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("dynamicTrees");
        DYNAMIC_TREES_SEASON_CONTEXT_CONCURRENT_MAP = builder
                .comment(
                        "Replaces Dynamic Trees' NormalSeasonManager season context HashMap with a ConcurrentHashMap.",
                        "This is intended to prevent ConcurrentModificationException during parallel feature generation, especially with C2ME threaded features.",
                        "Disable this if Dynamic Trees changes this internals or if diagnosing unrelated season behavior.")
                .define("seasonContextConcurrentMap", true);
        builder.pop();

        builder.push("hyle");
        HYLE_SAFE_TERTIARY_SELECTION = builder
                .comment(
                        "Guards Hyle tertiary stone selection against invalid indices.",
                        "Prevents worldgen crashes when Hyle's tertiary selector requests an index outside the available tertiary list size.",
                        "Keeps Unearthed/Hyle stone replacement enabled while avoiding ArrayIndexOutOfBoundsException in RoughNoiseSampler.selectTertiary.")
                .define("safeTertiarySelection", true);
        builder.pop();

        builder.push("apotheosis");
        APOTHEOSIS_SKIP_OFF_THREAD_TOOLTIPS = builder
                .comment(
                        "Skips Apotheosis' client tooltip augmentation when another mod computes tooltips off the Minecraft client thread.",
                        "This prevents C2ME's safe-random guard from reporting off-thread world random access during EMI tooltip indexing.",
                        "Normal render-thread tooltips are left unchanged.")
                .define("skipOffThreadTooltips", true);
        builder.pop();

        SPEC = builder.build();
    }

    private BtmFixesConfig() {
    }

    public static boolean dynamicTreesSeasonContextConcurrentMap() {
        return DYNAMIC_TREES_SEASON_CONTEXT_CONCURRENT_MAP.get();
    }

    public static boolean hyleSafeTertiarySelection() {
        return HYLE_SAFE_TERTIARY_SELECTION.get();
    }

    public static boolean apotheosisSkipOffThreadTooltips() {
        return APOTHEOSIS_SKIP_OFF_THREAD_TOOLTIPS.get();
    }
}
