package com.bettercontent.bettercontentfixes.mixin.ali;

import com.bettercontent.bettercontentfixes.config.BcFixesConfig;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.yanny.ali.emi.compatibility.EmiCompatibility", remap = false)
public abstract class EmiCompatibilityMixin {
    @Inject(method = "register", at = @At("HEAD"), cancellable = true, remap = false)
    private void better_content_fixes$skipOffThreadEmiRegistration(@Coerce final Object registry, final CallbackInfo ci) {
        if (!BcFixesConfig.advancedLootInfoSkipOffThreadEmiRegistration()) {
            return;
        }

        final Minecraft minecraft = Minecraft.getInstance();
        if (!minecraft.isSameThread()) {
            ci.cancel();
        }
    }
}
