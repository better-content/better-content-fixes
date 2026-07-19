package io.github.bcfixes.mixin.pollution;

import io.github.bcfixes.config.BcFixesConfig;
import net.minecraftforge.event.level.BlockEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.endertech.minecraft.mods.adpother.events.PlayerEvents", remap = false)
public abstract class PlayerEventsMixin {
    @Inject(method = "onPlayerBreaksBlock", at = @At("HEAD"), cancellable = true, remap = false)
    private static void bcfixes$skipPlayerBreakPollution(final BlockEvent.BreakEvent event, final CallbackInfo ci) {
        if (BcFixesConfig.pollutionDisablePlayerBlockBreakEmissions()) {
            ci.cancel();
        }
    }
}
