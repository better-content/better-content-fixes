package io.github.btmfixes;

import com.llamalad7.mixinextras.MixinExtrasBootstrap;
import io.github.btmfixes.compat.BurntGrassPalette;
import io.github.btmfixes.compat.FarmlandTrampleProtection;
import io.github.btmfixes.compat.FluidMixBlocker;
import io.github.btmfixes.compat.DynamicTreesUnsupportedTreeFallover;
import io.github.btmfixes.compat.DynamicTreesUnearthedSoils;
import io.github.btmfixes.compat.DynamicTreesSupportSweepCommand;
import io.github.btmfixes.compat.RegolithFarmlandPalette;
import io.github.btmfixes.compat.RegolithFarmlandTilling;
import io.github.btmfixes.compat.RealisticHandsLootModifiers;
import io.github.btmfixes.config.BtmFixesConfig;
import io.github.btmfixes.gametest.BurntGrassReplacementGameTests;
import io.github.btmfixes.gametest.DaylightProtectionGameTests;
import io.github.btmfixes.gametest.DynamicTreesUnsupportedTreeGameTests;
import io.github.btmfixes.gametest.FarmlandTrampleProtectionGameTests;
import io.github.btmfixes.gametest.FluidMixBlockerGameTests;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterGameTestsEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(BoundToMatterFixes.MOD_ID)
public final class BoundToMatterFixes {
    public static final String MOD_ID = "btmfixes";

    public BoundToMatterFixes() {
        MixinExtrasBootstrap.init();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, BtmFixesConfig.SPEC);
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
