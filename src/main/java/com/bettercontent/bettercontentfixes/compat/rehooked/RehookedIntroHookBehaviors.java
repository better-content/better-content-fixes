package com.bettercontent.bettercontentfixes.compat.rehooked;

import com.oe.rehooked.handlers.hook.server.SPlayerHookHandler;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.phys.Vec3;

/** Small server-side behavior adapters for Better Content's introductory hook variants. */
public final class RehookedIntroHookBehaviors {
    private RehookedIntroHookBehaviors() {
    }

    public static void applyRatchetCadence(final SPlayerHookHandler handler, final long activePullTick) {
        if (handler.countPulling() == 0
                || handler.getHookData().map(data -> !"ratchet_reel".equals(data.type())).orElse(true)) {
            return;
        }
        if (!IntroHookBehaviorPolicy.ratchetPulls(activePullTick)) {
            handler.setDeltaVThisTick(Vec3.ZERO);
        }
    }

    public static void applyClimbingVineLoss(final SPlayerHookHandler handler) {
        if (handler.countPulling() == 0
                || handler.getHookData().map(data -> !"climbing_vine".equals(data.type())).orElse(true)) {
            return;
        }
        final Vec3 pull = handler.getDeltaVThisTick();
        if (pull == null) {
            return;
        }
        final long pullingHooks = handler.getHooks().stream()
                .filter(hook -> hook.getState() == com.oe.rehooked.entities.hook.HookEntity.State.PULLING)
                .count();
        if (pullingHooks == 0L) {
            return;
        }
        final double factor = handler.getHooks().stream()
                .filter(hook -> hook.getState() == com.oe.rehooked.entities.hook.HookEntity.State.PULLING)
                .mapToDouble(hook -> hook.getHitPos()
                        .map(pos -> hook.level().getBlockState(pos).is(BlockTags.LEAVES))
                        .map(IntroHookBehaviorPolicy::climbingVinePullFactor)
                        .orElse(IntroHookBehaviorPolicy.VINE_NON_FOLIAGE_PULL_FACTOR))
                .average()
                .orElse(IntroHookBehaviorPolicy.VINE_NON_FOLIAGE_PULL_FACTOR);
        handler.setDeltaVThisTick(pull.scale(factor));
    }

    public static void applyGaffBoarding(final SPlayerHookHandler handler) {
        if (handler.countPulling() == 0
                || handler.getHookData().map(data -> !"anglers_gaff".equals(data.type())).orElse(true)) {
            return;
        }
        final Entity owner = handler.getOwner().orElse(null);
        if (!(owner instanceof Player player) || player.level().isClientSide()) {
            return;
        }
        for (var hook : handler.getHooks()) {
            if (hook.getState() != com.oe.rehooked.entities.hook.HookEntity.State.PULLING
                    || !(hook instanceof RehookedBoatTarget boatTarget)) {
                continue;
            }
            final Boat boat = boatTarget.betterContent$getBoatTarget().orElse(null);
            if (boat != null
                    && boat.level() == player.level()
                    && IntroHookBehaviorPolicy.shouldBoardBoat(player.distanceToSqr(boat))
                    && player.startRiding(boat)) {
                handler.removeAllHooks();
                return;
            }
        }
    }

    public static void applySoapGuidance(final SPlayerHookHandler handler) {
        if (handler.countPulling() == 0
                || handler.getHookData().map(data -> !"soap_on_a_rope".equals(data.type())).orElse(true)) {
            return;
        }
        final Vec3 pull = handler.getDeltaVThisTick();
        if (pull != null) {
            handler.setDeltaVThisTick(new Vec3(
                    pull.x,
                    IntroHookBehaviorPolicy.soapGuidedVerticalComponent(pull.y),
                    pull.z));
        }
    }
}
