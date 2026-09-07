package com.bettercontent.bettercontentfixes.mixin.sleepingoverhaul;

import com.bettercontent.bettercontentfixes.compat.sleeping.TimelapsePacer;
import com.bettercontent.bettercontentfixes.config.BcFixesConfig;
import github.cosmicdan.sleepingoverhaul.SleepingOverhaul;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
abstract class MinecraftServerMixin {
    @Inject(method = "tickServer", at = @At("TAIL"))
    private void betterContent$paceSleepingOverhaulTimelapse(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        final boolean active = BcFixesConfig.sleepingOverhaulPaceTimelapse()
                && SleepingOverhaul.serverState != null
                && SleepingOverhaul.serverState.isTimelapseActive();
        TimelapsePacer.pace(active, BcFixesConfig.sleepingOverhaulTargetTicksPerSecond());
    }
}
