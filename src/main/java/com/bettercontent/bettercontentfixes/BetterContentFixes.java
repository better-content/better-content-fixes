package com.bettercontent.bettercontentfixes;

import com.llamalad7.mixinextras.MixinExtrasBootstrap;
import com.bettercontent.bettercontentfixes.compat.AmbientSurfaceSpawnControl;
import com.bettercontent.bettercontentfixes.compat.BurntGrassPalette;
import com.bettercontent.bettercontentfixes.compat.ButcherKnifeDurability;
import com.bettercontent.bettercontentfixes.compat.FarmlandTrampleProtection;
import com.bettercontent.bettercontentfixes.compat.FluidMixBlocker;
import com.bettercontent.bettercontentfixes.compat.DynamicTreesUnsupportedTreeFallover;
import com.bettercontent.bettercontentfixes.compat.DecorativeVegetationTrample;
import com.bettercontent.bettercontentfixes.compat.DynamicTreesUnearthedSoils;
import com.bettercontent.bettercontentfixes.compat.DynamicTreesSupportSweepCommand;
import com.bettercontent.bettercontentfixes.compat.RegolithFarmlandPalette;
import com.bettercontent.bettercontentfixes.compat.RegolithFarmlandTilling;
import com.bettercontent.bettercontentfixes.compat.VoidWormSpawnRemoval;
import com.bettercontent.bettercontentfixes.compat.ThirstLootModifierCompat;
import com.bettercontent.bettercontentfixes.config.BcFixesConfig;
import com.bettercontent.bettercontentfixes.config.BcFixesClientConfig;
import com.bettercontent.bettercontentfixes.gametest.AmbientSurfaceSpawnGameTests;
import com.bettercontent.bettercontentfixes.gametest.BurntGrassReplacementGameTests;
import com.bettercontent.bettercontentfixes.gametest.DaylightProtectionGameTests;
import com.bettercontent.bettercontentfixes.gametest.DecorativeVegetationTrampleGameTests;
import com.bettercontent.bettercontentfixes.gametest.FarmlandTrampleProtectionGameTests;
import com.bettercontent.bettercontentfixes.gametest.FluidMixBlockerGameTests;
import com.bettercontent.bettercontentfixes.gametest.RehookedMobGrapplingGameTests;
import com.bettercontent.bettercontentfixes.gametest.SophisticatedBarrelHopperGameTests;
import com.bettercontent.bettercontentfixes.gametest.SourceberryFarmlandGameTests;
import com.bettercontent.bettercontentfixes.gametest.VanillaBoatGameTests;
import com.bettercontent.bettercontentfixes.gametest.WaterWheelBiomePolicyGameTests;
import com.bettercontent.bettercontentfixes.gametest.OptionalIntegrationGameTests;
import com.bettercontent.bettercontentfixes.water.RainCollectorRegistry;
import com.bettercontent.bettercontentfixes.water.SnowMeltHandler;
import com.bettercontent.bettercontentfixes.water.WaterSurvivalGameTests;
import com.bettercontent.bettercontentfixes.water.WaterBottleCurio;
import com.bettercontent.bettercontentfixes.curios.CoinPurseCurio;
import com.bettercontent.bettercontentfixes.quest.QuestIntegration;
import com.bettercontent.bettercontentfixes.quest.QuestPredicateGameTests;
import com.bettercontent.bettercontentfixes.trader.WanderingTraderGameTests;
import com.bettercontent.bettercontentfixes.trader.WanderingTraderVisits;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterGameTestsEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(BetterContentFixes.MOD_ID)
public final class BetterContentFixes {
    public static final String MOD_ID = "better_content_fixes";

    public BetterContentFixes() {
        MixinExtrasBootstrap.init();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, BcFixesConfig.SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, BcFixesClientConfig.SPEC);
        final var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        if (ModList.get().isLoaded("thirst")) {
            ThirstLootModifierCompat.register(modEventBus);
        }
        VoidWormSpawnRemoval.SERIALIZERS.register(modEventBus);
        BurntGrassPalette.BLOCKS.register(modEventBus);
        BurntGrassPalette.ITEMS.register(modEventBus);
        RegolithFarmlandPalette.BLOCKS.register(modEventBus);
        RegolithFarmlandPalette.ITEMS.register(modEventBus);
        RainCollectorRegistry.BLOCKS.register(modEventBus);
        RainCollectorRegistry.ITEMS.register(modEventBus);
        modEventBus.addListener(DynamicTreesUnearthedSoils::onCommonSetup);
        modEventBus.addListener(this::onRegisterGameTests);
        MinecraftForge.EVENT_BUS.register(FarmlandTrampleProtection.class);
        MinecraftForge.EVENT_BUS.register(AmbientSurfaceSpawnControl.class);
        MinecraftForge.EVENT_BUS.register(FluidMixBlocker.class);
        MinecraftForge.EVENT_BUS.register(DynamicTreesUnsupportedTreeFallover.class);
        MinecraftForge.EVENT_BUS.register(DecorativeVegetationTrample.class);
        MinecraftForge.EVENT_BUS.register(DynamicTreesSupportSweepCommand.class);
        MinecraftForge.EVENT_BUS.register(RegolithFarmlandTilling.class);
        MinecraftForge.EVENT_BUS.register(ButcherKnifeDurability.class);
        MinecraftForge.EVENT_BUS.register(SnowMeltHandler.class);
        MinecraftForge.EVENT_BUS.register(WaterBottleCurio.class);
        MinecraftForge.EVENT_BUS.register(WanderingTraderVisits.class);
        WaterBottleCurio.registerPredicate();
        CoinPurseCurio.registerPredicate();
        QuestIntegration.initialize();
    }

    private void onRegisterGameTests(final RegisterGameTestsEvent event) {
        event.register(BurntGrassReplacementGameTests.class);
        event.register(AmbientSurfaceSpawnGameTests.class);
        event.register(DaylightProtectionGameTests.class);
        event.register(DecorativeVegetationTrampleGameTests.class);
        event.register(FarmlandTrampleProtectionGameTests.class);
        event.register(FluidMixBlockerGameTests.class);
        event.register(SourceberryFarmlandGameTests.class);
        event.register(VanillaBoatGameTests.class);
        event.register(WaterWheelBiomePolicyGameTests.class);
        event.register(QuestPredicateGameTests.class);
        event.register(WaterSurvivalGameTests.class);
        event.register(WanderingTraderGameTests.class);
        event.register(OptionalIntegrationGameTests.class);
        if (ModList.get().isLoaded("sophisticatedstorage")) {
            event.register(SophisticatedBarrelHopperGameTests.class);
        }
        if (ModList.get().isLoaded("rehooked")) {
            event.register(RehookedMobGrapplingGameTests.class);
        }
    }
}
