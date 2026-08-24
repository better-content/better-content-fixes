package com.bettercontent.bettercontentfixes.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Removes only vanilla decorative grass while a living entity physically moves through it. */
public final class DecorativeVegetationTrample {
    private static final double MINIMUM_HORIZONTAL_MOVEMENT_SQUARED = 1.0E-6D;

    private DecorativeVegetationTrample() {
    }

    @SubscribeEvent
    public static void onLivingTick(final LivingEvent.LivingTickEvent event) {
        final LivingEntity entity = event.getEntity();
        trampleIfMoving(entity.level(), entity.blockPosition(), entity.getDeltaMovement());
    }

    public static boolean trampleIfMoving(final Level level, final BlockPos pos, final Vec3 movement) {
        if (level.isClientSide || movement.horizontalDistanceSqr() <= MINIMUM_HORIZONTAL_MOVEMENT_SQUARED) {
            return false;
        }
        if (!isTrampleable(level.getBlockState(pos))) {
            return false;
        }
        return level.destroyBlock(pos, false);
    }

    static boolean isTrampleable(final BlockState state) {
        return state.is(Blocks.GRASS)
                || state.is(Blocks.TALL_GRASS)
                || state.is(Blocks.FERN)
                || state.is(Blocks.LARGE_FERN);
    }
}
