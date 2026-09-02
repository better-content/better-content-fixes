package com.bettercontent.bettercontentfixes.gametest;

import com.bettercontent.bettercontentfixes.BetterContentFixes;
import com.bettercontent.bettercontentfixes.compat.DaylightProtectionPolicy;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@PrefixGameTestTemplate(false)
public final class DaylightProtectionGameTests {
    private DaylightProtectionGameTests() {
    }

    @GameTest(templateNamespace = BetterContentFixes.MOD_ID, template = "empty", timeoutTicks = 400)
    public static void zombiesDoNotIgniteUnderOpenSky(final GameTestHelper helper) {
        final Zombie zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(2, 2, 2));
        validateMobStaysOutOfSunBurn(helper, zombie, "zombie");
    }

    @GameTest(templateNamespace = BetterContentFixes.MOD_ID, template = "empty", timeoutTicks = 400)
    public static void skeletonsDoNotIgniteUnderOpenSky(final GameTestHelper helper) {
        final Skeleton skeleton = helper.spawnWithNoFreeWill(EntityType.SKELETON, new BlockPos(2, 2, 2));
        validateMobStaysOutOfSunBurn(helper, skeleton, "skeleton");
    }

    @GameTest(templateNamespace = BetterContentFixes.MOD_ID, template = "empty")
    public static void phantomsRetainVanillaDaylightBurning(final GameTestHelper helper) {
        final Phantom phantom = helper.spawnWithNoFreeWill(EntityType.PHANTOM, new BlockPos(2, 20, 2));
        helper.assertTrue(
                !DaylightProtectionPolicy.disablesSunBurnTick(phantom),
                "Phantoms must retain their vanilla daylight-burn check");
        helper.succeed();
    }

    @GameTest(templateNamespace = BetterContentFixes.MOD_ID, template = "empty", timeoutTicks = 200)
    public static void ordinaryFireStillIgnitesProtectedMobs(final GameTestHelper helper) {
        final Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(2, 2, 2));
        helper.getLevel().setDayTime(18000L);
        zombie.setSecondsOnFire(8);
        helper.runAfterDelay(20, () -> {
            if (!zombie.isOnFire()) {
                helper.fail("Expected ordinary non-solar fire to keep burning the protected zombie");
                return;
            }
            helper.succeed();
        });
    }

    private static void validateMobStaysOutOfSunBurn(final GameTestHelper helper, final Mob mob, final String label) {
        helper.getLevel().setDayTime(6000L);
        helper.getLevel().setWeatherParameters(0, 0, false, false);
        helper.runAfterDelay(200, () -> {
            if (mob.isOnFire()) {
                helper.fail("Expected " + label + " to remain unlit under daylight protection");
                return;
            }
            helper.succeed();
        });
    }
}
