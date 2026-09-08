package com.bettercontent.bettercontentfixes.compat;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/** Fits Fallout's feature-placed templates inside WorldGenRegion's writable 3x3 chunk envelope. */
public final class FalloutStructurePlacementBounds {
    public static final int WRITABLE_ENVELOPE_BLOCKS = 48;

    private FalloutStructurePlacementBounds() {
    }

    public static Optional<Placement> fit(
            final BoundingBox templateBounds,
            final BlockPos position,
            final BlockPos pivot,
            final ChunkPos centerChunk
    ) {
        if (templateBounds.getXSpan() > WRITABLE_ENVELOPE_BLOCKS
                || templateBounds.getZSpan() > WRITABLE_ENVELOPE_BLOCKS) {
            return Optional.empty();
        }

        final int envelopeMinX = centerChunk.getMinBlockX() - 16;
        final int envelopeMaxX = centerChunk.getMaxBlockX() + 16;
        final int envelopeMinZ = centerChunk.getMinBlockZ() - 16;
        final int envelopeMaxZ = centerChunk.getMaxBlockZ() + 16;
        final int shiftX = minimalShift(
                templateBounds.minX(), templateBounds.maxX(), envelopeMinX, envelopeMaxX);
        final int shiftZ = minimalShift(
                templateBounds.minZ(), templateBounds.maxZ(), envelopeMinZ, envelopeMaxZ);

        return Optional.of(new Placement(
                position.offset(shiftX, 0, shiftZ),
                pivot.offset(shiftX, 0, shiftZ),
                shiftX,
                shiftZ));
    }

    private static int minimalShift(
            final int templateMin,
            final int templateMax,
            final int envelopeMin,
            final int envelopeMax
    ) {
        if (templateMin < envelopeMin) {
            return envelopeMin - templateMin;
        }
        if (templateMax > envelopeMax) {
            return envelopeMax - templateMax;
        }
        return 0;
    }

    public record Placement(BlockPos position, BlockPos pivot, int shiftX, int shiftZ) {
    }
}
