package io.github.btmfixes.compat;

import io.github.btmfixes.config.BtmFixesConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class DynamicTreesUnsupportedTreeFallover {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String ROOTY_BLOCK_CLASS_NAME = "com.ferreusveritas.dynamictrees.block.rooty.RootyBlock";
    private static final int MAX_CHUNK_SWEEPS_PER_TICK = 2;
    private static final Queue<QueuedChunkSweep> PENDING_CHUNK_SWEEPS = new ArrayDeque<>();
    private static final Set<QueuedChunkSweep> PENDING_CHUNK_SWEEP_KEYS = new HashSet<>();

    private static Class<?> rootyBlockClass;
    private static Class<?> branchBlockClass;
    private static Class<?> branchDestructionDataClass;
    private static Class<?> speciesClass;
    private static Class<?> netVolumeClass;
    private static Class<?> fallingTreeEntityClass;
    private static Class<? extends Enum<?>> fallingTreeDestroyTypeClass;
    private static Method destroyTreeMethod;
    private static Method getDecayBlockStateMethod;
    private static Method destroyBranchFromNodeMethod;
    private static Method getBranchesDropsMethod;
    private static Method dropTreeMethod;
    private static Field destructionDataSpeciesField;
    private static Field destructionDataWoodVolumeField;
    private static Object harvestDestroyType;
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
        enqueueChunkSweep(level, chunk);
    }

    @SubscribeEvent
    public static void onServerLevelTick(final TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level)) {
            return;
        }
        if (!BtmFixesConfig.dynamicTreesDestroyUnsupportedTrees() || PENDING_CHUNK_SWEEPS.isEmpty()) {
            return;
        }

        int processed = 0;
        while (processed < MAX_CHUNK_SWEEPS_PER_TICK) {
            final QueuedChunkSweep sweep = PENDING_CHUNK_SWEEPS.peek();
            if (sweep == null) {
                return;
            }
            if (!sweep.dimension().equals(level.dimension())) {
                return;
            }

            PENDING_CHUNK_SWEEPS.poll();
            PENDING_CHUNK_SWEEP_KEYS.remove(sweep);
            processed++;

            if (!level.hasChunk(sweep.chunkX(), sweep.chunkZ())) {
                continue;
            }

            final ChunkAccess chunkAccess = level.getChunkSource().getChunkNow(sweep.chunkX(), sweep.chunkZ());
            if (!(chunkAccess instanceof LevelChunk chunk)) {
                continue;
            }

            scanChunkForUnsupportedTrees(level, chunk);
        }
    }

    private static void scanChunkForUnsupportedTrees(final ServerLevel level, final LevelChunk chunk) {
        if (!resolveReflection()) {
            return;
        }

        final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        final int minSectionY = chunk.getMinSection();
        final LevelChunkSection[] sections = chunk.getSections();

        for (int sectionIndex = 0; sectionIndex < sections.length; sectionIndex++) {
            final LevelChunkSection section = sections[sectionIndex];
            if (section == null || section.hasOnlyAir()) {
                continue;
            }

            final int sectionY = (minSectionY + sectionIndex) << 4;
            final PalettedContainer<BlockState> states = section.getStates();
            for (int localY = 0; localY < 16; localY++) {
                for (int localZ = 0; localZ < 16; localZ++) {
                    for (int localX = 0; localX < 16; localX++) {
                        final BlockState state = states.get(localX, localY, localZ);
                        if (!isRootyBlock(state.getBlock())) {
                            continue;
                        }
                        cursor.set(
                                chunk.getPos().getMinBlockX() + localX,
                                sectionY + localY,
                                chunk.getPos().getMinBlockZ() + localZ
                        );
                        destroyUnsupportedTree(level, cursor);
                    }
                }
            }
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
            if (!dropUnsupportedTree(level, pos)) {
                destroyTreeMethod.invoke(state.getBlock(), level, pos);
            }
            if (level.getBlockState(pos).getBlock() == state.getBlock()) {
                final BlockState decayState = (BlockState) getDecayBlockStateMethod.invoke(state.getBlock(), state, level, pos);
                level.setBlock(pos, decayState, Block.UPDATE_ALL);
            }
        } catch (ReflectiveOperationException | RuntimeException e) {
            LOGGER.error("Failed to deconstruct unsupported Dynamic Trees root at {}", pos, e);
        }
    }

    @SuppressWarnings("unchecked")
    private static boolean dropUnsupportedTree(final ServerLevel level, final BlockPos rootPos)
            throws ReflectiveOperationException {
        if (branchBlockClass == null
                || destroyBranchFromNodeMethod == null
                || getBranchesDropsMethod == null
                || dropTreeMethod == null
                || destructionDataSpeciesField == null
                || destructionDataWoodVolumeField == null
                || harvestDestroyType == null) {
            return false;
        }

        final BlockPos branchPos = rootPos.above();
        final BlockState branchState = level.getBlockState(branchPos);
        if (!branchBlockClass.isInstance(branchState.getBlock())) {
            return false;
        }

        final Object destructionData = destroyBranchFromNodeMethod.invoke(
                branchState.getBlock(),
                level,
                branchPos,
                Direction.DOWN,
                true,
                null
        );
        if (destructionData == null) {
            return false;
        }

        final Object species = destructionDataSpeciesField.get(destructionData);
        final Object woodVolume = destructionDataWoodVolumeField.get(destructionData);
        if (species == null || woodVolume == null) {
            return false;
        }

        final List<?> branchDrops = (List<?>) getBranchesDropsMethod.invoke(species, level, woodVolume);
        final Object fallingTree = dropTreeMethod.invoke(
                null,
                level,
                destructionData,
                branchDrops != null ? branchDrops : new ArrayList<>(),
                harvestDestroyType
        );
        return fallingTree != null;
    }

    private static boolean isRootyBlock(final Block block) {
        return rootyBlockClass != null && rootyBlockClass.isInstance(block);
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
            branchBlockClass = Class.forName("com.ferreusveritas.dynamictrees.block.branch.BranchBlock");
            branchDestructionDataClass = Class.forName("com.ferreusveritas.dynamictrees.util.BranchDestructionData");
            speciesClass = Class.forName("com.ferreusveritas.dynamictrees.tree.species.Species");
            netVolumeClass = Class.forName("com.ferreusveritas.dynamictrees.systems.nodemapper.NetVolumeNode$Volume");
            fallingTreeEntityClass = Class.forName("com.ferreusveritas.dynamictrees.entity.FallingTreeEntity");
            fallingTreeDestroyTypeClass =
                    (Class<? extends Enum<?>>) Class.forName("com.ferreusveritas.dynamictrees.entity.FallingTreeEntity$DestroyType");
            destroyTreeMethod = rootyBlockClass.getMethod("destroyTree", net.minecraft.world.level.Level.class, BlockPos.class);
            getDecayBlockStateMethod = rootyBlockClass.getMethod("getDecayBlockState", BlockState.class, net.minecraft.world.level.BlockGetter.class, BlockPos.class);
            destroyBranchFromNodeMethod = branchBlockClass.getMethod(
                    "destroyBranchFromNode",
                    net.minecraft.world.level.Level.class,
                    BlockPos.class,
                    Direction.class,
                    boolean.class,
                    net.minecraft.world.entity.LivingEntity.class
            );
            getBranchesDropsMethod = speciesClass.getMethod("getBranchesDrops", net.minecraft.world.level.Level.class, netVolumeClass);
            dropTreeMethod = fallingTreeEntityClass.getMethod(
                    "dropTree",
                    net.minecraft.world.level.Level.class,
                    branchDestructionDataClass,
                    List.class,
                    fallingTreeDestroyTypeClass
            );
            destructionDataSpeciesField = branchDestructionDataClass.getField("species");
            destructionDataWoodVolumeField = branchDestructionDataClass.getField("woodVolume");
            harvestDestroyType = Enum.valueOf((Class) fallingTreeDestroyTypeClass, "HARVEST");
            return true;
        } catch (ReflectiveOperationException e) {
            LOGGER.error("Failed to resolve Dynamic Trees rooty block reflection hooks", e);
            rootyBlockClass = null;
            branchBlockClass = null;
            branchDestructionDataClass = null;
            speciesClass = null;
            netVolumeClass = null;
            fallingTreeEntityClass = null;
            fallingTreeDestroyTypeClass = null;
            destroyTreeMethod = null;
            getDecayBlockStateMethod = null;
            destroyBranchFromNodeMethod = null;
            getBranchesDropsMethod = null;
            dropTreeMethod = null;
            destructionDataSpeciesField = null;
            destructionDataWoodVolumeField = null;
            harvestDestroyType = null;
            return false;
        }
    }

    private static void enqueueChunkSweep(final ServerLevel level, final LevelChunk chunk) {
        final QueuedChunkSweep sweep = new QueuedChunkSweep(level.dimension(), chunk.getPos().x, chunk.getPos().z);
        if (PENDING_CHUNK_SWEEP_KEYS.add(sweep)) {
            PENDING_CHUNK_SWEEPS.offer(sweep);
        }
    }

    private record QueuedChunkSweep(ResourceKey<Level> dimension, int chunkX, int chunkZ) {
        private QueuedChunkSweep {
            Objects.requireNonNull(dimension, "dimension");
        }
    }
}
