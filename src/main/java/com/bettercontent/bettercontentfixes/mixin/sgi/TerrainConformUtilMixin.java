package com.bettercontent.bettercontentfixes.mixin.sgi;

import com.bettercontent.bettercontentfixes.config.BcFixesConfig;
import java.util.Set;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.mcreator.structuregenerationimprover.TerrainConformUtil", remap = false)
public abstract class TerrainConformUtilMixin {
    private static final ResourceLocation BCFIXES_HYLE_STONE_REPLACER = new ResourceLocation("hyle", "stone_replacer");
    private static final Set<Block> BCFIXES_VANILLA_ROCK_HOSTS = Set.of(
            Blocks.STONE,
            Blocks.GRANITE,
            Blocks.DIORITE,
            Blocks.ANDESITE,
            Blocks.DEEPSLATE,
            Blocks.TUFF);
    private static final int BCFIXES_MAX_VERTICAL_PALETTE_SEARCH = 64;

    @Inject(method = "applyDuringSurface", at = @At("RETURN"), remap = false)
    private static void better_content_fixes$rerunHyleAfterSgiSurfaceConform(
            final WorldGenLevel level,
            final StructureManager structureManager,
            final ChunkAccess chunk,
            final CallbackInfo ci) {
        if (!BcFixesConfig.sgiRerunHyleAfterSurfaceConform()) {
            return;
        }
        if (!(level instanceof WorldGenRegion region) || chunk == null) {
            return;
        }

        final Optional<Registry<ConfiguredFeature<?, ?>>> configuredFeatures =
                region.registryAccess().registry(Registries.CONFIGURED_FEATURE);
        if (configuredFeatures.isEmpty()) {
            return;
        }

        final ConfiguredFeature<?, ?> stoneReplacer = configuredFeatures.get().get(BCFIXES_HYLE_STONE_REPLACER);
        if (stoneReplacer == null) {
            return;
        }

        final ChunkGenerator generator = region.getLevel().getChunkSource().getGenerator();
        final BlockPos origin = new BlockPos(
                chunk.getPos().getMinBlockX(),
                region.getMinBuildHeight(),
                chunk.getPos().getMinBlockZ());
        stoneReplacer.place(level, generator, RandomSource.create(postPassSeed(level, chunk)), origin);
        better_content_fixes$replaceResidualVanillaRock(chunk);
    }

    private static void better_content_fixes$replaceResidualVanillaRock(final ChunkAccess chunk) {
        final int minY = chunk.getMinBuildHeight();
        final int maxY = chunk.getMaxBuildHeight() - 1;
        final int minX = chunk.getPos().getMinBlockX();
        final int minZ = chunk.getPos().getMinBlockZ();
        final BlockPos.MutableBlockPos target = new BlockPos.MutableBlockPos();
        final BlockPos.MutableBlockPos probe = new BlockPos.MutableBlockPos();

        for (int y = minY; y <= maxY; y++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                for (int localX = 0; localX < 16; localX++) {
                    target.set(minX + localX, y, minZ + localZ);
                    final BlockState original = chunk.getBlockState(target);
                    if (!BCFIXES_VANILLA_ROCK_HOSTS.contains(original.getBlock())) {
                        continue;
                    }
                    final BlockState replacement = better_content_fixes$nearestUnearthedStone(
                            chunk, probe, target.getX(), y, target.getZ(), minY, maxY);
                    if (replacement != null) {
                        chunk.setBlockState(target, replacement, false);
                    }
                }
            }
        }
    }

    private static BlockState better_content_fixes$nearestUnearthedStone(
            final ChunkAccess chunk,
            final BlockPos.MutableBlockPos probe,
            final int x,
            final int y,
            final int z,
            final int minY,
            final int maxY) {
        for (int distance = 1; distance <= BCFIXES_MAX_VERTICAL_PALETTE_SEARCH; distance++) {
            if (y - distance >= minY) {
                final BlockState below = chunk.getBlockState(probe.set(x, y - distance, z));
                if (better_content_fixes$isUnearthedStone(below)) {
                    return below;
                }
            }
            if (y + distance <= maxY) {
                final BlockState above = chunk.getBlockState(probe.set(x, y + distance, z));
                if (better_content_fixes$isUnearthedStone(above)) {
                    return above;
                }
            }
        }
        return null;
    }

    private static boolean better_content_fixes$isUnearthedStone(final BlockState state) {
        final ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if (!"unearthed".equals(id.getNamespace())) {
            return false;
        }
        final String path = id.getPath();
        return !path.contains("regolith") && !path.contains("overgrown");
    }

    private static long postPassSeed(final WorldGenLevel level, final ChunkAccess chunk) {
        return level.getSeed() ^ chunk.getPos().toLong() ^ 0x5A17_5EED_5191L;
    }
}
