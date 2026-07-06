package io.github.btmfixes.mixin.minecraft;

import io.github.btmfixes.config.BtmFixesConfig;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public final class MobMixin {
    @Inject(method = "isSunBurnTick", at = @At("HEAD"), cancellable = true)
    private void btmfixes$disableSunBurnTick(final CallbackInfoReturnable<Boolean> cir) {
        if (BtmFixesConfig.mobsDisableSunBurnTick()) {
            cir.setReturnValue(false);
        }
    }
}
