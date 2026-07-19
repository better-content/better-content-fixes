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
        final BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        level.setWeatherParameters(0, 1200, true, false);
        final BlockState state = RainCollectorRegistry.RAIN_COLLECTOR.get().defaultBlockState();
        helper.setBlock(pos, state);
        RainCollectorRegistry.RAIN_COLLECTOR.get().tick(state, level, pos, RandomSource.create(1L));
        helper.assertBlockProperty(pos, RainCollectorBlock.LEVEL, 1);
        helper.succeed();
    }

    @GameTest(templateNamespace = BetterContentFixes.MOD_ID, template = "empty", timeoutTicks = 200)
    public static void coveredCollectorDoesNotFill(final GameTestHelper helper) {
        final ServerLevel level = helper.getLevel();
        final BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        level.setWeatherParameters(0, 1200, true, false);
        final BlockState state = RainCollectorRegistry.RAIN_COLLECTOR.get().defaultBlockState();
        helper.setBlock(pos, state);
        helper.setBlock(pos.above(2), Blocks.STONE);
        RainCollectorRegistry.RAIN_COLLECTOR.get().tick(state, level, pos, RandomSource.create(2L));
        helper.assertBlockProperty(pos, RainCollectorBlock.LEVEL, 0);
        helper.succeed();
    }

    @GameTest(templateNamespace = BetterContentFixes.MOD_ID, template = "empty", timeoutTicks = 180)
    public static void snowBelowCampfireMeltsWithoutExtinguishing(final GameTestHelper helper) {
        final ServerLevel level = helper.getLevel();
        final BlockPos snowPos = helper.absolutePos(new BlockPos(2, 2, 2));
        helper.setBlock(snowPos, Blocks.SNOW_BLOCK);
        helper.setBlock(snowPos.above(), Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true));
        SnowMeltHandler.scheduleCandidate(level, snowPos);
        helper.runAfterDelay(105, () -> {
            helper.assertBlockPresent(Blocks.WATER, snowPos);
            helper.assertBlockProperty(snowPos.above(), CampfireBlock.LIT, true);
            helper.succeed();
        });
    }
}
