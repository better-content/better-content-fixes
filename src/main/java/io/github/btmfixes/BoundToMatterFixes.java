package io.github.btmfixes;

import io.github.btmfixes.config.BtmFixesConfig;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(BoundToMatterFixes.MOD_ID)
public final class BoundToMatterFixes {
    public static final String MOD_ID = "btmfixes";

    public BoundToMatterFixes() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, BtmFixesConfig.SPEC);
    }
}
