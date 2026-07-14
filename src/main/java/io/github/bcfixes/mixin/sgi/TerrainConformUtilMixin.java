package io.github.bcfixes.mixin.sgi;

import io.github.bcfixes.config.BcFixesConfig;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.mcreator.structuregenerationimprover.TerrainConformUtil", remap = false)
public abstract class TerrainConformUtilMixin {
    private static final ResourceLocation BCFIXES_HYLE_STONE_REPLACER = new ResourceLocation("hyle", "stone_replacer");

    @Inject(method = "applyDuringSurface", at = @At("RETURN"), remap = false)
    private static void bcfixes$rerunHyleAfterSgiSurfaceConform(
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
    }

    private static long postPassSeed(final WorldGenLevel level, final ChunkAccess chunk) {
        return level.getSeed() ^ chunk.getPos().toLong() ^ 0x5A17_5EED_5191L;
    }
}
