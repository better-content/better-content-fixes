package com.bettercontent.bettercontentfixes.mixin.minecraft;

import com.bettercontent.bettercontentfixes.client.hud.DynamicHudRenderState;
import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = RenderSystem.class, remap = false)
public abstract class RenderSystemMixin {
    @ModifyVariable(method = "_setShaderColor", at = @At("HEAD"), argsOnly = true, ordinal = 3)
    private static float betterContentFixes$applyDynamicHudAlpha(final float requestedAlpha) {
        return DynamicHudRenderState.multiplyAlpha(requestedAlpha);
    }
}
