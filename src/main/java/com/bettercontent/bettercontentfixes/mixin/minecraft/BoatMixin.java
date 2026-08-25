package com.bettercontent.bettercontentfixes.mixin.minecraft;

import com.bettercontent.bettercontentfixes.compat.VanillaBoatPolicy;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Boat.class)
public abstract class BoatMixin {
    @ModifyConstant(method = "hurt", constant = @Constant(floatValue = 40.0F))
    private float betterContentFixes$scaleVanillaDestructionThreshold(final float vanillaThreshold) {
        return VanillaBoatPolicy.destructionThreshold((Boat) (Object) this, vanillaThreshold);
    }

    /** Replaces the vessel item with partial materials while leaving vanilla destruction intact. */
    @Inject(method = "destroy", at = @At("HEAD"))
    private void betterContentFixes$dropDestructionComponents(
            final DamageSource source,
            final CallbackInfo callback) {
        final Boat boat = (Boat) (Object) this;
        for (final var component : VanillaBoatPolicy.destructionComponents(boat)) {
            boat.spawnAtLocation(component);
        }
    }

    @ModifyArg(
            method = "destroy",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/vehicle/Boat;spawnAtLocation(Lnet/minecraft/world/level/ItemLike;)Lnet/minecraft/world/entity/item/ItemEntity;"),
            index = 0,
            require = 1)
    private ItemLike betterContentFixes$suppressVanillaVesselDrop(final ItemLike vanillaDrop) {
        return VanillaBoatPolicy.suppressVesselDrop((Boat) (Object) this) ? Items.AIR : vanillaDrop;
    }
}
