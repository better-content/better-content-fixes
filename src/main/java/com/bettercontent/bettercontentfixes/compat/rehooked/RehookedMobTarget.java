package com.bettercontent.bettercontentfixes.compat.rehooked;

import net.minecraft.world.entity.Mob;

import java.util.Optional;

public interface RehookedMobTarget {
    int betterContent$getMobTargetId();

    void betterContent$setMobTarget(Mob target);

    void betterContent$clearMobTarget();

    Optional<Mob> betterContent$getMobTarget();
}
