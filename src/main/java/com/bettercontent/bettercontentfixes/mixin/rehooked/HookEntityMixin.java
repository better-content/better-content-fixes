package com.bettercontent.bettercontentfixes.mixin.rehooked;

import com.bettercontent.bettercontentfixes.compat.rehooked.RehookedMobGrappling;
import com.bettercontent.bettercontentfixes.compat.rehooked.RehookedMobTarget;
import com.bettercontent.bettercontentfixes.compat.rehooked.RehookedBoatTarget;
import com.bettercontent.bettercontentfixes.compat.rehooked.IntroHookBehaviorPolicy;
import com.oe.rehooked.entities.hook.HookEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(value = HookEntity.class, remap = false)
public abstract class HookEntityMixin extends Projectile implements RehookedMobTarget, RehookedBoatTarget {
    @Unique
    private static final EntityDataAccessor<Integer> BETTER_CONTENT_MOB_TARGET =
            SynchedEntityData.defineId(HookEntity.class, EntityDataSerializers.INT);
    @Unique
    private static final EntityDataAccessor<Integer> BETTER_CONTENT_BOAT_TARGET =
            SynchedEntityData.defineId(HookEntity.class, EntityDataSerializers.INT);

    @Shadow
    public abstract void setState(HookEntity.State state);

    @Shadow
    public abstract void setReason(HookEntity.Reason reason);

    @Shadow
    protected abstract void setHitPos(BlockPos pos);

    @Shadow
    public abstract HookEntity.State getState();

    protected HookEntityMixin(final EntityType<? extends Projectile> type, final Level level) {
        super(type, level);
    }

    @Inject(method = "defineSynchedData", at = @At("RETURN"))
    private void betterContent$defineMobTarget(final CallbackInfo ci) {
        entityData.define(BETTER_CONTENT_MOB_TARGET, -1);
        entityData.define(BETTER_CONTENT_BOAT_TARGET, -1);
    }

    @Inject(method = "tickShot", at = @At("HEAD"), cancellable = true)
    private void betterContent$hitAnglersGaffBoat(final CallbackInfo ci) {
        final HookEntity hook = (HookEntity) (Object) this;
        if (hook.level().isClientSide() || !"anglers_gaff".equals(hook.getHookType())) {
            return;
        }
        final var hit = ProjectileUtil.getHitResultOnMoveVector(hook,
                entity -> entity instanceof Boat && entity != hook.getOwner());
        if (!(hit instanceof EntityHitResult entityHit) || !(entityHit.getEntity() instanceof Boat boat)) {
            return;
        }
        betterContent$setBoatTarget(boat);
        hook.setPos(boat.position());
        hook.setDeltaMovement(Vec3.ZERO);
        setHitPos(boat.blockPosition());
        setReason(HookEntity.Reason.HIT);
        setState(HookEntity.State.PULLING);
        ci.cancel();
    }

    @Inject(method = "tickShot", at = @At("HEAD"), cancellable = true)
    private void betterContent$hitMob(final CallbackInfo ci) {
        final HookEntity hook = (HookEntity) (Object) this;
        RehookedMobGrappling.findMobHit(hook).ifPresent(mob -> {
            betterContent$setMobTarget(mob);
            final Vec3 attachment = RehookedMobGrappling.attachmentPosition(mob);
            hook.setPos(attachment);
            hook.setDeltaMovement(Vec3.ZERO);
            setHitPos(mob.blockPosition());
            setReason(HookEntity.Reason.HIT);
            setState(HookEntity.State.PULLING);
            ci.cancel();
        });
    }

