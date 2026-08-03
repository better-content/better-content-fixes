package io.github.bcfixes.mixin.create;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Keeps the first Create workshop genuinely useful without granting passive power. */
@Mixin(targets = "com.simibubi.create.content.kinetics.crank.HandCrankBlock", remap = false)
public abstract class HandCrankBlockMixin {
    @Inject(method = "getRotationSpeed", at = @At("HEAD"), cancellable = true)
    private void bcfixes$useWorkshopSpeed(CallbackInfoReturnable<Integer> callback) {
        callback.setReturnValue(64);
    }
}
