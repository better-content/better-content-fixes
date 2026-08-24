package com.bettercontent.bettercontentfixes.compat.rehooked;

import com.bettercontent.bettercontentfixes.config.BcFixesConfig;
import com.oe.rehooked.data.HookData;
import com.oe.rehooked.entities.hook.HookEntity;
import com.oe.rehooked.handlers.hook.server.SPlayerHookHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.PartEntity;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class RehookedMobGrappling {
    public static final double MIN_EFFECTIVE_WEIGHT = 0.125D;
    public static final double MAX_EFFECTIVE_WEIGHT = 32.0D;
    private static final double RESISTANCE_WEIGHT_MULTIPLIER = 4.0D;
    private static final double SETTLED_DISTANCE = 0.2D;

    private RehookedMobGrappling() {
    }

    public static Optional<Mob> findMobHit(final HookEntity hook) {
        if (!BcFixesConfig.rehookedMobGrappling() || hook.level().isClientSide()) {
            return Optional.empty();
        }
        final HitResult hit = ProjectileUtil.getHitResultOnMoveVector(
                hook,
                entity -> mobFromHitEntity(entity).filter(RehookedMobGrappling::isEligibleTarget).isPresent());
        if (!(hit instanceof EntityHitResult entityHit)) {
            return Optional.empty();
        }
        return mobFromHitEntity(entityHit.getEntity()).filter(RehookedMobGrappling::isEligibleTarget);
    }

    public static boolean isEligibleTarget(final Mob mob) {
        return mob.isAlive() && !mob.isRemoved();
    }

    public static Vec3 attachmentPosition(final Entity entity) {
        return new Vec3(entity.getX(), entity.getBoundingBox().getCenter().y, entity.getZ());
    }

    public static double effectiveWeight(final LivingEntity entity) {
        return effectiveWeight(
                entity.getBbWidth(),
                entity.getBbHeight(),
                entity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
    }

    public static double effectiveWeight(
            final double width,
            final double height,
            final double knockbackResistance
    ) {
        final double volume = Math.max(0.0D, width) * Math.max(0.0D, width) * Math.max(0.0D, height);
        final double resistance = Math.max(0.0D, Math.min(1.0D, knockbackResistance));
        return Math.max(
                MIN_EFFECTIVE_WEIGHT,
                Math.min(MAX_EFFECTIVE_WEIGHT, volume * (1.0D + RESISTANCE_WEIGHT_MULTIPLIER * resistance)));
    }

    public static PullShares pullShares(final double playerWeight, final double mobWeight) {
        final double safePlayerWeight = Math.max(MIN_EFFECTIVE_WEIGHT, playerWeight);
        final double safeMobWeight = Math.max(MIN_EFFECTIVE_WEIGHT, mobWeight);
        final double total = safePlayerWeight + safeMobWeight;
        return new PullShares(safeMobWeight / total, safePlayerWeight / total);
    }

    public static void updateServerHandler(final SPlayerHookHandler handler) {
        if (!BcFixesConfig.rehookedMobGrappling()) {
            return;
        }
        handler.getOwner().ifPresent(owner -> handler.getHookData().ifPresent(data -> {
            final Map<Integer, Mob> uniqueTargets = new LinkedHashMap<>();
            double playerStrength = 0.0D;
            int pullingHookCount = 0;
            final double playerWeight = effectiveWeight(owner);

            for (HookEntity hook : handler.getHooks()) {
                if (hook.getState() != HookEntity.State.PULLING) {
                    continue;
                }
                pullingHookCount++;
                final RehookedMobTarget targetAccess = (RehookedMobTarget) hook;
                if (targetAccess.betterContent$getMobTargetId() < 0) {
                    playerStrength += 1.0D;
                    continue;
                }
                final Optional<Mob> target = targetAccess.betterContent$getMobTarget();
                if (target.isEmpty()) {
                    continue;
                }
                final Mob mob = target.get();
                uniqueTargets.putIfAbsent(mob.getId(), mob);
                playerStrength += pullShares(playerWeight, effectiveWeight(mob)).playerShare();
            }

            if (uniqueTargets.isEmpty() || pullingHookCount == 0) {
                return;
            }
            final Vec3 nativePlayerMotion = handler.getDeltaVThisTick();
            if (nativePlayerMotion != null) {
                handler.setDeltaVThisTick(nativePlayerMotion.scale(playerStrength / pullingHookCount));
            }
            uniqueTargets.values().forEach(mob -> pullMob(owner, mob, data, playerWeight));
        }));
    }

    private static void pullMob(
            final LivingEntity owner,
            final Mob mob,
            final HookData data,
            final double playerWeight
    ) {
        if (!isEligibleTarget(mob) || mob.level() != owner.level()) {
            return;
        }
        final Vec3 towardPlayer = attachmentPosition(owner).subtract(attachmentPosition(mob));
        if (towardPlayer.length() < SETTLED_DISTANCE) {
            return;
        }
        final Vec3 direction = towardPlayer.normalize();
        final double targetSpeed = data.pullSpeed() / 20.0D
                * pullShares(playerWeight, effectiveWeight(mob)).mobShare();
        final Vec3 currentMotion = mob.getDeltaMovement();
        final Vec3 perpendicularMotion = currentMotion.subtract(direction.scale(currentMotion.dot(direction)));
        mob.setDeltaMovement(perpendicularMotion.add(direction.scale(targetSpeed)));
        mob.hasImpulse = true;
        mob.hurtMarked = true;
    }

    private static Optional<Mob> mobFromHitEntity(final Entity entity) {
        if (entity instanceof Mob mob) {
            return Optional.of(mob);
        }
        if (entity instanceof PartEntity<?> part && part.getParent() instanceof Mob mob) {
            return Optional.of(mob);
        }
        return Optional.empty();
    }

    public record PullShares(double playerShare, double mobShare) {
    }
}