    @Inject(method = "tickShot", at = @At("RETURN"))
    private void betterContent$catchGrapnelBlockEdges(final CallbackInfo ci) {
        final HookEntity hook = (HookEntity) (Object) this;
        if (hook.level().isClientSide()
                || hook.getState() != HookEntity.State.SHOT
                || !"grapnel_bundle".equals(hook.getHookType())) {
            return;
        }
        final Vec3 shotEnd = hook.position().add(hook.getDeltaMovement());
        final BlockPos center = BlockPos.containing(shotEnd);
        BlockPos caughtPos = null;
        Vec3 caughtPoint = null;
        double closestDistanceSquared = Double.POSITIVE_INFINITY;
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    final BlockPos pos = center.offset(x, y, z);
                    final var shape = hook.level().getBlockState(pos).getCollisionShape(hook.level(), pos);
                    if (shape.isEmpty()) {
                        continue;
                    }
                    final AABB bounds = shape.bounds().move(pos);
                    final Vec3 nearest = new Vec3(
                            net.minecraft.util.Mth.clamp(shotEnd.x, bounds.minX, bounds.maxX),
                            net.minecraft.util.Mth.clamp(shotEnd.y, bounds.minY, bounds.maxY),
                            net.minecraft.util.Mth.clamp(shotEnd.z, bounds.minZ, bounds.maxZ));
                    final double distanceSquared = shotEnd.distanceToSqr(nearest);
                    if (IntroHookBehaviorPolicy.shouldForgivingCatch(distanceSquared)
                            && distanceSquared < closestDistanceSquared) {
                        closestDistanceSquared = distanceSquared;
                        caughtPos = pos;
                        caughtPoint = nearest;
                    }
                }
            }
        }
        if (caughtPos != null && caughtPoint != null) {
            hook.setPos(caughtPoint.x, caughtPoint.y, caughtPoint.z);
            hook.setDeltaMovement(Vec3.ZERO);
            setHitPos(caughtPos);
            setReason(HookEntity.Reason.HIT);
            setState(HookEntity.State.PULLING);
        }
    }

    @Inject(method = "tickPulling", at = @At("HEAD"), cancellable = true)
    private void betterContent$followMob(final CallbackInfo ci) {
        if (betterContent$getBoatTargetId() >= 0) {
            ci.cancel();
            final HookEntity hook = (HookEntity) (Object) this;
            final Boat boat = betterContent$getBoatTarget().orElse(null);
            if (boat == null || boat.isRemoved()) {
                if (!hook.level().isClientSide()) {
                    betterContent$clearBoatTarget();
                    setHitPos(null);
                    setReason(HookEntity.Reason.MISS);
                    setState(HookEntity.State.RETRACTING);
                }
                return;
            }
            hook.setPos(boat.position());
            hook.setDeltaMovement(Vec3.ZERO);
            if (!hook.level().isClientSide()) {
                setHitPos(boat.blockPosition());
            }
            return;
        }
        if (betterContent$getMobTargetId() < 0) {
            return;
        }
        ci.cancel();
        final HookEntity hook = (HookEntity) (Object) this;
        final Optional<Mob> target = betterContent$getMobTarget();
        if (target.isEmpty()) {
            if (!hook.level().isClientSide()) {
                betterContent$clearMobTarget();
                setHitPos(null);
                setReason(HookEntity.Reason.MISS);
                setState(HookEntity.State.RETRACTING);
            }
            return;
        }
        final Mob mob = target.get();
        if (!RehookedMobGrappling.isEligibleTarget(mob)) {
            if (!hook.level().isClientSide()) {
                betterContent$clearMobTarget();
                setHitPos(null);
                setReason(HookEntity.Reason.MISS);
                setState(HookEntity.State.RETRACTING);
            }
            return;
        }
        final Vec3 attachment = RehookedMobGrappling.attachmentPosition(mob);
        hook.setPos(attachment);
        hook.setDeltaMovement(Vec3.ZERO);
        if (!hook.level().isClientSide()) {
            setHitPos(mob.blockPosition());
        }
    }

    @Override
    public int betterContent$getMobTargetId() {
        if (!entityData.hasItem(BETTER_CONTENT_MOB_TARGET)) {
            return -1;
        }
        return entityData.get(BETTER_CONTENT_MOB_TARGET);
    }

    @Override
    public void betterContent$setMobTarget(final Mob target) {
        if (!entityData.hasItem(BETTER_CONTENT_MOB_TARGET)) {
            entityData.define(BETTER_CONTENT_MOB_TARGET, -1);
        }
        entityData.set(BETTER_CONTENT_MOB_TARGET, target.getId());
    }

    @Override
    public void betterContent$clearMobTarget() {
        if (entityData.hasItem(BETTER_CONTENT_MOB_TARGET)) {
            entityData.set(BETTER_CONTENT_MOB_TARGET, -1);
        }
    }

    @Override
    public Optional<Mob> betterContent$getMobTarget() {
        final Entity target = ((HookEntity) (Object) this).level().getEntity(betterContent$getMobTargetId());
        return target instanceof Mob mob ? Optional.of(mob) : Optional.empty();
    }

    @Override
    public int betterContent$getBoatTargetId() {
        if (!entityData.hasItem(BETTER_CONTENT_BOAT_TARGET)) {
            return -1;
        }
        return entityData.get(BETTER_CONTENT_BOAT_TARGET);
    }

    @Override
    public void betterContent$setBoatTarget(final Boat target) {
        if (!entityData.hasItem(BETTER_CONTENT_BOAT_TARGET)) {
            entityData.define(BETTER_CONTENT_BOAT_TARGET, -1);
        }
        entityData.set(BETTER_CONTENT_BOAT_TARGET, target.getId());
    }

    @Override
    public void betterContent$clearBoatTarget() {
        if (entityData.hasItem(BETTER_CONTENT_BOAT_TARGET)) {
            entityData.set(BETTER_CONTENT_BOAT_TARGET, -1);
        }
    }

    @Override
    public Optional<Boat> betterContent$getBoatTarget() {
        final Entity target = ((HookEntity) (Object) this).level().getEntity(betterContent$getBoatTargetId());
        return target instanceof Boat boat ? Optional.of(boat) : Optional.empty();
    }
}
