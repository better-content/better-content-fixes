package com.bettercontent.bettercontentfixes.mixin.minecraft;

import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.NaturalSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(NaturalSpawner.SpawnState.class)
public interface NaturalSpawnerSpawnStateAccessor {
    @Invoker("canSpawnForCategory")
    boolean better_content_fixes$canSpawnForCategory(MobCategory category, ChunkPos chunkPos);
}
