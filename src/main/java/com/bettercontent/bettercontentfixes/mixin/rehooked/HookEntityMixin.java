package com.bettercontent.bettercontentfixes.mixin.rehooked;

import com.bettercontent.bettercontentfixes.compat.rehooked.RehookedMobGrappling;
import com.bettercontent.bettercontentfixes.compat.rehooked.RehookedMobTarget;
import com.oe.rehooked.entities.hook.HookEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(value = HookEntity.class, remap = false)
public abstract class HookEntityMixin extends Projectile implements RehookedMobTarget {
    @Unique
    private static final EntityDataAccessor<Integer> BETTER_CONTENT_MOB_TARGET =
            SynchedEntityData.defineId(HookEntity.class, EntityDataSerializers.INT);

    @Shadow
    public abstract void setState(HookEntity.State state);

    @Shadow
    public abstract void setReason(HookEntity.Reason reason);

    @Shadow
    protected abstract void setHitPos(BlockPos pos);

    protected HookEntityMixin(final EntityType<? extends Projectile> type, final Level level) {
        super(type, level);
    }

    @Inject(method = "defineSynchedData", at = @At("RETURN"))
    private void betterContent$defineMobTarget(final CallbackInfo ci) {
        entityData.define(BETTER_CONTENT_MOB_TARGET, -1);
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

    @Inject(method = "tickPulling", at = @At("HEAD"), cancellable = true)
    private void betterContent$followMob(final CallbackInfo ci) {
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
}
