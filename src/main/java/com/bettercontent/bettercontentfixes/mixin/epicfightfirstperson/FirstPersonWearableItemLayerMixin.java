package com.bettercontent.bettercontentfixes.mixin.epicfightfirstperson;

import com.bettercontent.bettercontentfixes.client.FirstPersonLimbVisibility;
import com.bettercontent.bettercontentfixes.config.BcFixesClientConfig;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.kenji.first_person_compat.client.layers.FirstPersonWearableItemLayer;
import net.minecraft.world.entity.EquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import yesman.epicfight.api.client.model.SkinnedMesh;

@Mixin(value = FirstPersonWearableItemLayer.class, remap = false)
public abstract class FirstPersonWearableItemLayerMixin {
    @WrapOperation(
            method = "renderLayer(Lyesman/epicfight/world/capabilities/entitypatch/LivingEntityPatch;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I[Lyesman/epicfight/api/utils/math/OpenMatrix4f;FFFF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lyesman/epicfight/api/client/model/SkinnedMesh;initialize()V"),
            require = 1)
    private void betterContentFixes$hideFirstPersonArmorLimbs(
            final SkinnedMesh mesh,
            final Operation<Void> original,
            @Local final EquipmentSlot slot
    ) {
        original.call(mesh);
        if (BcFixesClientConfig.hideFirstPersonLimbs()) {
            FirstPersonLimbVisibility.hideArmorLimbs(mesh, slot);
        }
    }
}
