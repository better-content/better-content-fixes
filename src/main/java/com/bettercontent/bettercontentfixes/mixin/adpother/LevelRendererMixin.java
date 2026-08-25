package com.bettercontent.bettercontentfixes.mixin.adpother;

import com.endertech.minecraft.mods.adpother.renderers.AcidRain;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    @Inject(
            method = "renderSnowAndRain",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/resources/ResourceLocation;)V",
                    shift = At.Shift.AFTER
            ),
            require = 2
    )
    private void betterContentFixes$selectAcidRainTexture(final CallbackInfo ci) {
        AcidRain.onVanillaTextureBinding();
    }
}
