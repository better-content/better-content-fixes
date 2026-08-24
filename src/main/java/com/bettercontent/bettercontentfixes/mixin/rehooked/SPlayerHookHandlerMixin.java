package com.bettercontent.bettercontentfixes.mixin.rehooked;

import com.bettercontent.bettercontentfixes.compat.rehooked.RehookedMobGrappling;
import com.oe.rehooked.handlers.hook.server.SPlayerHookHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SPlayerHookHandler.class, remap = false)
public abstract class SPlayerHookHandlerMixin {
    @Inject(method = "update", at = @At("RETURN"))
    private void betterContent$applyMobTug(final CallbackInfo ci) {
        RehookedMobGrappling.updateServerHandler((SPlayerHookHandler) (Object) this);
    }
}
