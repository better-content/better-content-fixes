package com.bettercontent.bettercontentfixes.mixin.epicfightfirstperson;

import com.bettercontent.bettercontentfixes.client.FirstPersonLimbVisibility;
import com.bettercontent.bettercontentfixes.config.BcFixesClientConfig;
import net.kenji.first_person_compat.client.render.FirstPersonBodyRenderer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.client.mesh.HumanoidMesh;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;

@Mixin(value = FirstPersonBodyRenderer.class, remap = false)
public abstract class FirstPersonBodyRendererMixin {
    @Inject(
            method = "prepareModel(Lyesman/epicfight/client/mesh/HumanoidMesh;Lnet/minecraft/client/player/LocalPlayer;Lyesman/epicfight/client/world/capabilites/entitypatch/player/LocalPlayerPatch;Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;)V",
            at = @At("TAIL"),
            require = 1)
    private void betterContentFixes$hideFirstPersonPlayerLimbs(
            final HumanoidMesh mesh,
            final LocalPlayer player,
            final LocalPlayerPatch playerPatch,
            final LivingEntityRenderer<LocalPlayer, PlayerModel<LocalPlayer>> renderer,
            final CallbackInfo ci
    ) {
        if (BcFixesClientConfig.hideFirstPersonLimbs()) {
            FirstPersonLimbVisibility.hidePlayerLimbs(mesh);
        }
    }
}
