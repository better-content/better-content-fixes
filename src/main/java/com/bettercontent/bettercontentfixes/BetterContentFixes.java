package com.bettercontent.bettercontentfixes;

import com.llamalad7.mixinextras.MixinExtrasBootstrap;
import com.bettercontent.bettercontentfixes.compat.AmbientSurfaceSpawnControl;
import com.bettercontent.bettercontentfixes.compat.BurntGrassPalette;
import com.bettercontent.bettercontentfixes.compat.ButcherKnifeDurability;
import com.bettercontent.bettercontentfixes.compat.FarmlandTrampleProtection;
import com.bettercontent.bettercontentfixes.compat.FluidMixBlocker;
import com.bettercontent.bettercontentfixes.compat.DynamicTreesUnsupportedTreeFallover;
import com.bettercontent.bettercontentfixes.compat.DynamicTreesUnearthedSoils;
import com.bettercontent.bettercontentfixes.compat.DynamicTreesSupportSweepCommand;
import com.bettercontent.bettercontentfixes.compat.RegolithFarmlandPalette;
import com.bettercontent.bettercontentfixes.compat.RegolithFarmlandTilling;
import com.bettercontent.bettercontentfixes.config.BcFixesConfig;
import com.bettercontent.bettercontentfixes.gametest.AmbientSurfaceSpawnGameTests;
import com.bettercontent.bettercontentfixes.gametest.BurntGrassReplacementGameTests;
import com.bettercontent.bettercontentfixes.gametest.DaylightProtectionGameTests;
import com.bettercontent.bettercontentfixes.gametest.FarmlandTrampleProtectionGameTests;
import com.bettercontent.bettercontentfixes.gametest.FluidMixBlockerGameTests;
import com.bettercontent.bettercontentfixes.water.RainCollectorRegistry;
import com.bettercontent.bettercontentfixes.water.SnowMeltHandler;
import com.bettercontent.bettercontentfixes.water.WaterSurvivalGameTests;
import com.bettercontent.bettercontentfixes.water.WaterBottleCurio;
import com.bettercontent.bettercontentfixes.quest.QuestIntegration;
import com.bettercontent.bettercontentfixes.quest.QuestPredicateGameTests;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterGameTestsEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(BetterContentFixes.MOD_ID)
public final class BetterContentFixes {
    public static final String MOD_ID = "better_content_fixes";

    public BetterContentFixes() {
        MixinExtrasBootstrap.init();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, BcFixesConfig.SPEC);
        BurntGrassPalette.BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        BurntGrassPalette.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        RegolithFarmlandPalette.BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        RegolithFarmlandPalette.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        RainCollectorRegistry.BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        RainCollectorRegistry.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        FMLJavaModLoadingContext.get().getModEventBus().addListener(DynamicTreesUnearthedSoils::onCommonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onRegisterGameTests);
        MinecraftForge.EVENT_BUS.register(FarmlandTrampleProtection.class);
        MinecraftForge.EVENT_BUS.register(AmbientSurfaceSpawnControl.class);
        MinecraftForge.EVENT_BUS.register(FluidMixBlocker.class);
        MinecraftForge.EVENT_BUS.register(DynamicTreesUnsupportedTreeFallover.class);
        MinecraftForge.EVENT_BUS.register(DynamicTreesSupportSweepCommand.class);
        MinecraftForge.EVENT_BUS.register(RegolithFarmlandTilling.class);
        MinecraftForge.EVENT_BUS.register(ButcherKnifeDurability.class);
        MinecraftForge.EVENT_BUS.register(SnowMeltHandler.class);
        MinecraftForge.EVENT_BUS.register(WaterBottleCurio.class);
        WaterBottleCurio.registerPredicate();
        QuestIntegration.initialize();
    }

    private void onRegisterGameTests(final RegisterGameTestsEvent event) {
        event.register(BurntGrassReplacementGameTests.class);
        event.register(AmbientSurfaceSpawnGameTests.class);
        event.register(DaylightProtectionGameTests.class);
        event.register(FarmlandTrampleProtectionGameTests.class);
        event.register(FluidMixBlockerGameTests.class);
        event.register(QuestPredicateGameTests.class);
        event.register(WaterSurvivalGameTests.class);
    }
}
