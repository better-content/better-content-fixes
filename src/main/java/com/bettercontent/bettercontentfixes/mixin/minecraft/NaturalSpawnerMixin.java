package com.bettercontent.bettercontentfixes.mixin.minecraft;

import com.bettercontent.bettercontentfixes.config.BcFixesConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Adds the requested Y-gated extra natural-monster passes without consulting player height. */
@Mixin(NaturalSpawner.class)
public abstract class NaturalSpawnerMixin {
    @Invoker("spawnCategoryForChunk")
    private static void better_content_fixes$invokeSpawnCategoryForChunk(
            final MobCategory category,
            final ServerLevel level,
            final LevelChunk chunk,
            final NaturalSpawner.SpawnPredicate predicate,
            final NaturalSpawner.AfterSpawnCallback callback) {
        throw new AssertionError("mixin invoker");
    }

    @Redirect(
            method = "spawnForChunk",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/NaturalSpawner$SpawnState;canSpawnForCategory(Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/world/level/ChunkPos;)Z"),
            require = 1)
    private static boolean better_content_fixes$raiseOverworldMonsterCap(
            final NaturalSpawner.SpawnState spawnState,
            final MobCategory category,
            final ChunkPos chunkPos,
            final ServerLevel level,
            final LevelChunk chunk,
            final NaturalSpawner.SpawnState ignoredSpawnState,
            final boolean spawnFriendlies,
            final boolean spawnMonsters,
            final boolean forcedDespawn) {
        if (!BcFixesConfig.mobsVerticalNaturalSpawnScaling()
                || !level.dimension().equals(Level.OVERWORLD)
                || category != MobCategory.MONSTER) {
            return ((NaturalSpawnerSpawnStateAccessor) spawnState)
                    .better_content_fixes$canSpawnForCategory(category, chunkPos);
        }

        if (((NaturalSpawnerSpawnStateAccessor) spawnState)
                .better_content_fixes$canSpawnForCategory(category, chunkPos)) {
            return true;
        }

        final int expandedCap = category.getMaxInstancesPerChunk()
                * spawnState.getSpawnableChunkCount()
                * BcFixesConfig.mobsVerticalSpawnMaxMultiplier();
        return spawnState.getMobCategoryCounts().getOrDefault(category, 0) < expandedCap;
    }

    @Redirect(
            method = "spawnForChunk",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/NaturalSpawner;spawnCategoryForChunk(Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/LevelChunk;Lnet/minecraft/world/level/NaturalSpawner$SpawnPredicate;Lnet/minecraft/world/level/NaturalSpawner$AfterSpawnCallback;)V"),
            require = 1)
    private static void better_content_fixes$runVerticalMonsterPasses(
            final MobCategory category,
            final ServerLevel level,
            final LevelChunk chunk,
            final NaturalSpawner.SpawnPredicate predicate,
            final NaturalSpawner.AfterSpawnCallback callback) {
        final int passes = BcFixesConfig.mobsVerticalNaturalSpawnScaling()
                && level.dimension().equals(Level.OVERWORLD)
                && category == MobCategory.MONSTER
                ? BcFixesConfig.mobsVerticalSpawnMaxMultiplier()
                : 1;
        for (int pass = 0; pass < passes; pass++) {
            better_content_fixes$invokeSpawnCategoryForChunk(category, level, chunk, predicate, callback);
        }
    }
}
