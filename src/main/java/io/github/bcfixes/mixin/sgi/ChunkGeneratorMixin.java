package io.github.bcfixes.mixin.sgi;

import java.lang.reflect.Method;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ChunkGenerator.class, priority = 900)
public abstract class ChunkGeneratorMixin {
    private static final String SGI_TERRAIN_CONFORM_UTIL =
            "net.mcreator.structuregenerationimprover.TerrainConformUtil";

    // SGI injects this call at decoration HEAD. Deferring it lets it sample Hyle's final palette.
    @Redirect(
            method = "m_213609_",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/mcreator/structuregenerationimprover/TerrainConformUtil;applyDuringSurface(Lnet/minecraft/world/level/WorldGenLevel;Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/world/level/chunk/ChunkAccess;)V",
                    remap = false),
            require = 0,
            remap = false)
    private void bcfixes$deferSgiSurfaceConform(
            final WorldGenLevel level,
            final StructureManager structureManager,
            final ChunkAccess chunk) {
        // Invoked at the tail after Hyle/Unearthed has completed normal decoration.
    }

    @Inject(method = "m_213609_", at = @At("TAIL"), remap = false)
    private void bcfixes$applySgiSurfaceConformAfterDecoration(
            final WorldGenLevel level,
            final ChunkAccess chunk,
            final StructureManager structureManager,
            final CallbackInfo ci) {
        try {
            final Class<?> terrainConform = Class.forName(SGI_TERRAIN_CONFORM_UTIL);
            final Method applyDuringSurface = terrainConform.getMethod(
                    "applyDuringSurface", WorldGenLevel.class, StructureManager.class, ChunkAccess.class);
            applyDuringSurface.invoke(null, level, structureManager, chunk);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not defer SGI terrain conforming", e);
        }
    }
}
