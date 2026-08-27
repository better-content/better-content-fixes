package com.bettercontent.bettercontentfixes.compat;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

final class VerticalNaturalSpawnScalingTest {
    private static final float DELTA = 0.0001F;

    @Test
    void keepsTheMinimumBandThroughSeaLevelPlus128() {
        assertEquals(1.0F, VerticalNaturalSpawnScaling.multiplier(63, 63, -64, 320, 128, 8), DELTA);
        assertEquals(1.0F, VerticalNaturalSpawnScaling.multiplier(191, 63, -64, 320, 128, 8), DELTA);
    }

    @Test
    void reachesEightAtBothOverworldBuildLimits() {
        assertEquals(8.0F, VerticalNaturalSpawnScaling.multiplier(-64, 63, -64, 320, 128, 8), DELTA);
        assertEquals(8.0F, VerticalNaturalSpawnScaling.multiplier(319, 63, -64, 320, 128, 8), DELTA);
    }

    @Test
    void derivesPassAcceptanceFromTheConfiguredMaximum() {
        assertEquals(0.125F, VerticalNaturalSpawnScaling.passAcceptanceChance(1.0F, 8), DELTA);
        assertEquals(1.0F, VerticalNaturalSpawnScaling.passAcceptanceChance(8.0F, 8), DELTA);
    }
}
