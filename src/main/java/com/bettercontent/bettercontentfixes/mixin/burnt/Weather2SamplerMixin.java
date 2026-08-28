package com.bettercontent.bettercontentfixes.mixin.burnt;

import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Prevents Burnt's Weather2 bridge from synchronously generating chunks around an empty server's spawn. */
@Mixin(targets = "net.pixelbank.burnt.integration.Weather2Sampler", remap = false)
public abstract class Weather2SamplerMixin {
    @Inject(method = "onLevelTick", at = @At("HEAD"), cancellable = true, remap = false)
    private static void better_content_fixes$skipSampleWithoutPlayers(
            final TickEvent.LevelTickEvent event,
            final CallbackInfo ci
    ) {
        if (event.level instanceof ServerLevel level && level.players().isEmpty()) {
            ci.cancel();
        }
    }
}
