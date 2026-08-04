package io.github.bcfixes.prestige;

import io.github.bcfixes.BetterContentFixes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@PrefixGameTestTemplate(false)
public final class WorldCondenserGameTests {
    private WorldCondenserGameTests() {}

    @GameTest(templateNamespace = BetterContentFixes.MOD_ID, template = "empty", timeoutTicks = 200)
    public static void formationAndAttunementAreValidated(final GameTestHelper helper) {
        BlockPos center = helper.absolutePos(new BlockPos(2, 2, 2));
        BlockPos interfacePos = center.relative(Direction.NORTH);
        BlockState interfaceState = PrestigeRegistry.WORLD_CONDENSER_INTERFACE.get().defaultBlockState()
                .setValue(WorldCondenserInterfaceBlock.FACING, Direction.NORTH);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos cursor = center.offset(dx, dy, dz);
                    if (cursor.equals(center)) helper.setBlock(cursor, Blocks.AIR);
                    else if (cursor.equals(interfacePos)) helper.setBlock(cursor, interfaceState);
                    else helper.setBlock(cursor, PrestigeRegistry.WORLD_CONDENSER_HULL.get());
                }
            }
        }
        if (!WorldCondenserFormation.isFormed(helper.getLevel(), interfacePos, interfaceState)) {
            helper.fail("Expected a hollow 3x3x3 shell with one face interface to form");
            return;
        }
        if (!(helper.getLevel().getBlockEntity(interfacePos) instanceof WorldCondenserBlockEntity entity)) {
            helper.fail("World Condenser interface did not create its block entity");
            return;
        }
        entity.attune();
        CompoundTag saved = entity.saveWithFullMetadata();
        WorldCondenserBlockEntity restored = new WorldCondenserBlockEntity(interfacePos, interfaceState);
        restored.load(saved);
        if (!restored.isAttuned()) {
            helper.fail("World Condenser attunement did not survive NBT persistence");
            return;
        }
        helper.setBlock(center.above().east(), Blocks.AIR);
        if (WorldCondenserFormation.isFormed(helper.getLevel(), interfacePos, interfaceState)) {
            helper.fail("World Condenser stayed formed after a hull block was removed");
            return;
        }
        helper.succeed();
    }
}
