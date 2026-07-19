package io.github.bcfixes.water;

import io.github.bcfixes.BetterContentFixes;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@PrefixGameTestTemplate(false)
public final class WaterSurvivalGameTests {
    private WaterSurvivalGameTests() {
    }

    @GameTest(templateNamespace = BetterContentFixes.MOD_ID, template = "empty", timeoutTicks = 200)
    public static void exposedCollectorFillsOneChargePerPulse(final GameTestHelper helper) {
        final ServerLevel level = helper.getLevel();
        final BlockPos relativePos = new BlockPos(2, 200, 2);
        final BlockPos worldPos = helper.absolutePos(relativePos);
        level.setWeatherParameters(0, 1200, true, false);
        level.setRainLevel(1.0F);
        final BlockState state = RainCollectorRegistry.RAIN_COLLECTOR.get().defaultBlockState();
        helper.setBlock(relativePos, state);
        RainCollectorRegistry.RAIN_COLLECTOR.get().tick(state, level, worldPos, RandomSource.create(1L));
        helper.assertBlockProperty(relativePos, RainCollectorBlock.LEVEL, 1);
        helper.succeed();
    }

    @GameTest(templateNamespace = BetterContentFixes.MOD_ID, template = "empty", timeoutTicks = 200)
    public static void coveredCollectorDoesNotFill(final GameTestHelper helper) {
        final ServerLevel level = helper.getLevel();
        final BlockPos relativePos = new BlockPos(2, 200, 2);
        final BlockPos worldPos = helper.absolutePos(relativePos);
        level.setWeatherParameters(0, 1200, true, false);
        level.setRainLevel(1.0F);
        final BlockState state = RainCollectorRegistry.RAIN_COLLECTOR.get().defaultBlockState();
        helper.setBlock(relativePos, state);
        helper.setBlock(relativePos.above(2), Blocks.STONE);
        RainCollectorRegistry.RAIN_COLLECTOR.get().tick(state, level, worldPos, RandomSource.create(2L));
        helper.assertBlockProperty(relativePos, RainCollectorBlock.LEVEL, 0);
        helper.succeed();
    }

    @GameTest(templateNamespace = BetterContentFixes.MOD_ID, template = "empty", timeoutTicks = 180)
    public static void snowInCampfireCubeMeltsWithoutExtinguishing(final GameTestHelper helper) {
        final ServerLevel level = helper.getLevel();
        final BlockPos campfirePos = new BlockPos(3, 3, 3);
        final BlockPos lowerCorner = campfirePos.offset(-1, -1, -1);
        final BlockPos upperCorner = campfirePos.offset(1, 1, 1);
        final BlockPos outside = campfirePos.offset(2, 0, 0);
        helper.setBlock(lowerCorner, Blocks.SNOW_BLOCK);
        helper.setBlock(upperCorner, Blocks.SNOW_BLOCK);
        helper.setBlock(outside, Blocks.SNOW_BLOCK);
        helper.setBlock(campfirePos, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true));
        SnowMeltHandler.scheduleAroundCampfire(level, helper.absolutePos(campfirePos));
        helper.runAfterDelay(105, () -> {
            helper.assertBlockPresent(Blocks.WATER, lowerCorner);
            helper.assertBlockPresent(Blocks.WATER, upperCorner);
            helper.assertBlockPresent(Blocks.SNOW_BLOCK, outside);
            helper.assertBlockProperty(campfirePos, CampfireBlock.LIT, true);
            helper.succeed();
        });
    }
}
