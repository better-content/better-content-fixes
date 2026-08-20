package com.bettercontent.bettercontentfixes.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModList;

public final class BcFixesConfig {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue DYNAMIC_TREES_SEASON_CONTEXT_CONCURRENT_MAP;
    public static final ForgeConfigSpec.BooleanValue DYNAMIC_TREES_UNEARTHED_REGOLITH_SOILS;
    public static final ForgeConfigSpec.BooleanValue DYNAMIC_TREES_DESTROY_UNSUPPORTED_TREES;
    public static final ForgeConfigSpec.BooleanValue HYLE_SAFE_TERTIARY_SELECTION;
    public static final ForgeConfigSpec.BooleanValue HYLE_COMPLETE_BOTTOM_SECTION;
    public static final ForgeConfigSpec.BooleanValue HYLE_RUN_AFTER_UNDERGROUND_DECORATION;
    public static final ForgeConfigSpec.BooleanValue APOTHEOSIS_SKIP_OFF_THREAD_TOOLTIPS;
    public static final ForgeConfigSpec.BooleanValue ADVANCED_LOOT_INFO_SKIP_OFF_THREAD_EMI_REGISTRATION;
    public static final ForgeConfigSpec.BooleanValue SGI_RERUN_HYLE_AFTER_SURFACE_CONFORM;
    public static final ForgeConfigSpec.BooleanValue LOST_CITIES_SERIALIZE_DH_C2ME_FEATURE_PLACEMENT;
    public static final ForgeConfigSpec.BooleanValue LOST_CITIES_CANCEL_STALE_DH_CLIENT_REQUESTS;
    public static final ForgeConfigSpec.BooleanValue BURNT_MODDED_GRASS_REPLACEMENTS;
    public static final ForgeConfigSpec.BooleanValue FLUID_MIXING_BLOCK_GENERATED_BLOCKS;
    public static final ForgeConfigSpec.BooleanValue FARMLAND_PREVENT_TRAMPLE;
    public static final ForgeConfigSpec.BooleanValue MOBS_DISABLE_SUN_BURN_TICK;
    public static final ForgeConfigSpec.BooleanValue MOBS_BLOCK_NATURAL_SURFACE_HOSTILES;
    public static final ForgeConfigSpec.IntValue MOBS_NATURAL_SURFACE_DEPTH;
    public static final ForgeConfigSpec.BooleanValue POLLUTION_DISABLE_PLAYER_BLOCK_BREAK_EMISSIONS;
    public static final ForgeConfigSpec.DoubleValue VANILLA_BOAT_DURABILITY_MULTIPLIER;
    public static final ForgeConfigSpec.BooleanValue VANILLA_BOAT_SUPPRESS_DESTRUCTION_DROP;

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
                        "Opt-in administrative repair for unsupported Dynamic Trees only.",
                        "Disabled by default because its chunk-load scan can alter an already-generated world.",
                        "Enable only after a verified backup and an operator-approved maintenance window.")
                .define("destroyUnsupportedTrees", false);
        builder.pop();

        builder.push("pollution");
        POLLUTION_DISABLE_PLAYER_BLOCK_BREAK_EMISSIONS = builder
                .comment(
                        "Cancels Pollution of the Realms emissions from its player block-break event only.",
                        "World-level block-break emissions remain available to Create drills, contraptions, explosions, and other automation.")
                .define("disablePlayerBlockBreakEmissions", true);
        builder.pop();

        builder.push("vanillaBoats");
        VANILLA_BOAT_DURABILITY_MULTIPLIER = builder
                .comment(
                        "Multiplier applied to the accumulated-damage destruction threshold of vanilla boats and chest boats.",
                        "Movement, collisions, passengers, damage accumulation, and modded vessel entity types are unchanged.")
                .defineInRange("durabilityMultiplier", 10.0D, 1.0D, 100.0D);
        VANILLA_BOAT_SUPPRESS_DESTRUCTION_DROP = builder
                .comment(
                        "Suppresses the boat or chest-boat item when a vanilla vessel is destroyed.",
                        "Chest-boat inventory contents still drop normally; unrelated entity drops are unchanged.")
                .define("suppressDestructionDrop", true);
        builder.pop();

        builder.push("hyle");
        HYLE_SAFE_TERTIARY_SELECTION = builder
                .comment(
                        "Guards Hyle tertiary stone selection against invalid indices.",
                        "Prevents worldgen crashes when Hyle's tertiary selector requests an index outside the available tertiary list size.",
                        "Keeps Unearthed/Hyle stone replacement enabled while avoiding ArrayIndexOutOfBoundsException in RoughNoiseSampler.selectTertiary.")
                .define("safeTertiarySelection", true);
        HYLE_COMPLETE_BOTTOM_SECTION = builder
                .comment(
                        "Completes Hyle stone replacement in the Overworld's lowest chunk section.",
                        "Hyle's bottom interpolation slice can resolve to its no-replacement sentinel, leaving deepslate and tuff below Y -48 untouched.",
                        "The repair uses the nearest valid generated Hyle stratum for each column, so regional Unearthed geology remains continuous.")
                .define("completeBottomSection", true);
        HYLE_RUN_AFTER_UNDERGROUND_DECORATION = builder
                .comment(
                        "Moves Hyle's stone replacement pass to the end of underground decoration.",
                        "This replaces stone-family blocks emitted after local modifications by underground structures, ores, and decoration features.",
                        "Above-ground authored boulders and structure masonry remain outside this underground geology pass.")
                .define("runAfterUndergroundDecoration", true);
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

        builder.push("burnt");
        BURNT_MODDED_GRASS_REPLACEMENTS = builder
                .comment(
                        "Replaces Burnt's generic burnt grass result with pack-specific burnt variants for modded grass-like blocks.",
                        "Uses native Burnt outputs when they exist andbetter_content_fixes-owned burnt palette blocks otherwise.",
                        "Disable only when diagnosing Burnt grass spread behavior.")
                .define("moddedGrassReplacements", true);
        builder.pop();

        builder.push("fluidMixing");
        FLUID_MIXING_BLOCK_GENERATED_BLOCKS = builder
                .comment(
                        "Reverts fluid-generated block placements unless the produced block is explicitly allowlisted.",
                        "This preserves the current pack policy from Cobble-Gen-Haters while moving ownership intobetter_content_fixes.",
                        "The default allowlist is data-driven through thebetter_content_fixes:allowed_fluid_generated_blocks block tag.")
                .define("blockGeneratedBlocks", true);
        builder.pop();

        builder.push("farmland");
        FARMLAND_PREVENT_TRAMPLE = builder
                .comment(
                        "Cancels farmland trampling from fall impacts.",
                        "Prevents players and mobs from degrading cultivated soil back into dirt when landing on it.",
                        "Disable only if a future system intentionally wants fall-impact farmland loss back.")
                .define("preventTrample", true);
        builder.pop();

        builder.push("mobs");
        MOBS_DISABLE_SUN_BURN_TICK = builder
                .comment(
                        "Forces Mob.isSunBurnTick() to return false.",
                        "This preserves the current pack policy from Protect Mobs From Daylight while moving ownership intobetter_content_fixes.",
                        "Applies anywhere vanilla or another mod uses Mob.isSunBurnTick for daylight burning checks.")
                .define("disableSunBurnTick", true);
        MOBS_BLOCK_NATURAL_SURFACE_HOSTILES = builder
                .comment(
                        "Denies natural and chunk-generation monsters near the Overworld terrain surface and on tagged grass-covered ground at any depth.",
                        "The surface is measured with the leaf-ignoring motion-blocking heightmap, so tree canopies do not create ambient spawn pockets.",
                        "The depth-independent ground list is data-driven through the better_content_fixes:ambient_spawn_denied_surfaces block tag.",
                        "Spawner, structure, event, summon, command, and scripted entity insertion remain unaffected.")
                .define("blockNaturalSurfaceHostiles", true);
        MOBS_NATURAL_SURFACE_DEPTH = builder
                .comment(
                        "Number of blocks below the local leaf-ignoring terrain surface that remain reserved from ambient monster spawning.",
                        "A value of 6 denies the surface block and the six-block band beneath it while leaving deeper caves active.")
                .defineInRange("naturalSurfaceDepth", 6, 0, 64);
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

    private BcFixesConfig() {
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

    public static boolean hyleSafeTertiarySelection() {
        return isLoaded("hyle") && HYLE_SAFE_TERTIARY_SELECTION.get();
    }

    public static boolean hyleCompleteBottomSection() {
        return isLoaded("hyle") && HYLE_COMPLETE_BOTTOM_SECTION.get();
    }

    public static boolean hyleRunAfterUndergroundDecoration() {
        return isLoaded("hyle") && HYLE_RUN_AFTER_UNDERGROUND_DECORATION.get();
    }

    public static boolean apotheosisSkipOffThreadTooltips() {
        return isLoaded("apotheosis") && APOTHEOSIS_SKIP_OFF_THREAD_TOOLTIPS.get();
    }

    public static boolean advancedLootInfoSkipOffThreadEmiRegistration() {
        return isLoaded("ali") && isLoaded("emi") && ADVANCED_LOOT_INFO_SKIP_OFF_THREAD_EMI_REGISTRATION.get();
    }

    public static boolean burntModdedGrassReplacements() {
        return isLoaded("burnt") && BURNT_MODDED_GRASS_REPLACEMENTS.get();
    }

    public static boolean fluidMixingBlockGeneratedBlocks() {
        return FLUID_MIXING_BLOCK_GENERATED_BLOCKS.get();
    }

    public static boolean farmlandPreventTrample() {
        return FARMLAND_PREVENT_TRAMPLE.get();
    }

    public static boolean mobsDisableSunBurnTick() {
        return MOBS_DISABLE_SUN_BURN_TICK.get();
    }

    public static boolean mobsBlockNaturalSurfaceHostiles() {
        return MOBS_BLOCK_NATURAL_SURFACE_HOSTILES.get();
    }

    public static int mobsNaturalSurfaceDepth() {
        return MOBS_NATURAL_SURFACE_DEPTH.get();
    }

    public static boolean pollutionDisablePlayerBlockBreakEmissions() {
        return isLoaded("adpother") && POLLUTION_DISABLE_PLAYER_BLOCK_BREAK_EMISSIONS.get();
    }

    public static double vanillaBoatDurabilityMultiplier() {
        return VANILLA_BOAT_DURABILITY_MULTIPLIER.get();
    }

    public static boolean vanillaBoatSuppressDestructionDrop() {
        return VANILLA_BOAT_SUPPRESS_DESTRUCTION_DROP.get();
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
