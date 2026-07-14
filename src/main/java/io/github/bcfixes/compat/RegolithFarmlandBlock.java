package io.github.bcfixes.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.common.FarmlandWaterManager;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.IPlantable;

public final class RegolithFarmlandBlock extends FarmBlock {
    private final Block baseRegolithBlock;

    public RegolithFarmlandBlock(final Block baseRegolithBlock) {
        super(BlockBehaviour.Properties.copy(baseRegolithBlock).randomTicks());
        this.baseRegolithBlock = baseRegolithBlock;
    }

    @Override
    public BlockState updateShape(
            final BlockState state,
            final Direction direction,
            final BlockState neighborState,
            final LevelAccessor level,
            final BlockPos pos,
            final BlockPos neighborPos) {
        if (direction == Direction.UP && !state.canSurvive(level, pos)) {
            level.scheduleTick(pos, this, 1);
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public boolean canSurvive(final BlockState state, final LevelReader level, final BlockPos pos) {
        final BlockState above = level.getBlockState(pos.above());
        return !above.isSolid()
                || above.getBlock() instanceof FenceGateBlock
                || above.getBlock() instanceof MovingPistonBlock;
    }

    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context) {
        final BlockState farmland = defaultBlockState();
        if (!farmland.canSurvive(context.getLevel(), context.getClickedPos())) {
            return fallbackState();
        }
        return super.getStateForPlacement(context);
    }

    @Override
    public void tick(final BlockState state, final ServerLevel level, final BlockPos pos, final RandomSource random) {
        if (!state.canSurvive(level, pos)) {
            turnToBase(null, state, level, pos);
        }
    }

    @Override
    public void randomTick(final BlockState state, final ServerLevel level, final BlockPos pos, final RandomSource random) {
        final int moisture = state.getValue(MOISTURE);
        if (!isNearWater(level, pos) && !level.isRainingAt(pos.above())) {
            if (moisture > 0) {
                level.setBlock(pos, state.setValue(MOISTURE, moisture - 1), 2);
            } else if (!shouldMaintainFarmland(level, pos)) {
                turnToBase(null, state, level, pos);
            }
        } else if (moisture < MAX_MOISTURE) {
            level.setBlock(pos, state.setValue(MOISTURE, MAX_MOISTURE), 2);
        }
    }

    @Override
    public void fallOn(final Level level, final BlockState state, final BlockPos pos, final Entity entity, final float fallDistance) {
        if (!level.isClientSide
                && ForgeHooks.onFarmlandTrample(level, pos, fallbackState(), fallDistance, entity)) {
            turnToBase(entity, state, level, pos);
        }
        super.fallOn(level, state, pos, entity, fallDistance);
    }

    private BlockState fallbackState() {
        return baseRegolithBlock.defaultBlockState();
    }

    private void turnToBase(final Entity entity, final BlockState state, final Level level, final BlockPos pos) {
        final BlockState replacement = pushEntitiesUp(state, fallbackState(), level, pos);
        level.setBlockAndUpdate(pos, replacement);
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(entity, replacement));
    }

    private static boolean shouldMaintainFarmland(final BlockGetter level, final BlockPos pos) {
        final BlockState plantState = level.getBlockState(pos.above());
        final BlockState farmlandState = level.getBlockState(pos);
        return plantState.getBlock() instanceof IPlantable plantable
                && farmlandState.canSustainPlant(level, pos, Direction.UP, plantable);
    }

    private static boolean isNearWater(final LevelReader level, final BlockPos pos) {
        final BlockState state = level.getBlockState(pos);
        for (BlockPos candidate : BlockPos.betweenClosed(pos.offset(-4, 0, -4), pos.offset(4, 1, 4))) {
            final FluidState fluidState = level.getFluidState(candidate);
            if (state.canBeHydrated(level, pos, fluidState, candidate)) {
                return true;
            }
        }
        return FarmlandWaterManager.hasBlockWaterTicket(level, pos);
    }
}
