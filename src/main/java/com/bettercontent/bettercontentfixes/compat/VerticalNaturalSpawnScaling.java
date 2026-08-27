package com.bettercontent.bettercontentfixes.compat;

import net.minecraft.util.Mth;

/** Pure Y-to-rate policy for the extra Overworld natural monster spawn passes. */
public final class VerticalNaturalSpawnScaling {
    public static final int MINIMUM_MULTIPLIER = 1;

    private VerticalNaturalSpawnScaling() {
    }

    public static float multiplier(
            final int spawnY,
            final int seaLevel,
            final int minBuildHeight,
            final int maxBuildHeightExclusive,
            final int upperMinimumRange,
            final int maximumMultiplier) {
        final int cappedMaximum = Math.max(MINIMUM_MULTIPLIER, maximumMultiplier);
        final int upperMinimumY = Math.min(
                maxBuildHeightExclusive - 1,
                seaLevel + Math.max(0, upperMinimumRange));

        if (spawnY >= seaLevel && spawnY <= upperMinimumY) {
            return MINIMUM_MULTIPLIER;
        }

        if (spawnY < seaLevel) {
            return interpolate(
                    spawnY,
                    minBuildHeight,
                    seaLevel,
                    cappedMaximum,
                    true);
        }

        return interpolate(
                spawnY,
                upperMinimumY,
                maxBuildHeightExclusive - 1,
                cappedMaximum,
                false);
    }

    public static float passAcceptanceChance(final float multiplier, final int maximumMultiplier) {
        return Mth.clamp(multiplier / Math.max(MINIMUM_MULTIPLIER, maximumMultiplier), 0.0F, 1.0F);
    }

    private static float interpolate(
            final int y,
            final int lowerY,
            final int upperY,
            final int maximumMultiplier,
            final boolean descending) {
        if (upperY <= lowerY) {
            return maximumMultiplier;
        }
        final float progress = descending
                ? (float) (upperY - y) / (upperY - lowerY)
                : (float) (y - lowerY) / (upperY - lowerY);
        return Mth.clamp(
                MINIMUM_MULTIPLIER + (maximumMultiplier - MINIMUM_MULTIPLIER) * progress,
                MINIMUM_MULTIPLIER,
                maximumMultiplier);
    }
}
