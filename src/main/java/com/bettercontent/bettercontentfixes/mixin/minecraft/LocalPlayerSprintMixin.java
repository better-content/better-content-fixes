package com.bettercontent.bettercontentfixes.mixin.minecraft;

import com.bettercontent.bettercontentfixes.config.BcFixesClientConfig;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerSprintMixin {
    @ModifyConstant(
            method = {"aiStep", "m_8119_"},
            constant = @Constant(intValue = 7, ordinal = 0),
            remap = false,
            require = 1)
    private int betterContentFixes$replaceForwardDoubleTapWindow(final int original) {
        return ModList.get().isLoaded("parcool") && BcFixesClientConfig.replaceForwardDoubleTapSprint()
                ? 0
                : original;
    }
}
