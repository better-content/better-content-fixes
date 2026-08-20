package com.bettercontent.bettercontentfixes.gametest;

import com.bettercontent.bettercontentfixes.BetterContentFixes;
import com.bettercontent.bettercontentfixes.compat.RegolithFarmlandDefinitions;
import com.bettercontent.bettercontentfixes.compat.RegolithFarmlandPalette;
import com.bettercontent.bettercontentfixes.compat.SourceberryFarmlandCompat;
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
public final class SourceberryFarmlandGameTests {
    private static final ResourceLocation SOURCEBERRY_BUSH =
            new ResourceLocation("ars_nouveau", "sourceberry_bush");

    private SourceberryFarmlandGameTests() {
    }

    @GameTest(templateNamespace = BetterContentFixes.MOD_ID, template = "empty", timeoutTicks = 200)
    public static void sourceberrySustainsOnEveryRegolithFarmland(final GameTestHelper helper) {
        final Block sourceberry = sourceberry(helper);
        if (sourceberry == null) {
            return;
        }
        final BlockPos farmlandPos = helper.absolutePos(new BlockPos(2, 2, 2));
        final BlockPos plantPos = farmlandPos.above();

        for (RegolithFarmlandDefinitions.Entry entry : RegolithFarmlandPalette.entries()) {
            final BlockState farmland = RegolithFarmlandPalette.getFarmlandBlock(entry.farmlandId())
                    .orElseThrow(() -> new AssertionError("Missing regolith farmland block " + entry.farmlandId()))
                    .defaultBlockState();
            helper.getLevel().setBlockAndUpdate(farmlandPos, farmland);
            if (!farmland.is(SourceberryFarmlandCompat.COMMON_FARMLAND)) {
                helper.fail(entry.farmlandId() + " is missing from #c:farmland");
                return;
            }
            if (!sourceberry.defaultBlockState().canSurvive(helper.getLevel(), plantPos)) {
                helper.fail("Expected sourceberry to survive on " + entry.farmlandId());
                return;
            }
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = BetterContentFixes.MOD_ID, template = "empty", timeoutTicks = 200)
    public static void sourceberryRetainsVanillaSubstratesAndRejectsStone(final GameTestHelper helper) {
        final Block sourceberry = sourceberry(helper);
        if (sourceberry == null) {
            return;
        }
        final BlockPos farmlandPos = helper.absolutePos(new BlockPos(2, 2, 2));
        final BlockPos plantPos = farmlandPos.above();

        helper.getLevel().setBlockAndUpdate(farmlandPos, Blocks.FARMLAND.defaultBlockState());
        if (!sourceberry.defaultBlockState().canSurvive(helper.getLevel(), plantPos)) {
            helper.fail("Expected sourceberry to retain vanilla farmland support");
            return;
        }

        helper.getLevel().setBlockAndUpdate(farmlandPos, Blocks.STONE.defaultBlockState());
        if (sourceberry.defaultBlockState().canSurvive(helper.getLevel(), plantPos)) {
            helper.fail("Expected sourceberry to reject stone");
            return;
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = BetterContentFixes.MOD_ID, template = "empty", timeoutTicks = 200)
    public static void commonFarmlandExceptionIsSourceberryOnly(final GameTestHelper helper) {
        final RegolithFarmlandDefinitions.Entry entry = RegolithFarmlandPalette.entries().get(0);
        final BlockState farmland = RegolithFarmlandPalette.getFarmlandBlock(entry.farmlandId())
                .orElseThrow(() -> new AssertionError("Missing regolith farmland block " + entry.farmlandId()))
                .defaultBlockState();
        final BlockPos farmlandPos = helper.absolutePos(new BlockPos(2, 2, 2));
        helper.getLevel().setBlockAndUpdate(farmlandPos, farmland);

        if (Blocks.SWEET_BERRY_BUSH.defaultBlockState().canSurvive(helper.getLevel(), farmlandPos.above())) {
            helper.fail("Common farmland exception must not affect other bushes");
            return;
        }
        helper.succeed();
    }

    private static Block sourceberry(final GameTestHelper helper) {
        final Block sourceberry = ForgeRegistries.BLOCKS.getValue(SOURCEBERRY_BUSH);
        if (sourceberry == null || sourceberry == Blocks.AIR) {
            helper.fail("Missing required GameTest block " + SOURCEBERRY_BUSH);
            return null;
        }
        return sourceberry;
    }
}
