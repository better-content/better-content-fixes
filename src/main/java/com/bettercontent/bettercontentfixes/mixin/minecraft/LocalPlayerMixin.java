package com.bettercontent.bettercontentfixes.mixin.minecraft;

import com.bettercontent.bettercontentfixes.config.BcFixesClientConfig;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {
    @Shadow
    private int sprintTriggerTime;

    @Inject(
            method = "aiStep",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/client/ForgeHooksClient;onMovementInputUpdate(Lnet/minecraft/client/player/LocalPlayer;Lnet/minecraft/client/player/Input;)V",
                    shift = At.Shift.AFTER))
    private void betterContentFixes$replaceForwardDoubleTapSprint(CallbackInfo ci) {
        if (ModList.get().isLoaded("combatroll") && BcFixesClientConfig.combatRollReplaceForwardDoubleTapSprint()) {
            sprintTriggerTime = 0;
        }
    }
}
