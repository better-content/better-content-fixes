package com.bettercontent.bettercontentfixes.mixin.minecraft;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.levelgen.feature.MonsterRoomFeature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Restores the spawner block entity when concurrent feature placement has not instantiated it yet. */
@Mixin(MonsterRoomFeature.class)
public abstract class MonsterRoomFeatureMixin {
    @Redirect(
            method = {"place", "m_142674_"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/WorldGenLevel;getBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;"),
            require = 1)
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

    /**
     * Vanilla logs an error and reports success when its guarded spawner write was rejected, such
     * as a dungeon origin on the protected bedrock floor. Preserve the protected block and report
     * the feature failure accurately instead of treating the expected missing entity as corruption.
     */
    @Inject(
            method = {"place", "m_142674_"},
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/slf4j/Logger;error(Ljava/lang/String;[Ljava/lang/Object;)V"),
            cancellable = true,
            require = 1)
    private void betterContentFixes$rejectDungeonWithoutSpawner(
            final FeaturePlaceContext<NoneFeatureConfiguration> context,
            final CallbackInfoReturnable<Boolean> callback) {
        callback.setReturnValue(false);
    }
}
