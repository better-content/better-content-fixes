package com.bettercontent.bettercontentfixes.mixin.create;

import com.bettercontent.bettercontentfixes.compat.WaterWheelBiomePolicy;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Applies to both standard and large wheels through Create's shared flow-score implementation. */
@Mixin(targets = "com.simibubi.create.content.kinetics.waterwheel.WaterWheelBlockEntity", remap = false)
public abstract class WaterWheelBlockEntityMixin {
    @Shadow @Final protected BlockPos worldPosition;
    @Shadow protected Level level;

    @Shadow
    public abstract void setFlowScoreAndUpdate(int score);

    @Inject(method = "determineAndApplyFlowScore", at = @At("HEAD"), cancellable = true)
    private void betterContentFixes$denyGenerationOutsideRivers(final CallbackInfo callback) {
        if (level != null && !WaterWheelBiomePolicy.allowsGeneration(level, worldPosition)) {
            setFlowScoreAndUpdate(0);
            callback.cancel();
        }
    }
}
