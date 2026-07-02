package io.github.btmfixes.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class DynamicTreesSupportSweep {
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
    private static final int CLEAR_RADIUS = 8;
    private static final int CLEAR_HEIGHT = 32;

    private DynamicTreesSupportSweep() {
    }

    public static List<String> run(final ServerLevel level, final BlockPos rootPos) throws ReflectiveOperationException {
        final DynamicTreesReflection reflection = DynamicTreesReflection.resolve();
        final BlockPos saplingPos = rootPos.above();
        final List<String> failures = new ArrayList<>();

        for (Object species : reflection.getAllSpecies()) {
            final ResourceLocation speciesId = reflection.getRegistryName(species);
            if (speciesId == null || "null".equals(speciesId.getPath())) {
                continue;
            }
            if (shouldSkipSpecies(speciesId)) {
                continue;
            }

            clearTestColumn(level, rootPos);
            final boolean generated = tryGenerateTree(level, rootPos, saplingPos, species, reflection);
            if (!generated) {
                failures.add(speciesId + " did not generate a rooted tree");
                continue;
            }

            level.setBlock(rootPos.below(), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            if (reflection.isRooty(level.getBlockState(rootPos))) {
                failures.add(speciesId + " root survived support loss");
            }
        }

        return failures;
    }

    private static boolean shouldSkipSpecies(final ResourceLocation speciesId) {
        final String path = speciesId.getPath();
        return path.endsWith("_undergrowth") || path.contains("mushroom");
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
            if (!hasBranchNear(level, rootPos, reflection)) {
                continue;
            }
            return true;
        }

        return false;
    }

    private static boolean hasBranchNear(final ServerLevel level, final BlockPos rootPos,
                                         final DynamicTreesReflection reflection) throws ReflectiveOperationException {
        for (int y = 1; y <= 12; y++) {
            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    if (reflection.isBranch(level.getBlockState(rootPos.offset(x, y, z)))) {
                        return true;
                    }
                }
            }
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
        private final Method treeHelperIsBranch;
        private final Object speciesRegistry;

        private DynamicTreesReflection(final Method registryGetAll, final Method registryEntryGetRegistryName,
                                       final Method speciesGetSapling, final Method speciesTransitionToTree,
                                       final Method treeHelperIsRooty, final Method treeHelperIsBranch,
                                       final Object speciesRegistry) {
            this.registryGetAll = registryGetAll;
            this.registryEntryGetRegistryName = registryEntryGetRegistryName;
            this.speciesGetSapling = speciesGetSapling;
            this.speciesTransitionToTree = speciesTransitionToTree;
            this.treeHelperIsRooty = treeHelperIsRooty;
            this.treeHelperIsBranch = treeHelperIsBranch;
            this.speciesRegistry = speciesRegistry;
        }

        private static DynamicTreesReflection resolve() throws ReflectiveOperationException {
            if (instance != null) {
                return instance;
            }

            final Class<?> speciesClass = Class.forName("com.ferreusveritas.dynamictrees.tree.species.Species");
            final Class<?> simpleRegistryClass = Class.forName("com.ferreusveritas.dynamictrees.api.registry.SimpleRegistry");
            final Class<?> registryEntryClass = Class.forName("com.ferreusveritas.dynamictrees.api.registry.RegistryEntry");
            final Class<?> treeHelperClass = Class.forName("com.ferreusveritas.dynamictrees.api.TreeHelper");

            final Field registryField = speciesClass.getField("REGISTRY");
            final Object speciesRegistry = registryField.get(null);

            final Method registryGetAll = simpleRegistryClass.getMethod("getAll");
            final Method registryEntryGetRegistryName = registryEntryClass.getMethod("getRegistryName");
            final Method speciesGetSapling = speciesClass.getMethod("getSapling");
            final Method speciesTransitionToTree = speciesClass.getMethod("transitionToTree", net.minecraft.world.level.Level.class, BlockPos.class);
            final Method treeHelperIsRooty = treeHelperClass.getMethod("isRooty", BlockState.class);
            final Method treeHelperIsBranch = treeHelperClass.getMethod("isBranch", BlockState.class);

            instance = new DynamicTreesReflection(
                    registryGetAll,
                    registryEntryGetRegistryName,
                    speciesGetSapling,
                    speciesTransitionToTree,
                    treeHelperIsRooty,
                    treeHelperIsBranch,
                    speciesRegistry
            );
            return instance;
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

        private boolean isBranch(final BlockState state) throws ReflectiveOperationException {
            return (boolean) treeHelperIsBranch.invoke(null, state);
        }
    }
}
