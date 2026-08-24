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
    public static final ForgeConfigSpec.BooleanValue AMBIENT_SOUNDS_RETRY_REJECTED_STREAMS;
    public static final ForgeConfigSpec.BooleanValue EXPLOSION_OVERHAUL_CLAMP_CONCUSSION_DURATION;
    public static final ForgeConfigSpec.IntValue EXPLOSION_OVERHAUL_MAX_CONCUSSION_DURATION_SECONDS;
    public static final ForgeConfigSpec.BooleanValue WANDERING_TRADER_RECURRING_VISITS;
    public static final ForgeConfigSpec.IntValue WANDERING_TRADER_INITIAL_DELAY;
    public static final ForgeConfigSpec.IntValue WANDERING_TRADER_VISIT_INTERVAL;
    public static final ForgeConfigSpec.IntValue WANDERING_TRADER_RETRY_DELAY;
    public static final ForgeConfigSpec.BooleanValue WANDERING_TRADER_ANNOUNCE_ARRIVAL;
    public static final ForgeConfigSpec.BooleanValue SGI_RERUN_HYLE_AFTER_SURFACE_CONFORM;
    public static final ForgeConfigSpec.BooleanValue LOST_CITIES_SERIALIZE_DH_C2ME_FEATURE_PLACEMENT;
    public static final ForgeConfigSpec.BooleanValue LOST_CITIES_CANCEL_STALE_DH_CLIENT_REQUESTS;
    public static final ForgeConfigSpec.BooleanValue THE_FLESH_THAT_HATES_DISABLE_PROXIMITY_MUSIC;
    public static final ForgeConfigSpec.BooleanValue WEATHER2_DISABLE_FOG_OVERRIDE_WITH_SHADERS;
    public static final ForgeConfigSpec.BooleanValue SOPHISTICATED_STORAGE_BARREL_HOPPER_EXTRACTION;
    public static final ForgeConfigSpec.BooleanValue BURNT_MODDED_GRASS_REPLACEMENTS;
    public static final ForgeConfigSpec.BooleanValue FLUID_MIXING_BLOCK_GENERATED_BLOCKS;
    public static final ForgeConfigSpec.BooleanValue FARMLAND_PREVENT_TRAMPLE;
    public static final ForgeConfigSpec.BooleanValue MOBS_DISABLE_SUN_BURN_TICK;
    public static final ForgeConfigSpec.BooleanValue MOBS_BLOCK_NATURAL_SURFACE_HOSTILES;
    public static final ForgeConfigSpec.IntValue MOBS_NATURAL_SURFACE_DEPTH;
    public static final ForgeConfigSpec.BooleanValue MOBS_VERTICAL_NATURAL_SPAWN_SCALING;
    public static final ForgeConfigSpec.IntValue MOBS_VERTICAL_SPAWN_UPPER_MINIMUM_RANGE;
    public static final ForgeConfigSpec.IntValue MOBS_VERTICAL_SPAWN_MAX_MULTIPLIER;
    public static final ForgeConfigSpec.BooleanValue MOBS_BLOCK_VANILLA_ZOMBIES_AND_SKELETONS;
    public static final ForgeConfigSpec.BooleanValue POLLUTION_DISABLE_PLAYER_BLOCK_BREAK_EMISSIONS;
    public static final ForgeConfigSpec.DoubleValue VANILLA_BOAT_DURABILITY_MULTIPLIER;
    public static final ForgeConfigSpec.BooleanValue VANILLA_BOAT_SUPPRESS_DESTRUCTION_DROP;
    public static final ForgeConfigSpec.BooleanValue REHOOKED_MOB_GRAPPLING;

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

        builder.push("ambientSounds");
        AMBIENT_SOUNDS_RETRY_REJECTED_STREAMS = builder
                .comment(
                        "Retires AmbientSounds streams that Minecraft rejected before playback so their parent sounds can retry later.",
                        "Minecraft has a finite sound-channel pool; without this recovery, a rejected stream can remain tracked forever and permanently silence that ambient sound.",
                        "Active streams and streams that have played at least once retain AmbientSounds' normal lifecycle.")
                .define("retryRejectedStreams", true);
        builder.pop();

        builder.push("explosionOverhaul");
        EXPLOSION_OVERHAUL_CLAMP_CONCUSSION_DURATION = builder
                .comment(
                        "Caps Explosion Overhaul concussion effects so large clustered blasts cannot cause excessively long shell shock.",
                        "The cap applies to blur, camera sway, low-pass audio, and deafness, including duration accumulated by repeated blasts.")
                .define("clampConcussionDuration", true);
        EXPLOSION_OVERHAUL_MAX_CONCUSSION_DURATION_SECONDS = builder
                .comment("Maximum concussion hold duration in seconds. Default: 45 seconds.")
                .defineInRange("maxConcussionDurationSeconds", 45, 1, 100);
        builder.pop();

        builder.push("wanderingTrader");
        WANDERING_TRADER_RECURRING_VISITS = builder
                .comment(
                        "Replaces the vanilla random wandering-trader chance with one shared recurring world visitor.",
                        "Vanilla meeting-point placement, biome exclusions, llamas, gamerules, and despawning remain in use.")
                .define("recurringVisits", true);
        WANDERING_TRADER_INITIAL_DELAY = builder
                .comment("Ticks from first enabled world load to the first visit. Default: two Minecraft days.")
                .defineInRange("initialDelay", 48_000, 0, Integer.MAX_VALUE);
        WANDERING_TRADER_VISIT_INTERVAL = builder
                .comment("Ticks between successful visits. Default: five Minecraft days.")
                .defineInRange("visitInterval", 120_000, 1_200, Integer.MAX_VALUE);
        WANDERING_TRADER_RETRY_DELAY = builder
                .comment("Ticks before retrying a due visit when vanilla cannot find a valid spawn position.")
                .defineInRange("retryDelay", 1_200, 20, 24_000);
        WANDERING_TRADER_ANNOUNCE_ARRIVAL = builder
                .comment("Broadcasts the themed trader's dimension and exact block coordinates after a scheduled arrival.")
                .define("announceArrival", true);
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
                        "Forces Mob.isSunBurnTick() to return false for protected mobs.",
                        "Phantoms and Phantom subclasses retain vanilla daylight burning so exposed phantoms are cleared after sunrise.",
                        "Other daylight-sensitive mobs remain protected; ordinary fire and non-solar fire damage are unchanged.")
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
        MOBS_VERTICAL_NATURAL_SPAWN_SCALING = builder
                .comment(
                        "Scales Overworld natural monster spawning from the candidate spawn block's Y coordinate.",
                        "The controller runs extra vanilla natural-spawn passes and never uses player Y, spawners, structures, events, summons, commands, or chunk-generation spawning.")
                .define("verticalNaturalSpawnScaling", true);
        MOBS_VERTICAL_SPAWN_UPPER_MINIMUM_RANGE = builder
                .comment(
                        "Blocks above sea level that retain the minimum natural-monster spawn multiplier.",
                        "With the default 128, the minimum band extends from the Overworld sea level through Y 191.")
                .defineInRange("verticalSpawnUpperMinimumRange", 128, 0, 512);
        MOBS_VERTICAL_SPAWN_MAX_MULTIPLIER = builder
                .comment(
                        "Maximum Overworld natural-monster spawn multiplier at the lower and upper build limits.",
                        "The minimum multiplier is always 1; the default 8 runs eight natural-spawn passes with a Y-derived acceptance chance.")
                .defineInRange("verticalSpawnMaxMultiplier", 8, 1, 16);
        MOBS_BLOCK_VANILLA_ZOMBIES_AND_SKELETONS = builder
                .comment(
                        "Denies only natural and chunk-generation spawning of minecraft:zombie and minecraft:skeleton in the Overworld.",
                        "Spawner, structure, event, summon, command, scripted insertion, and variant entity types remain available.")
                .define("blockVanillaZombiesAndSkeletons", true);
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

        builder.push("theFleshThatHates");
        THE_FLESH_THAT_HATES_DISABLE_PROXIMITY_MUSIC = builder
                .comment(
                        "Disables The Flesh That Hates' automatic proximity music near clusters of flesh blocks.",
                        "The upstream handler pauses vanilla music and routes its horror score through the Jukebox/Note Blocks channel.",
                        "Entity, combat, evolution, jukebox, note-block, and other Records-channel sounds remain unchanged.")
                .define("disableProximityMusic", true);
        builder.pop();

        builder.push("weather2");
        WEATHER2_DISABLE_FOG_OVERRIDE_WITH_SHADERS = builder
                .comment(
                        "Disables Weather2's custom fog color and distance override while an Oculus shader pack is active.",
                        "Weather2 still tracks storm state and renders weather particles; the active shader pack owns sky and fog rendering.",
                        "Shaders-off Weather2 fog behavior is unchanged.")
                .define("disableFogOverrideWithShaders", true);
        builder.pop();

        builder.push("sophisticatedStorage");
        SOPHISTICATED_STORAGE_BARREL_HOPPER_EXTRACTION = builder
                .comment(
                        "Allows vanilla hoppers below Sophisticated Storage barrels and limited barrels to extract items.",
                        "Extraction uses the barrel's input/output inventory handler, preserving storage filters and slot rules.",
                        "Other Sophisticated Storage blocks and hopper insertion behavior are unchanged.")
                .define("barrelHopperExtraction", true);
        builder.pop();

        builder.push("rehooked");
        REHOOKED_MOB_GRAPPLING = builder
                .comment(
                        "Allows ReHooked projectiles to attach to mobs, including bosses and modded Mob subclasses.",
                        "A mob hit creates a weight-based tug that moves both the player and mob without dealing impact damage.",
                        "Players, vehicles, and non-Mob living entities remain invalid grapple targets.")
                .define("mobGrappling", true);
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

    public static boolean ambientSoundsRetryRejectedStreams() {
        return isLoaded("ambientsounds") && AMBIENT_SOUNDS_RETRY_REJECTED_STREAMS.get();
    }

    public static boolean explosionOverhaulClampConcussionDuration() {
        return isLoaded("explosionoverhaul") && EXPLOSION_OVERHAUL_CLAMP_CONCUSSION_DURATION.get();
    }

    public static int explosionOverhaulMaxConcussionDurationSeconds() {
        return EXPLOSION_OVERHAUL_MAX_CONCUSSION_DURATION_SECONDS.get();
    }

    public static boolean wanderingTraderRecurringVisits() {
        return WANDERING_TRADER_RECURRING_VISITS.get();
    }

    public static int wanderingTraderInitialDelay() {
        return WANDERING_TRADER_INITIAL_DELAY.get();
    }

    public static int wanderingTraderVisitInterval() {
        return WANDERING_TRADER_VISIT_INTERVAL.get();
    }

    public static int wanderingTraderRetryDelay() {
        return WANDERING_TRADER_RETRY_DELAY.get();
    }

    public static boolean wanderingTraderAnnounceArrival() {
        return WANDERING_TRADER_ANNOUNCE_ARRIVAL.get();
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

    public static boolean mobsVerticalNaturalSpawnScaling() {
        return MOBS_VERTICAL_NATURAL_SPAWN_SCALING.get();
    }

    public static int mobsVerticalSpawnUpperMinimumRange() {
        return MOBS_VERTICAL_SPAWN_UPPER_MINIMUM_RANGE.get();
    }

    public static int mobsVerticalSpawnMaxMultiplier() {
        return MOBS_VERTICAL_SPAWN_MAX_MULTIPLIER.get();
    }

    public static boolean mobsBlockVanillaZombiesAndSkeletons() {
        return MOBS_BLOCK_VANILLA_ZOMBIES_AND_SKELETONS.get();
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

    public static boolean rehookedMobGrappling() {
        return isLoaded("rehooked") && REHOOKED_MOB_GRAPPLING.get();
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

    public static boolean theFleshThatHatesDisableProximityMusic() {
        return THE_FLESH_THAT_HATES_DISABLE_PROXIMITY_MUSIC.get()
                && isLoaded("the_flesh_that_hates");
    }

    public static boolean weather2DisableFogOverrideWithShaders() {
        return WEATHER2_DISABLE_FOG_OVERRIDE_WITH_SHADERS.get()
                && isLoaded("weather2")
                && isLoaded("oculus");
    }

    public static boolean sophisticatedStorageBarrelHopperExtraction() {
        return SOPHISTICATED_STORAGE_BARREL_HOPPER_EXTRACTION.get()
                && isLoaded("sophisticatedstorage");
    }

    private static boolean isLoaded(final String modId) {
        return ModList.get().isLoaded(modId);
    }
}
