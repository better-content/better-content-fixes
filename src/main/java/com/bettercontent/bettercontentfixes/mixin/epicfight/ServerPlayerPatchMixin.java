package com.bettercontent.bettercontentfixes.mixin.epicfight;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

@Mixin(value = ServerPlayerPatch.class, remap = false)
public abstract class ServerPlayerPatchMixin {
    @Inject(method = "toVanillaMode", at = @At("HEAD"), cancellable = true, require = 1)
    private void betterContentFixes$keepBattleMode(final boolean synchronize, final CallbackInfo ci) {
        ((ServerPlayerPatch) (Object) this).toEpicFightMode(synchronize);
        ci.cancel();
    }
}
