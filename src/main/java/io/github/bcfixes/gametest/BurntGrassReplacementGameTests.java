package io.github.bcfixes.gametest;

import io.github.bcfixes.BetterContentFixes;
import io.github.bcfixes.compat.BurntGrassPalette;
import io.github.bcfixes.compat.BurntGrassReplacementDefinitions;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

@PrefixGameTestTemplate(false)
public final class BurntGrassReplacementGameTests {
    private BurntGrassReplacementGameTests() {
    }

    @GameTest(templateNamespace = BetterContentFixes.MOD_ID, template = "empty", timeoutTicks = 1200)
    public static void everyCoveredSourceBlockResolvesToItsExpectedTarget(final GameTestHelper helper) {
        final int width = 8;
        int index = 0;
        int validated = 0;
        for (BurntGrassReplacementDefinitions.Entry entry : BurntGrassPalette.entries()) {
            Block sourceBlock = lookupBlock(entry.sourceId());
            Block expectedBlock = lookupBlock(entry.targetId());
            if (sourceBlock == null || expectedBlock == null) {
                continue;
            }
            BlockPos pos = helper.absolutePos(new BlockPos(2 + (index % width) * 2, 2, 2 + (index / width) * 2));

            helper.getLevel().setBlockAndUpdate(pos, sourceBlock.defaultBlockState());
            BlockState actual = BurntGrassPalette.resolveReplacementState(helper.getLevel().getBlockState(pos))
                    .orElseThrow(() -> new AssertionError("No replacement state for " + entry.sourceId()));
            if (actual.getBlock() != expectedBlock) {
                helper.fail("Expected " + entry.targetId() + " for " + entry.sourceId()
                        + " but resolved " + ForgeRegistries.BLOCKS.getKey(actual.getBlock()));
                return;
            }
            helper.getLevel().setBlockAndUpdate(pos, actual);
            if (helper.getLevel().getBlockState(pos).getBlock() != expectedBlock) {
                helper.fail("Resolved replacement for " + entry.sourceId() + " could not be placed");
                return;
            }
            index++;
            validated++;
        }
        if (validated == 0) {
            helper.fail("No replacement entries could be validated in the standalone game-test runtime");
            return;
        }
        helper.succeed();
    }

    private static Block lookupBlock(final ResourceLocation id) {
        Block block = ForgeRegistries.BLOCKS.getValue(id);
        if (block == null || block == Blocks.AIR) {
            return null;
        }
        return block;
    }
}
