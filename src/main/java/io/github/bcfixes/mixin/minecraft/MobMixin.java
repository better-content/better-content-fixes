package io.github.bcfixes.mixin.minecraft;

import io.github.bcfixes.config.BcFixesConfig;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public final class MobMixin {
    @Inject(method = "isSunBurnTick", at = @At("HEAD"), cancellable = true)
    private void bcfixes$disableSunBurnTick(final CallbackInfoReturnable<Boolean> cir) {
        if (BcFixesConfig.mobsDisableSunBurnTick()) {
            cir.setReturnValue(false);
        }
    }
}
