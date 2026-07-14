package io.github.bcfixes;

import com.llamalad7.mixinextras.MixinExtrasBootstrap;
import io.github.bcfixes.compat.BurntGrassPalette;
import io.github.bcfixes.compat.FarmlandTrampleProtection;
import io.github.bcfixes.compat.FluidMixBlocker;
import io.github.bcfixes.compat.DynamicTreesUnsupportedTreeFallover;
import io.github.bcfixes.compat.DynamicTreesUnearthedSoils;
import io.github.bcfixes.compat.DynamicTreesSupportSweepCommand;
import io.github.bcfixes.compat.RegolithFarmlandPalette;
import io.github.bcfixes.compat.RegolithFarmlandTilling;
import io.github.bcfixes.compat.RealisticHandsLootModifiers;
import io.github.bcfixes.config.BcFixesConfig;
import io.github.bcfixes.gametest.BurntGrassReplacementGameTests;
import io.github.bcfixes.gametest.DaylightProtectionGameTests;
import io.github.bcfixes.gametest.DynamicTreesUnsupportedTreeGameTests;
import io.github.bcfixes.gametest.FarmlandTrampleProtectionGameTests;
import io.github.bcfixes.gametest.FluidMixBlockerGameTests;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterGameTestsEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(BetterContentFixes.MOD_ID)
public final class BetterContentFixes {
    public static final String MOD_ID = "bcfixes";

    public BetterContentFixes() {
        MixinExtrasBootstrap.init();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, BcFixesConfig.SPEC);
        BurntGrassPalette.BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        BurntGrassPalette.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        RegolithFarmlandPalette.BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        RegolithFarmlandPalette.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        RealisticHandsLootModifiers.REGISTRY.register(FMLJavaModLoadingContext.get().getModEventBus());
        FMLJavaModLoadingContext.get().getModEventBus().addListener(DynamicTreesUnearthedSoils::onCommonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onRegisterGameTests);
        MinecraftForge.EVENT_BUS.register(FarmlandTrampleProtection.class);
        MinecraftForge.EVENT_BUS.register(FluidMixBlocker.class);
        MinecraftForge.EVENT_BUS.register(DynamicTreesUnsupportedTreeFallover.class);
        MinecraftForge.EVENT_BUS.register(DynamicTreesSupportSweepCommand.class);
        MinecraftForge.EVENT_BUS.register(RegolithFarmlandTilling.class);
    }

    private void onRegisterGameTests(final RegisterGameTestsEvent event) {
        event.register(BurntGrassReplacementGameTests.class);
        event.register(DaylightProtectionGameTests.class);
        event.register(DynamicTreesUnsupportedTreeGameTests.class);
        event.register(FarmlandTrampleProtectionGameTests.class);
        event.register(FluidMixBlockerGameTests.class);
    }
}
