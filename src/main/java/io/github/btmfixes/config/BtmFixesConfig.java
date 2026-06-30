package io.github.btmfixes.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModList;

public final class BtmFixesConfig {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue DYNAMIC_TREES_SEASON_CONTEXT_CONCURRENT_MAP;
    public static final ForgeConfigSpec.BooleanValue DYNAMIC_TREES_UNEARTHED_REGOLITH_SOILS;
    public static final ForgeConfigSpec.BooleanValue DYNAMIC_TREES_DESTROY_UNSUPPORTED_TREES;
    public static final ForgeConfigSpec.BooleanValue DYNAMIC_TREES_RECONSTRUCT_FALLEN_LOGS;
    public static final ForgeConfigSpec.BooleanValue HYLE_SAFE_TERTIARY_SELECTION;
    public static final ForgeConfigSpec.BooleanValue APOTHEOSIS_SKIP_OFF_THREAD_TOOLTIPS;
    public static final ForgeConfigSpec.BooleanValue ADVANCED_LOOT_INFO_SKIP_OFF_THREAD_EMI_REGISTRATION;
    public static final ForgeConfigSpec.BooleanValue SGI_RERUN_HYLE_AFTER_SURFACE_CONFORM;
    public static final ForgeConfigSpec.BooleanValue LOST_CITIES_SERIALIZE_DH_C2ME_FEATURE_PLACEMENT;
    public static final ForgeConfigSpec.BooleanValue LOST_CITIES_CANCEL_STALE_DH_CLIENT_REQUESTS;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("dynamicTrees");
        DYNAMIC_TREES_SEASON_CONTEXT_CONCURRENT_MAP = builder
                .comment(
                        "Replaces Dynamic Trees' NormalSeasonManager season context HashMap with a ConcurrentHashMap.",
                        "This is intended to prevent ConcurrentModificationException during parallel feature generation, especially with C2ME threaded features.",
                        "Disable this if Dynamic Trees changes this internals or if diagnosing unrelated season behavior.")
                .define("seasonContextConcurrentMap", true);
        DYNAMIC_TREES_UNEARTHED_REGOLITH_SOILS = builder
                .comment(
                        "Registers Unearthed/Hyle regolith and overgrown stone surface blocks as Dynamic Trees dirt-like soils.",
                        "Keeps Unearthed dirt replacement enabled while allowing Dynamic Trees worldgen to place forest trees on replaced forest surfaces.",
                        "Disable only when diagnosing Dynamic Trees soil registration behavior.")
                .define("unearthedRegolithSoils", true);
        DYNAMIC_TREES_DESTROY_UNSUPPORTED_TREES = builder
                .comment(
                        "Deconstructs Dynamic Trees trees when their rooty soil block no longer has a solid supporting block underneath.",
                        "Catches both immediate support loss from block updates and preexisting floating rooty soils when a chunk loads.",
                        "Applies to base Dynamic Trees rooty blocks and extension species that anchor through the same rooty soil system.")
                .define("destroyUnsupportedTrees", true);
        DYNAMIC_TREES_RECONSTRUCT_FALLEN_LOGS = builder
                .comment(
                        "When a Dynamic Trees falling tree lands, tries to reconstruct it as placed primitive logs instead of only dropping log items.",
                        "Uses the tree's destroyed branch voxel data to place sideways logs that roughly match the fallen shape, then leaves only any unplaced remainder as item drops.",
                        "Disable this if Dynamic Trees changes its falling tree internals or if placed-log reconstruction causes compatibility issues.")
                .define("reconstructFallenLogs", true);
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

        builder.push("advancedLootInfo");
        ADVANCED_LOOT_INFO_SKIP_OFF_THREAD_EMI_REGISTRATION = builder
                .comment(
                        "Skips Advanced Loot Info's EMI data registration when EMI runs it off the Minecraft client thread.",
                        "ALI creates example entities during EMI indexing; some entity constructors touch world random and trip C2ME's safe-random guard off-thread.",
                        "Normal render-thread ALI behavior is left unchanged.")
                .define("skipOffThreadEmiRegistration", true);
        builder.pop();

