package io.github.btmfixes.gametest;

import io.github.btmfixes.BoundToMatterFixes;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@PrefixGameTestTemplate(false)
public final class DaylightProtectionGameTests {
    private DaylightProtectionGameTests() {
    }

    @GameTest(templateNamespace = BoundToMatterFixes.MOD_ID, template = "empty", timeoutTicks = 400)
    public static void zombiesDoNotIgniteUnderOpenSky(final GameTestHelper helper) {
        final Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(2, 2, 2));
        validateMobStaysOutOfSunBurn(helper, zombie, "zombie");
    }

    @GameTest(templateNamespace = BoundToMatterFixes.MOD_ID, template = "empty", timeoutTicks = 400)
    public static void skeletonsDoNotIgniteUnderOpenSky(final GameTestHelper helper) {
        final Skeleton skeleton = helper.spawnWithNoFreeWill(EntityType.SKELETON, new BlockPos(2, 2, 2));
        validateMobStaysOutOfSunBurn(helper, skeleton, "skeleton");
    }

    private static void validateMobStaysOutOfSunBurn(final GameTestHelper helper, final Mob mob, final String label) {
        helper.getLevel().setDayTime(6000L);
        helper.runAfterDelay(60, () -> {
            if (mob.isOnFire()) {
                helper.fail("Expected " + label + " to remain unlit under daylight protection");
                return;
            }
            helper.succeed();
        });
    }
}
