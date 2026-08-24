package com.bettercontent.bettercontentfixes.gametest;

import com.bettercontent.bettercontentfixes.BetterContentFixes;
import com.bettercontent.bettercontentfixes.compat.rehooked.RehookedMobGrappling;
import com.bettercontent.bettercontentfixes.compat.rehooked.RehookedMobTarget;
import com.oe.rehooked.entities.hook.HookEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@PrefixGameTestTemplate(false)
public final class RehookedMobGrapplingGameTests {
    private RehookedMobGrapplingGameTests() {
    }

    @GameTest(templateNamespace = BetterContentFixes.MOD_ID, template = "empty", timeoutTicks = 100)
    public static void mobHitBeforeBlockCreatesAttachmentWithoutDamage(final GameTestHelper helper) {
        final TestShot shot = createShot(helper, 4.5D, 7, false);
        final float healthBefore = shot.mob().getHealth();

        shot.hook().tick();

        final RehookedMobTarget target = (RehookedMobTarget) shot.hook();
        if (target.betterContent$getMobTargetId() != shot.mob().getId()) {
            helper.fail("Expected the nearer mob to become the synchronized grapple target");
            return;
        }
        if (shot.hook().getState() != HookEntity.State.PULLING) {
            helper.fail("Expected a mob hit to enter ReHooked's pulling state");
            return;
        }
        if (shot.mob().getHealth() != healthBefore) {
            helper.fail("Mob grappling must not deal impact damage");
            return;
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = BetterContentFixes.MOD_ID, template = "empty", timeoutTicks = 100)
    public static void blockHitBeforeMobPreservesNativeBlockGrapple(final GameTestHelper helper) {
        final TestShot shot = createShot(helper, 6.5D, 4, true);

        shot.hook().tick();

        final RehookedMobTarget target = (RehookedMobTarget) shot.hook();
        if (target.betterContent$getMobTargetId() >= 0) {
            helper.fail("A mob behind the first block collision must not replace the block grapple");
            return;
        }
        if (shot.hook().getState() != HookEntity.State.PULLING) {
            helper.fail("Expected the nearer block to retain native ReHooked pulling behavior");
            return;
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = BetterContentFixes.MOD_ID, template = "empty", timeoutTicks = 100)
    public static void removedTargetRetractsAndBossesRemainEligible(final GameTestHelper helper) {
        final TestShot shot = createShot(helper, 4.5D, 7, false);
        final WitherBoss boss = EntityType.WITHER.create(helper.getLevel());
        if (boss == null || !RehookedMobGrappling.isEligibleTarget(boss)) {
            helper.fail("Boss Mob subclasses must remain valid grapple targets");
            return;
        }

        shot.hook().tick();
        shot.mob().discard();
        shot.hook().tick();

        if (shot.hook().getState() != HookEntity.State.RETRACTING) {
            helper.fail("A removed grapple target must enter ReHooked's retracting state");
            return;
        }
        helper.succeed();
    }

    private static TestShot createShot(
            final GameTestHelper helper,
            final double mobRelativeX,
            final int wallRelativeX,
            final boolean solidWall
    ) {
        final ServerLevel level = helper.getLevel();
        final ServerPlayer owner = helper.makeMockServerPlayerInLevel();
        final Vec3 ownerPosition = Vec3.atCenterOf(helper.absolutePos(new BlockPos(1, 2, 1)));
        owner.setPos(ownerPosition.x, ownerPosition.y, ownerPosition.z);

        final Zombie mob = EntityType.ZOMBIE.create(level);
        if (mob == null) {
            throw new IllegalStateException("Unable to create zombie grapple target");
        }
        final BlockPos origin = helper.absolutePos(BlockPos.ZERO);
        mob.setPos(origin.getX() + mobRelativeX, origin.getY() + 2.0D, origin.getZ() + 1.5D);
        level.addFreshEntity(mob);

        if (solidWall || wallRelativeX > 0) {
            helper.setBlock(new BlockPos(wallRelativeX, 3, 1), Blocks.STONE);
        }

        final HookEntity hook = new HookEntity(owner);
        hook.setHookType("wood");
        hook.setPos(origin.getX() + 2.5D, origin.getY() + 3.0D, origin.getZ() + 1.5D);
        hook.setDeltaMovement(6.0D, 0.0D, 0.0D);
        level.addFreshEntity(hook);
        return new TestShot(hook, mob);
    }

    private record TestShot(HookEntity hook, Zombie mob) {
    }
}
