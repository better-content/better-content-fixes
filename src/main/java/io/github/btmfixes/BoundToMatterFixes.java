package io.github.btmfixes;

import com.llamalad7.mixinextras.MixinExtrasBootstrap;
import io.github.btmfixes.compat.DynamicTreesUnearthedSoils;
import io.github.btmfixes.config.BtmFixesConfig;
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
    }
}
