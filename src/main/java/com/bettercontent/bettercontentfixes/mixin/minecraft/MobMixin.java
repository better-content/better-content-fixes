package com.bettercontent.bettercontentfixes.mixin.minecraft;

import com.bettercontent.bettercontentfixes.config.BcFixesConfig;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Phantom;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public final class MobMixin {
    @Inject(
            method = {"isSunBurnTick", "m_21527_"},
            at = @At("HEAD"),
            cancellable = true,
            require = 1,
            remap = false)
    private void better_content_fixes$disableSunBurnTick(final CallbackInfoReturnable<Boolean> cir) {
        if (BcFixesConfig.mobsDisableSunBurnTick() && !((Object) this instanceof Phantom)) {
            cir.setReturnValue(false);
        }
    }
}
