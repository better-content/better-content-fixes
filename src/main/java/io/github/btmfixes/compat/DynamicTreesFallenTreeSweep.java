package io.github.btmfixes.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class DynamicTreesFallenTreeSweep {
    private static final List<ResourceLocation> REPRESENTATIVE_SPECIES = List.of(
            ResourceLocation.fromNamespaceAndPath("dynamictrees", "oak"),
            ResourceLocation.fromNamespaceAndPath("dthexerei", "witch_hazel"),
            ResourceLocation.fromNamespaceAndPath("dtmalum", "runewood")
    );
    private static final List<Block> SOIL_CANDIDATES = List.of(
            Blocks.GRASS_BLOCK,
            Blocks.DIRT,
            Blocks.PODZOL,
            Blocks.ROOTED_DIRT,
            Blocks.MUD,
            Blocks.MOSS_BLOCK,
            Blocks.MYCELIUM,
            Blocks.SAND,
            Blocks.RED_SAND,
            Blocks.TERRACOTTA,
            Blocks.NETHERRACK,
            Blocks.CRIMSON_NYLIUM,
            Blocks.WARPED_NYLIUM,
            Blocks.SOUL_SOIL,
            Blocks.SOUL_SAND
    );
    private static final int CLEAR_RADIUS = 24;
    private static final int CLEAR_HEIGHT = 40;
    private static final int BRANCH_SCAN_RADIUS = 6;
    private static final int BRANCH_SCAN_HEIGHT = 20;
    private static final int LANDING_SCAN_RADIUS = 28;
    private static final int LANDING_SCAN_HEIGHT = 24;
    private static final int SETTLE_TIMEOUT_TICKS = 200;
    private static final int POST_LANDING_SETTLE_TICKS = 60;

    private DynamicTreesFallenTreeSweep() {
    }

    public static SweepRunner start(final ServerLevel level, final BlockPos rootPos) throws ReflectiveOperationException {
        return new SweepRunner(level, rootPos, DynamicTreesReflection.resolve());
    }

    public enum PollResult {
        PENDING,
        READY_FOR_NEXT,
        COMPLETE
    }

    public static final class SweepRunner {
        private final ServerLevel level;
        private final BlockPos rootPos;
        private final BlockPos saplingPos;
        private final DynamicTreesReflection reflection;
        private final List<String> failures = new ArrayList<>();

        private int representativeIndex;
        private ProbeState current;

        private SweepRunner(final ServerLevel level, final BlockPos rootPos, final DynamicTreesReflection reflection) {
            this.level = level;
            this.rootPos = rootPos;
            this.saplingPos = rootPos.above();
            this.reflection = reflection;
        }

        public boolean startNextRepresentative() throws ReflectiveOperationException {
            current = null;
            while (representativeIndex < REPRESENTATIVE_SPECIES.size()) {
                final ResourceLocation speciesId = REPRESENTATIVE_SPECIES.get(representativeIndex++);
                final Object species = reflection.findSpecies(speciesId);
                if (species == null) {
                    continue;
                }

                final ProbeState prepared = prepareProbe(speciesId, species);
                if (prepared == null) {
                    failures.add(speciesId + " did not generate a rooted tree with a primitive-log payload");
                    continue;
                }
                current = prepared;
                return true;
            }
            return false;
        }

        public PollResult poll() throws ReflectiveOperationException {
            if (current == null) {
                return PollResult.COMPLETE;
            }

            current.totalTicks++;
            if (reflection.isRooty(level.getBlockState(rootPos)) || hasFallingTreeNear(level, rootPos, reflection)) {
                if (current.totalTicks > SETTLE_TIMEOUT_TICKS) {
                    failures.add(current.speciesId + " did not settle within " + SETTLE_TIMEOUT_TICKS + " ticks after support loss");
                    current = null;
                    return representativeIndex < REPRESENTATIVE_SPECIES.size() ? PollResult.READY_FOR_NEXT : PollResult.COMPLETE;
                }
                return PollResult.PENDING;
            }

            current.postLandingTicks++;
            if (current.postLandingTicks < POST_LANDING_SETTLE_TICKS) {
                return PollResult.PENDING;
            }

            final String failure = evaluateCurrent();
            if (failure != null) {
                failures.add(failure);
            }
            current = null;
            return representativeIndex < REPRESENTATIVE_SPECIES.size() ? PollResult.READY_FOR_NEXT : PollResult.COMPLETE;
        }

        public List<String> failures() {
            return List.copyOf(failures);
        }

        private ProbeState prepareProbe(final ResourceLocation speciesId, final Object species)
                throws ReflectiveOperationException {
            clearTestColumn(level, rootPos);
            if (!tryGenerateTree(level, rootPos, saplingPos, species, reflection)) {
                return null;
            }

            final Set<Block> primitiveLogs = collectPrimitiveLogs(level, rootPos, reflection);
            if (primitiveLogs.isEmpty()) {
                return null;
            }

            level.setBlock(rootPos.below(), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            return new ProbeState(speciesId, primitiveLogs);
        }

        private String evaluateCurrent() throws ReflectiveOperationException {
            if (reflection.isRooty(level.getBlockState(rootPos))) {
                return current.speciesId + " left a rooty block behind after support-loss fallover";
            }

            final BlockPos min = rootPos.offset(-LANDING_SCAN_RADIUS, -1, -LANDING_SCAN_RADIUS);
            final BlockPos max = rootPos.offset(LANDING_SCAN_RADIUS, LANDING_SCAN_HEIGHT, LANDING_SCAN_RADIUS);
            int placedLogs = 0;
            int horizontalLogs = 0;
            int placedLeaves = 0;

            for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
                final BlockState state = level.getBlockState(pos);
                if (current.primitiveLogs.contains(state.getBlock())) {
                    placedLogs++;
                    if (isHorizontalLog(state)) {
                        horizontalLogs++;
                    }
                }
                if (state.getBlock() instanceof LeavesBlock) {
                    placedLeaves++;
                }
            }

            if (placedLogs == 0) {
                return current.speciesId + " reconstructed no primitive log blocks in the settled landing area";
            }
            if (horizontalLogs == 0) {
                return current.speciesId + " reconstructed logs, but none settled horizontally";
            }
            if (placedLeaves == 0) {
                return current.speciesId + " reconstructed no primitive leaves in the settled landing area";
            }
            return null;
        }
    }

    private static Set<Block> collectPrimitiveLogs(final ServerLevel level, final BlockPos rootPos,
                                                   final DynamicTreesReflection reflection)
            throws ReflectiveOperationException {
        final Set<Block> primitiveLogs = new HashSet<>();
        for (int y = 1; y <= BRANCH_SCAN_HEIGHT; y++) {
            for (int x = -BRANCH_SCAN_RADIUS; x <= BRANCH_SCAN_RADIUS; x++) {
                for (int z = -BRANCH_SCAN_RADIUS; z <= BRANCH_SCAN_RADIUS; z++) {
                    final BlockState state = level.getBlockState(rootPos.offset(x, y, z));
                    final Block primitiveLog = reflection.getPrimitiveLog(state);
                    if (primitiveLog != null) {
                        primitiveLogs.add(primitiveLog);
                    }
                }
            }
        }
        return primitiveLogs;
    }

    private static boolean hasFallingTreeNear(final ServerLevel level, final BlockPos rootPos,
                                              final DynamicTreesReflection reflection) {
        final AABB box = new AABB(
                rootPos.getX() - LANDING_SCAN_RADIUS,
                rootPos.getY(),
                rootPos.getZ() - LANDING_SCAN_RADIUS,
                rootPos.getX() + LANDING_SCAN_RADIUS + 1,
                rootPos.getY() + LANDING_SCAN_HEIGHT + 1,
                rootPos.getZ() + LANDING_SCAN_RADIUS + 1
        );
        return !level.getEntitiesOfClass(Entity.class, box, reflection::isFallingTree).isEmpty();
    }

    private static boolean isHorizontalLog(final BlockState state) {
        if (state.hasProperty(RotatedPillarBlock.AXIS)) {
            final Direction.Axis axis = state.getValue(RotatedPillarBlock.AXIS);
            return axis == Direction.Axis.X || axis == Direction.Axis.Z;
        }
        final Property<?> axisProperty = state.getProperties().stream()
                .filter(property -> "axis".equals(property.getName()))
                .findFirst()
                .orElse(null);
        if (axisProperty == null) {
            return false;
        }
        final Object axisValue = state.getValue(axisProperty);
        return axisValue == Direction.Axis.X || axisValue == Direction.Axis.Z;
    }

    private static boolean tryGenerateTree(final ServerLevel level, final BlockPos rootPos, final BlockPos saplingPos,
                                           final Object species, final DynamicTreesReflection reflection)
            throws ReflectiveOperationException {
        final Block saplingBlock = reflection.getSaplingBlock(species);
        if (saplingBlock == null) {
            return false;
        }

        for (Block soil : SOIL_CANDIDATES) {
            clearTestColumn(level, rootPos);
            level.setBlock(rootPos.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
            level.setBlock(rootPos, soil.defaultBlockState(), Block.UPDATE_ALL);
            level.setBlock(saplingPos, saplingBlock.defaultBlockState(), Block.UPDATE_ALL);

            if (!reflection.transitionToTree(species, level, saplingPos)) {
                continue;
            }
            if (!reflection.isRooty(level.getBlockState(rootPos))) {
                continue;
            }
            if (collectPrimitiveLogs(level, rootPos, reflection).isEmpty()) {
                continue;
            }
            return true;
        }

        return false;
    }

    private static void clearTestColumn(final ServerLevel level, final BlockPos rootPos) {
        final BlockPos min = rootPos.offset(-CLEAR_RADIUS, -1, -CLEAR_RADIUS);
        final BlockPos max = rootPos.offset(CLEAR_RADIUS, CLEAR_HEIGHT, CLEAR_RADIUS);
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            final BlockState replacement = pos.getY() == rootPos.getY() - 1
                    ? Blocks.STONE.defaultBlockState()
                    : Blocks.AIR.defaultBlockState();
            level.setBlock(pos, replacement, Block.UPDATE_ALL);
        }
    }

    private static final class DynamicTreesReflection {
        private static DynamicTreesReflection instance;

        private final Method registryGetAll;
        private final Method registryEntryGetRegistryName;
        private final Method speciesGetSapling;
        private final Method speciesTransitionToTree;
        private final Method treeHelperIsRooty;
        private final Method branchBlockGetPrimitiveLog;
        private final Object speciesRegistry;
        private final Class<?> branchBlockClass;
        private final Class<?> fallingTreeEntityClass;

        private DynamicTreesReflection(final Method registryGetAll, final Method registryEntryGetRegistryName,
                                       final Method speciesGetSapling, final Method speciesTransitionToTree,
                                       final Method treeHelperIsRooty, final Method branchBlockGetPrimitiveLog,
                                       final Object speciesRegistry, final Class<?> branchBlockClass,
                                       final Class<?> fallingTreeEntityClass) {
            this.registryGetAll = registryGetAll;
            this.registryEntryGetRegistryName = registryEntryGetRegistryName;
            this.speciesGetSapling = speciesGetSapling;
            this.speciesTransitionToTree = speciesTransitionToTree;
            this.treeHelperIsRooty = treeHelperIsRooty;
            this.branchBlockGetPrimitiveLog = branchBlockGetPrimitiveLog;
            this.speciesRegistry = speciesRegistry;
            this.branchBlockClass = branchBlockClass;
            this.fallingTreeEntityClass = fallingTreeEntityClass;
        }

        private static DynamicTreesReflection resolve() throws ReflectiveOperationException {
            if (instance != null) {
                return instance;
            }

            final Class<?> speciesClass = Class.forName("com.ferreusveritas.dynamictrees.tree.species.Species");
            final Class<?> simpleRegistryClass = Class.forName("com.ferreusveritas.dynamictrees.api.registry.SimpleRegistry");
            final Class<?> registryEntryClass = Class.forName("com.ferreusveritas.dynamictrees.api.registry.RegistryEntry");
            final Class<?> treeHelperClass = Class.forName("com.ferreusveritas.dynamictrees.api.TreeHelper");
            final Class<?> branchBlockClass = Class.forName("com.ferreusveritas.dynamictrees.block.branch.BranchBlock");
            final Class<?> fallingTreeEntityClass = Class.forName("com.ferreusveritas.dynamictrees.entity.FallingTreeEntity");

            final Field registryField = speciesClass.getField("REGISTRY");
            final Object speciesRegistry = registryField.get(null);

            instance = new DynamicTreesReflection(
                    simpleRegistryClass.getMethod("getAll"),
                    registryEntryClass.getMethod("getRegistryName"),
                    speciesClass.getMethod("getSapling"),
                    speciesClass.getMethod("transitionToTree", net.minecraft.world.level.Level.class, BlockPos.class),
                    treeHelperClass.getMethod("isRooty", BlockState.class),
                    branchBlockClass.getMethod("getPrimitiveLog"),
                    speciesRegistry,
                    branchBlockClass,
                    fallingTreeEntityClass
            );
            return instance;
        }

        private Object findSpecies(final ResourceLocation id) throws ReflectiveOperationException {
            for (Object species : getAllSpecies()) {
                final ResourceLocation speciesId = getRegistryName(species);
                if (id.equals(speciesId)) {
                    return species;
                }
            }
            return null;
        }

        @SuppressWarnings("unchecked")
        private Set<Object> getAllSpecies() throws ReflectiveOperationException {
            return (Set<Object>) registryGetAll.invoke(speciesRegistry);
        }

        private ResourceLocation getRegistryName(final Object species) throws ReflectiveOperationException {
            return (ResourceLocation) registryEntryGetRegistryName.invoke(species);
        }

        @SuppressWarnings("unchecked")
        private Block getSaplingBlock(final Object species) throws ReflectiveOperationException {
            final Optional<Object> optionalSapling = (Optional<Object>) speciesGetSapling.invoke(species);
            if (optionalSapling.isEmpty()) {
                return null;
            }
            return (Block) optionalSapling.get();
        }

        private boolean transitionToTree(final Object species, final ServerLevel level, final BlockPos saplingPos)
                throws ReflectiveOperationException {
            return (boolean) speciesTransitionToTree.invoke(species, level, saplingPos);
        }

        private boolean isRooty(final BlockState state) throws ReflectiveOperationException {
            return (boolean) treeHelperIsRooty.invoke(null, state);
        }

        @SuppressWarnings("unchecked")
        private Block getPrimitiveLog(final BlockState state) throws ReflectiveOperationException {
            if (!branchBlockClass.isInstance(state.getBlock())) {
                return null;
            }
            final Optional<Block> primitiveLog = (Optional<Block>) branchBlockGetPrimitiveLog.invoke(state.getBlock());
            return primitiveLog.orElse(null);
        }

        private boolean isFallingTree(final Entity entity) {
            return fallingTreeEntityClass.isInstance(entity);
        }
    }

    private static final class ProbeState {
        private final ResourceLocation speciesId;
        private final Set<Block> primitiveLogs;
        private int totalTicks;
        private int postLandingTicks;

        private ProbeState(final ResourceLocation speciesId, final Set<Block> primitiveLogs) {
            this.speciesId = speciesId;
            this.primitiveLogs = Set.copyOf(primitiveLogs);
        }
    }
}
