package com.bettercontent.bettercontentfixes.gametest;

import com.bettercontent.bettercontentfixes.BetterContentFixes;
import com.bettercontent.bettercontentfixes.compat.DecorativeVegetationTrample;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@PrefixGameTestTemplate(false)
public final class DecorativeVegetationTrampleGameTests {
    private DecorativeVegetationTrampleGameTests() {
    }

    @GameTest(templateNamespace = BetterContentFixes.MOD_ID, template = "empty")
    public static void movingEntityTramplesOnlyDecorativeVegetation(final GameTestHelper helper) {
        final BlockPos grass = new BlockPos(2, 2, 2);
        final BlockPos crop = new BlockPos(4, 2, 2);
        helper.setBlock(grass, Blocks.TALL_GRASS.defaultBlockState());
        helper.setBlock(crop, Blocks.WHEAT.defaultBlockState());

        helper.assertTrue(
                DecorativeVegetationTrample.trampleIfMoving(helper.getLevel(), helper.absolutePos(grass), new Vec3(0.1D, 0.0D, 0.0D)),
                "Moving through tall grass must trample it");
        helper.assertBlockPresent(Blocks.AIR, grass);
        helper.assertTrue(
                !DecorativeVegetationTrample.trampleIfMoving(helper.getLevel(), helper.absolutePos(crop), new Vec3(0.1D, 0.0D, 0.0D)),
                "Crops must not be treated as decorative vegetation");
        helper.assertBlockPresent(Blocks.WHEAT, crop);
        helper.succeed();
    }

    @GameTest(templateNamespace = BetterContentFixes.MOD_ID, template = "empty")
    public static void stationaryEntityDoesNotTrampleVegetation(final GameTestHelper helper) {
        final BlockPos grass = new BlockPos(2, 2, 2);
        helper.setBlock(grass, Blocks.TALL_GRASS.defaultBlockState());
        helper.assertTrue(
                !DecorativeVegetationTrample.trampleIfMoving(helper.getLevel(), helper.absolutePos(grass), Vec3.ZERO),
                "Standing still must not trample decorative vegetation");
        helper.assertBlockPresent(Blocks.TALL_GRASS, grass);
        helper.succeed();
    }
}
