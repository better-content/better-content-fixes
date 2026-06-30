package io.github.btmfixes;

import com.llamalad7.mixinextras.MixinExtrasBootstrap;
import io.github.btmfixes.compat.DynamicTreesFallenTreeSweepCommand;
import io.github.btmfixes.compat.DynamicTreesUnsupportedTreeFallover;
import io.github.btmfixes.compat.DynamicTreesUnearthedSoils;
import io.github.btmfixes.compat.DynamicTreesSupportSweepCommand;
import io.github.btmfixes.config.BtmFixesConfig;
import io.github.btmfixes.gametest.DynamicTreesUnsupportedTreeGameTests;
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
        FMLJavaModLoadingContext.get().getModEventBus().addListener(DynamicTreesUnearthedSoils::onCommonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onRegisterGameTests);
        MinecraftForge.EVENT_BUS.register(DynamicTreesUnsupportedTreeFallover.class);
        MinecraftForge.EVENT_BUS.register(DynamicTreesSupportSweepCommand.class);
        MinecraftForge.EVENT_BUS.register(DynamicTreesFallenTreeSweepCommand.class);
    }

    private void onRegisterGameTests(final RegisterGameTestsEvent event) {
        event.register(DynamicTreesUnsupportedTreeGameTests.class);
    }
}
