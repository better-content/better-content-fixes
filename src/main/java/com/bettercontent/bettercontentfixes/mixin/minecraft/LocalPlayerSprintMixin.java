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
public abstract class LocalPlayerSprintMixin {
    @Shadow
    private int sprintTriggerTime;

    @Inject(
            method = "aiStep",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/client/ForgeHooksClient;onMovementInputUpdate(Lnet/minecraft/client/player/LocalPlayer;Lnet/minecraft/client/player/Input;)V",
                    shift = At.Shift.AFTER),
            require = 1)
    private void betterContentFixes$replaceForwardDoubleTapSprint(final CallbackInfo ci) {
        if (ModList.get().isLoaded("parcool") && BcFixesClientConfig.replaceForwardDoubleTapSprint()) {
            sprintTriggerTime = 0;
        }
    }
}
