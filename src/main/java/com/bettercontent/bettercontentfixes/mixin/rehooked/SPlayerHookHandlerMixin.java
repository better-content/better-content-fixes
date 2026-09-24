package com.bettercontent.bettercontentfixes.mixin.rehooked;

import com.bettercontent.bettercontentfixes.compat.rehooked.RehookedMobGrappling;
import com.bettercontent.bettercontentfixes.compat.rehooked.RehookedIntroHookBehaviors;
import com.oe.rehooked.handlers.hook.server.SPlayerHookHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SPlayerHookHandler.class, remap = false)
public abstract class SPlayerHookHandlerMixin {
    @Unique
    private long betterContent$ratchetPullTicks;

    @Inject(method = "update", at = @At("RETURN"))
    private void betterContent$applyMobTug(final CallbackInfo ci) {
        final SPlayerHookHandler handler = (SPlayerHookHandler) (Object) this;
        RehookedMobGrappling.updateServerHandler(handler);
        RehookedIntroHookBehaviors.applyClimbingVineLoss(handler);
        RehookedIntroHookBehaviors.applyGaffBoarding(handler);
        RehookedIntroHookBehaviors.applySoapGuidance(handler);
        final boolean ratchetPulling = handler.countPulling() > 0
                && handler.getHookData().map(data -> "ratchet_reel".equals(data.type())).orElse(false);
        if (ratchetPulling) {
            RehookedIntroHookBehaviors.applyRatchetCadence(handler, betterContent$ratchetPullTicks++);
        } else {
            betterContent$ratchetPullTicks = 0L;
        }
    }
}
