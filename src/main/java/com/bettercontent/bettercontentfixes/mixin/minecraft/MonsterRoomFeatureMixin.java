package com.bettercontent.bettercontentfixes.mixin.minecraft;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.levelgen.feature.MonsterRoomFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Restores the spawner block entity when concurrent feature placement has not instantiated it yet. */
@Mixin(MonsterRoomFeature.class)
public abstract class MonsterRoomFeatureMixin {
    @Redirect(
            method = "place",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/WorldGenLevel;getBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;"))
    private BlockEntity betterContentFixes$recoverDeferredSpawnerEntity(
            final WorldGenLevel level, final BlockPos position) {
        final BlockEntity existing = level.getBlockEntity(position);
        if (existing != null || !(level instanceof WorldGenRegion region)) return existing;

        final var state = level.getBlockState(position);
        if (!state.is(Blocks.SPAWNER)) return null;

        final SpawnerBlockEntity recovered = new SpawnerBlockEntity(position, state);
        region.getChunk(position).setBlockEntity(recovered);
        return recovered;
    }
}
