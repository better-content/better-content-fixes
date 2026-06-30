package io.github.btmfixes.compat;

import io.github.btmfixes.config.BtmFixesConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class DynamicTreesFallenTreeReconstruction {
    private static final int FALLEN_TREE_PLACEMENT_FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
    private static final String FALLING_TREE_ENTITY_CLASS_NAME = "com.ferreusveritas.dynamictrees.entity.FallingTreeEntity";
    private static final String BRANCH_BLOCK_CLASS_NAME = "com.ferreusveritas.dynamictrees.block.branch.BranchBlock";
    private static final String BRANCH_DESTRUCTION_DATA_CLASS_NAME = "com.ferreusveritas.dynamictrees.util.BranchDestructionData";

    private static Class<?> fallingTreeEntityClass;
    private static Class<?> branchBlockClass;
    private static Class<?> branchDestructionDataClass;
    private static Method getDestroyDataMethod;
    private static Method getPayloadMethod;
    private static Method getNumBranchesMethod;
    private static Method getBranchRelPosMethod;
    private static Method getBranchBlockStateMethod;
    private static Method getConnectionsMethod;
    private static Method getPrimitiveLogMethod;
    private static Method getNumLeavesMethod;
    private static Method getLeavesRelPosMethod;
    private static Method getLeavesBlockStateMethod;
    private static Field leavesDropsField;
    private static boolean reflectionResolved;

    private DynamicTreesFallenTreeReconstruction() {
    }

    public static boolean suppressEarlyLeafDrops(final Object entity) {
        return BtmFixesConfig.dynamicTreesReconstructFallenLogs() && isFallingTreeEntity(entity);
    }

    public static void reconstructAtLanding(final Object entity) {
        if (!BtmFixesConfig.dynamicTreesReconstructFallenLogs()
                || !isFallingTreeEntity(entity)
                || !(entity instanceof Entity mcEntity)
                || !(mcEntity.level() instanceof ServerLevel level)) {
            return;
        }
        if (!resolveReflection()) {
            return;
        }

        try {
            final Object destroyData = getDestroyDataMethod.invoke(entity);
            if (destroyData == null) {
                return;
            }
            @SuppressWarnings("unchecked")
            final List<ItemStack> payload = (List<ItemStack>) getPayloadMethod.invoke(entity);
            if (payload == null) {
                return;
            }

            final Direction fallDirection = resolveFallDirection(mcEntity);
            final BlockPos origin = mcEntity.blockPosition();

            placeLogs(level, origin, fallDirection, destroyData, payload);
            placeLeaves(level, origin, fallDirection, destroyData);
        } catch (ReflectiveOperationException ignored) {
            // leave Dynamic Trees' default payload behavior untouched
        }
    }

    private static void placeLogs(final ServerLevel level, final BlockPos origin, final Direction fallDirection,
                                  final Object destroyData, final List<ItemStack> payload)
            throws ReflectiveOperationException {
        final int branchCount = (int) getNumBranchesMethod.invoke(destroyData);
        if (branchCount <= 0) {
            return;
        }

        final List<PlacedBranch> branches = new ArrayList<>(branchCount);
        int minY = 0;
        for (int i = 0; i < branchCount; i++) {
            final BlockPos relPos = (BlockPos) getBranchRelPosMethod.invoke(destroyData, i);
            final BlockState branchState = (BlockState) getBranchBlockStateMethod.invoke(destroyData, i);
            final Block primitiveLog = getPrimitiveLog(branchState);
            if (primitiveLog == null) {
                continue;
            }

            final int[] connections = new int[6];
            getConnectionsMethod.invoke(destroyData, i, connections);
            final BlockPos transformed = transformRelPos(relPos, fallDirection);
            minY = Math.min(minY, transformed.getY());
            branches.add(new PlacedBranch(transformed, primitiveLog, resolveLogAxis(fallDirection, connections)));
        }

        if (branches.isEmpty()) {
            return;
        }

        final int yOffset = -minY;
        for (PlacedBranch branch : branches) {
            final BlockPos targetPos = origin.offset(branch.relPos().getX(), branch.relPos().getY() + yOffset, branch.relPos().getZ());
            final BlockState targetState = level.getBlockState(targetPos);
            if (!canReplaceForFallenTree(targetState)) {
                continue;
            }

            final ItemStack matchingPayload = findPayloadStackForBlock(payload, branch.block());
            if (matchingPayload == null) {
                continue;
            }

            final BlockState placedState = orientLogState(branch.block().defaultBlockState(), branch.axis());
            if (level.setBlock(targetPos, placedState, FALLEN_TREE_PLACEMENT_FLAGS)) {
                matchingPayload.shrink(1);
            }
        }
    }

    private static void placeLeaves(final ServerLevel level, final BlockPos origin, final Direction fallDirection,
                                    final Object destroyData) throws ReflectiveOperationException {
        final int leavesCount = (int) getNumLeavesMethod.invoke(destroyData);
        if (leavesCount <= 0) {
            return;
        }

        final List<PlacedLeaf> leaves = new ArrayList<>(leavesCount);
        int minY = 0;
        for (int i = 0; i < leavesCount; i++) {
            final BlockPos relPos = (BlockPos) getLeavesRelPosMethod.invoke(destroyData, i);
            final BlockState leafState = (BlockState) getLeavesBlockStateMethod.invoke(destroyData, i);
            if (!(leafState.getBlock() instanceof LeavesBlock)) {
                continue;
            }
            final BlockPos transformed = transformRelPos(relPos, fallDirection);
            minY = Math.min(minY, transformed.getY());
            leaves.add(new PlacedLeaf(transformed, leafState));
        }

        if (leaves.isEmpty()) {
            return;
        }

        final int yOffset = -minY;
        for (PlacedLeaf leaf : leaves) {
            final BlockPos targetPos = origin.offset(leaf.relPos().getX(), leaf.relPos().getY() + yOffset, leaf.relPos().getZ());
            if (!canReplaceForFallenTree(level.getBlockState(targetPos))) {
                continue;
            }
            level.setBlock(targetPos, normalizeLeafState(leaf.state()), FALLEN_TREE_PLACEMENT_FLAGS);
        }

        @SuppressWarnings("unchecked")
        final List<Object> leavesDrops = (List<Object>) leavesDropsField.get(destroyData);
        leavesDrops.clear();
    }

    private static Block getPrimitiveLog(final BlockState branchState) throws ReflectiveOperationException {
        if (!branchBlockClass.isInstance(branchState.getBlock())) {
            return null;
        }
        @SuppressWarnings("unchecked")
        final Optional<Block> primitiveLog = (Optional<Block>) getPrimitiveLogMethod.invoke(branchState.getBlock());
        if (primitiveLog.isEmpty()) {
            return null;
        }
        final Block block = primitiveLog.get();
        return block.asItem() instanceof BlockItem ? block : null;
    }

    private static ItemStack findPayloadStackForBlock(final List<ItemStack> payload, final Block block) {
        for (ItemStack stack : payload) {
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() == block) {
                return stack;
            }
        }
        return null;
    }

    private static Direction resolveFallDirection(final Entity entity) {
        final Direction horizontal = Direction.fromYRot(entity.getYRot());
        return horizontal.getAxis().isHorizontal() ? horizontal : Direction.EAST;
    }

    private static BlockPos transformRelPos(final BlockPos relPos, final Direction fallDirection) {
        return switch (fallDirection.getAxis()) {
            case X -> new BlockPos(fallDirection.getStepX() * relPos.getY(), relPos.getX(), relPos.getZ());
            case Z -> new BlockPos(relPos.getX(), relPos.getZ(), fallDirection.getStepZ() * relPos.getY());
            default -> relPos;
        };
    }

    private static Direction.Axis resolveLogAxis(final Direction fallDirection, final int[] connections) {
        final int originalY = Math.max(connections[0], connections[1]);
        final int originalZ = Math.max(connections[2], connections[3]);
        final int originalX = Math.max(connections[4], connections[5]);

        final int xScore;
        final int yScore;
        final int zScore;
        if (fallDirection.getAxis() == Direction.Axis.X) {
            xScore = originalY;
            yScore = originalX;
            zScore = originalZ;
        } else {
            xScore = originalX;
            yScore = originalZ;
            zScore = originalY;
        }

        if (xScore >= yScore && xScore >= zScore) {
            return Direction.Axis.X;
        }
        if (zScore >= xScore && zScore >= yScore) {
            return Direction.Axis.Z;
        }
        return Direction.Axis.Y;
    }

    private static BlockState orientLogState(final BlockState state, final Direction.Axis axis) {
        if (state.hasProperty(RotatedPillarBlock.AXIS)) {
            return state.setValue(RotatedPillarBlock.AXIS, axis);
        }
        final Property<?> axisProperty = state.getProperties().stream()
                .filter(property -> "axis".equals(property.getName()))
                .findFirst()
                .orElse(null);
        if (axisProperty == null) {
            return state;
        }
        @SuppressWarnings({"rawtypes", "unchecked"})
        final Property rawProperty = axisProperty;
        return state.setValue(rawProperty, axis);
    }

    private static boolean canReplaceForFallenTree(final BlockState state) {
        return state.isAir() || state.canBeReplaced();
    }

    private static BlockState normalizeLeafState(final BlockState state) {
        if (state.hasProperty(LeavesBlock.PERSISTENT)) {
            return state.setValue(LeavesBlock.PERSISTENT, true);
        }
        return state;
    }

    private static boolean isFallingTreeEntity(final Object entity) {
        return resolveReflection() && fallingTreeEntityClass.isInstance(entity);
    }

    private static boolean resolveReflection() {
        if (reflectionResolved) {
            return fallingTreeEntityClass != null
                    && branchBlockClass != null
                    && branchDestructionDataClass != null
                    && getDestroyDataMethod != null
                    && getPayloadMethod != null
                    && getNumBranchesMethod != null
                    && getBranchRelPosMethod != null
                    && getBranchBlockStateMethod != null
                    && getConnectionsMethod != null
                    && getPrimitiveLogMethod != null
                    && getNumLeavesMethod != null
                    && getLeavesRelPosMethod != null
                    && getLeavesBlockStateMethod != null
                    && leavesDropsField != null;
        }
        reflectionResolved = true;
        try {
            fallingTreeEntityClass = Class.forName(FALLING_TREE_ENTITY_CLASS_NAME);
            branchBlockClass = Class.forName(BRANCH_BLOCK_CLASS_NAME);
            branchDestructionDataClass = Class.forName(BRANCH_DESTRUCTION_DATA_CLASS_NAME);

            getDestroyDataMethod = fallingTreeEntityClass.getMethod("getDestroyData");
            getPayloadMethod = fallingTreeEntityClass.getMethod("getPayload");

            getNumBranchesMethod = branchDestructionDataClass.getMethod("getNumBranches");
            getBranchRelPosMethod = branchDestructionDataClass.getMethod("getBranchRelPos", int.class);
            getBranchBlockStateMethod = branchDestructionDataClass.getMethod("getBranchBlockState", int.class);
            getConnectionsMethod = branchDestructionDataClass.getMethod("getConnections", int.class, int[].class);
            getNumLeavesMethod = branchDestructionDataClass.getMethod("getNumLeaves");
            getLeavesRelPosMethod = branchDestructionDataClass.getMethod("getLeavesRelPos", int.class);
            getLeavesBlockStateMethod = branchDestructionDataClass.getMethod("getLeavesBlockState", int.class);
            leavesDropsField = branchDestructionDataClass.getField("leavesDrops");

            getPrimitiveLogMethod = branchBlockClass.getMethod("getPrimitiveLog");
            return true;
        } catch (ReflectiveOperationException e) {
            fallingTreeEntityClass = null;
            branchBlockClass = null;
            branchDestructionDataClass = null;
            getDestroyDataMethod = null;
            getPayloadMethod = null;
            getNumBranchesMethod = null;
            getBranchRelPosMethod = null;
            getBranchBlockStateMethod = null;
            getConnectionsMethod = null;
            getPrimitiveLogMethod = null;
            getNumLeavesMethod = null;
            getLeavesRelPosMethod = null;
            getLeavesBlockStateMethod = null;
            leavesDropsField = null;
            return false;
        }
    }

    private record PlacedBranch(BlockPos relPos, Block block, Direction.Axis axis) {
    }

    private record PlacedLeaf(BlockPos relPos, BlockState state) {
    }
}
