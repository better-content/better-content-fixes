package com.bettercontent.bettercontentfixes.compat.rehooked;

import net.minecraft.world.entity.vehicle.Boat;

import java.util.Optional;

/** Synced boat anchor held by an Angler's Gaff hook entity. */
public interface RehookedBoatTarget {
    int betterContent$getBoatTargetId();

    void betterContent$setBoatTarget(Boat target);

    void betterContent$clearBoatTarget();

    Optional<Boat> betterContent$getBoatTarget();
}
