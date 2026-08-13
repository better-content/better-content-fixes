package com.bettercontent.bettercontentfixes.gametest;

import com.bettercontent.bettercontentfixes.BetterContentFixes;
import com.bettercontent.bettercontentfixes.compat.FarmlandTrampleProtection;
import com.bettercontent.bettercontentfixes.compat.RegolithFarmlandDefinitions;
import com.bettercontent.bettercontentfixes.compat.RegolithFarmlandPalette;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@PrefixGameTestTemplate(false)
public final class FarmlandTrampleProtectionGameTests {
    private FarmlandTrampleProtectionGameTests() {
    }

    @GameTest(templateNamespace = BetterContentFixes.MOD_ID, template = "empty", timeoutTicks = 200)
    public static void farmlandTrampleIsCanceled(final GameTestHelper helper) {
        final BlockPos farmlandPos = helper.absolutePos(new BlockPos(2, 2, 2));
        helper.setBlock(farmlandPos, Blocks.FARMLAND.defaultBlockState());
        final FallingBlockEntity entity = new FallingBlockEntity(EntityType.FALLING_BLOCK, helper.getLevel());
        final BlockEvent.FarmlandTrampleEvent event = new BlockEvent.FarmlandTrampleEvent(
                helper.getLevel(),
                farmlandPos,
                Blocks.FARMLAND.defaultBlockState(),
                1.5f,
                entity
        );

        FarmlandTrampleProtection.onFarmlandTrample(event);
        if (!event.isCanceled()) {
            helper.fail("Expected farmland trample to be canceled");
            return;
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = BetterContentFixes.MOD_ID, template = "empty", timeoutTicks = 200)
    public static void regolithFarmlandSustainsVanillaCrops(final GameTestHelper helper) {
        final RegolithFarmlandDefinitions.Entry entry = RegolithFarmlandPalette.entries().get(0);
        final BlockState farmland = RegolithFarmlandPalette.getFarmlandBlock(entry.farmlandId())
                .orElseThrow(() -> new AssertionError("Missing regolith farmland block " + entry.farmlandId()))
                .defaultBlockState();
        final BlockPos farmlandPos = helper.absolutePos(new BlockPos(2, 2, 2));
        final IPlantable wheat = (IPlantable) Blocks.WHEAT;

        helper.getLevel().setBlockAndUpdate(farmlandPos, farmland);
        if (!farmland.canSustainPlant(helper.getLevel(), farmlandPos, Direction.UP, wheat)) {
            helper.fail("Expected " + entry.farmlandId() + " to sustain wheat");
            return;
        }
        helper.succeed();
    }
}
