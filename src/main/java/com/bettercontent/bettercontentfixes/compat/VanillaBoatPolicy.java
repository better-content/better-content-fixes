package com.bettercontent.bettercontentfixes.compat;

import com.bettercontent.bettercontentfixes.config.BcFixesConfig;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;

/** Pack policy deliberately limited to Minecraft's two vanilla boat entity types. */
public final class VanillaBoatPolicy {
    private VanillaBoatPolicy() {
    }

    public static boolean appliesTo(final Boat boat) {
        return appliesTo(boat.getType());
    }

    public static boolean appliesTo(final EntityType<?> type) {
        return type == EntityType.BOAT || type == EntityType.CHEST_BOAT;
    }

    public static float destructionThreshold(final Boat boat, final float vanillaThreshold) {
        if (!appliesTo(boat)) {
            return vanillaThreshold;
        }
        return (float) (vanillaThreshold * BcFixesConfig.vanillaBoatDurabilityMultiplier());
    }

    public static boolean suppressVesselDrop(final Boat boat) {
        return appliesTo(boat) && BcFixesConfig.vanillaBoatSuppressDestructionDrop();
    }
}
