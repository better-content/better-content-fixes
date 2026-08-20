package com.bettercontent.bettercontentfixes.mixin.minecraft;

import com.bettercontent.bettercontentfixes.compat.VanillaBoatPolicy;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.vehicle.Boat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Boat.class)
public abstract class BoatMixin {
    @ModifyConstant(method = "hurt", constant = @Constant(floatValue = 40.0F))
    private float betterContentFixes$scaleVanillaDestructionThreshold(final float vanillaThreshold) {
        return VanillaBoatPolicy.destructionThreshold((Boat) (Object) this, vanillaThreshold);
    }

    /** Cancels only the vessel item; ChestBoat still runs its own inventory-drop path. */
    @Inject(method = "destroy", at = @At("HEAD"), cancellable = true)
    private void betterContentFixes$suppressVanillaVesselDrop(
            final DamageSource source,
            final CallbackInfo callback) {
        if (VanillaBoatPolicy.suppressVesselDrop((Boat) (Object) this)) {
            callback.cancel();
        }
    }
}
