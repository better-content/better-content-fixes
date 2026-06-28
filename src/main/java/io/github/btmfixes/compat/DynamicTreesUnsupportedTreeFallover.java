package io.github.btmfixes.compat;

import io.github.btmfixes.config.BtmFixesConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Method;

public final class DynamicTreesUnsupportedTreeFallover {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String ROOTY_BLOCK_CLASS_NAME = "com.ferreusveritas.dynamictrees.block.rooty.RootyBlock";

    private static Class<?> rootyBlockClass;
    private static Method destroyTreeMethod;
    private static Method getDecayBlockStateMethod;
    private static boolean reflectionResolved;

    private DynamicTreesUnsupportedTreeFallover() {
    }

    @SubscribeEvent
    public static void onNeighborNotify(final BlockEvent.NeighborNotifyEvent event) {
        if (!BtmFixesConfig.dynamicTreesDestroyUnsupportedTrees() || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        if (!event.getNotifiedSides().contains(Direction.UP)) {
            return;
        }
        destroyUnsupportedTree(level, event.getPos().above());
    }

    @SubscribeEvent
    public static void onChunkLoad(final ChunkEvent.Load event) {
        if (!BtmFixesConfig.dynamicTreesDestroyUnsupportedTrees() || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        if (!(event.getChunk() instanceof LevelChunk chunk)) {
            return;
        }
        for (BlockPos pos : chunk.getBlockEntitiesPos()) {
            destroyUnsupportedTree(level, pos);
        }
    }

    private static void destroyUnsupportedTree(final ServerLevel level, final BlockPos pos) {
        if (!resolveReflection()) {
            return;
        }

        final BlockState state = level.getBlockState(pos);
        if (!isRootyBlock(state.getBlock()) || isSupported(level, pos)) {
            return;
        }

        try {
            destroyTreeMethod.invoke(state.getBlock(), level, pos);
            if (level.getBlockState(pos).getBlock() == state.getBlock()) {
                final BlockState decayState = (BlockState) getDecayBlockStateMethod.invoke(state.getBlock(), state, level, pos);
                level.setBlock(pos, decayState, Block.UPDATE_ALL);
            }
        } catch (ReflectiveOperationException | RuntimeException e) {
            LOGGER.error("Failed to deconstruct unsupported Dynamic Trees root at {}", pos, e);
        }
    }

    private static boolean isRootyBlock(final Block block) {
        return rootyBlockClass.isInstance(block);
    }

    private static boolean isSupported(final LevelAccessor level, final BlockPos pos) {
        final BlockPos belowPos = pos.below();
        final BlockState belowState = level.getBlockState(belowPos);
        return belowState.isFaceSturdy(level, belowPos, Direction.UP);
    }

    private static boolean resolveReflection() {
        if (reflectionResolved) {
            return rootyBlockClass != null && destroyTreeMethod != null && getDecayBlockStateMethod != null;
        }
        reflectionResolved = true;
        try {
            rootyBlockClass = Class.forName(ROOTY_BLOCK_CLASS_NAME);
            destroyTreeMethod = rootyBlockClass.getMethod("destroyTree", net.minecraft.world.level.Level.class, BlockPos.class);
            getDecayBlockStateMethod = rootyBlockClass.getMethod("getDecayBlockState", BlockState.class, net.minecraft.world.level.BlockGetter.class, BlockPos.class);
            return true;
        } catch (ReflectiveOperationException e) {
            LOGGER.error("Failed to resolve Dynamic Trees rooty block reflection hooks", e);
            rootyBlockClass = null;
            destroyTreeMethod = null;
            getDecayBlockStateMethod = null;
            return false;
        }
    }
}
