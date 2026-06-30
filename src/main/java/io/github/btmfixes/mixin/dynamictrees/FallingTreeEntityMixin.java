package io.github.btmfixes.mixin.dynamictrees;

import io.github.btmfixes.compat.DynamicTreesFallenTreeReconstruction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.ferreusveritas.dynamictrees.entity.FallingTreeEntity", remap = false)
public abstract class FallingTreeEntityMixin {
    @Inject(method = "dropPayLoad", at = @At("HEAD"), remap = false)
    private void btmfixes$reconstructFallenTreeAsBlocks(final CallbackInfo ci) {
        DynamicTreesFallenTreeReconstruction.reconstructAtLanding(this);
    }

    @Inject(method = "standardDropLeavesPayLoad", at = @At("HEAD"), cancellable = true, remap = false)
    private static void btmfixes$suppressEarlyLeafDrops(@Coerce final Object fallingTreeEntity, final CallbackInfo ci) {
        if (DynamicTreesFallenTreeReconstruction.suppressEarlyLeafDrops(fallingTreeEntity)) {
            ci.cancel();
        }
    }
}