        builder.push("structureGenerationImprover");
        SGI_RERUN_HYLE_AFTER_SURFACE_CONFORM = builder
                .comment(
                        "Runs Hyle's stone replacer again after Structure Generation Improver conforms structure terrain.",
                        "SGI writes new vanilla terrain after normal biome decoration, which means Hyle/Unearthed has already run and cannot replace those blocks.",
                        "This post-pass keeps SGI foundations and blended terrain in the same Unearthed stone/regolith palette as surrounding terrain.")
                .define("rerunHyleAfterSurfaceConform", true);
        builder.pop();

        builder.push("lostCities");
        LOST_CITIES_SERIALIZE_DH_C2ME_FEATURE_PLACEMENT = builder
                .comment(
                        "Serializes Lost Cities feature placement only in the lostcities:lostcity dimension when Lost Cities, Distant Horizons, and C2ME are all loaded.",
                        "This keeps Lost Cities structures, Distant Horizons generation, and C2ME threaded settings active while avoiding shared Lost Cities generation-state races from DH/C2ME worker threads.",
                        "Disable only when diagnosing Lost Cities/Distant Horizons/C2ME compatibility behavior.")
                .define("serializeDhC2meFeaturePlacement", true);
        LOST_CITIES_CANCEL_STALE_DH_CLIENT_REQUESTS = builder
                .comment(
                        "Cancels stale Distant Horizons client full-data requests rejected during Lost Cities dimension switches.",
                        "Distant Horizons 2.4.5 can complete these rejected requests as failed results with a null Throwable, then log a NullPointerException in onWorldGenTaskComplete.",
                        "This does not disable DH generation; it drops only the already-rejected client request future.")
                .define("cancelStaleDhClientRequests", true);
        builder.pop();

        SPEC = builder.build();
    }

    private BtmFixesConfig() {
    }

    public static boolean dynamicTreesSeasonContextConcurrentMap() {
        return isLoaded("dynamictrees") && DYNAMIC_TREES_SEASON_CONTEXT_CONCURRENT_MAP.get();
    }

    public static boolean dynamicTreesUnearthedRegolithSoils() {
        return DYNAMIC_TREES_UNEARTHED_REGOLITH_SOILS.get() && isLoaded("dynamictrees") && isLoaded("unearthed");
    }

    public static boolean dynamicTreesDestroyUnsupportedTrees() {
        return DYNAMIC_TREES_DESTROY_UNSUPPORTED_TREES.get() && isLoaded("dynamictrees");
    }

    public static boolean dynamicTreesReconstructFallenLogs() {
        return DYNAMIC_TREES_RECONSTRUCT_FALLEN_LOGS.get() && isLoaded("dynamictrees");
    }

    public static boolean hyleSafeTertiarySelection() {
        return isLoaded("hyle") && HYLE_SAFE_TERTIARY_SELECTION.get();
    }

    public static boolean apotheosisSkipOffThreadTooltips() {
        return isLoaded("apotheosis") && APOTHEOSIS_SKIP_OFF_THREAD_TOOLTIPS.get();
    }

    public static boolean advancedLootInfoSkipOffThreadEmiRegistration() {
        return isLoaded("ali") && isLoaded("emi") && ADVANCED_LOOT_INFO_SKIP_OFF_THREAD_EMI_REGISTRATION.get();
    }

    public static boolean sgiRerunHyleAfterSurfaceConform() {
        return SGI_RERUN_HYLE_AFTER_SURFACE_CONFORM.get()
                && isLoaded("structure_generation_improver")
                && isLoaded("hyle")
                && isLoaded("unearthed");
    }

    public static boolean lostCitiesSerializeDhC2meFeaturePlacement() {
        return LOST_CITIES_SERIALIZE_DH_C2ME_FEATURE_PLACEMENT.get()
                && isLoaded("lostcities")
                && isLoaded("distanthorizons")
                && isLoaded("c2me");
    }

    public static boolean lostCitiesCancelStaleDhClientRequests() {
        return LOST_CITIES_CANCEL_STALE_DH_CLIENT_REQUESTS.get()
                && isLoaded("lostcities")
                && isLoaded("distanthorizons")
                && isLoaded("c2me");
    }

    private static boolean isLoaded(final String modId) {
        return ModList.get().isLoaded(modId);
    }
}
