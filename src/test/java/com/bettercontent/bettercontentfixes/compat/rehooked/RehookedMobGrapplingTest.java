package com.bettercontent.bettercontentfixes.compat.rehooked;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class RehookedMobGrapplingTest {
    private static final double EPSILON = 1.0E-9D;

    @Test
    void equalWeightsSplitPullEvenly() {
        final RehookedMobGrappling.PullShares shares = RehookedMobGrappling.pullShares(1.0D, 1.0D);

        assertEquals(0.5D, shares.playerShare(), EPSILON);
        assertEquals(0.5D, shares.mobShare(), EPSILON);
        assertEquals(1.0D, shares.playerShare() + shares.mobShare(), EPSILON);
    }

    @Test
    void largerResistantMobPullsPlayerMore() {
        final double playerWeight = RehookedMobGrappling.effectiveWeight(0.6D, 1.8D, 0.0D);
        final double smallMobWeight = RehookedMobGrappling.effectiveWeight(0.4D, 0.7D, 0.0D);
        final double largeResistantMobWeight = RehookedMobGrappling.effectiveWeight(1.4D, 2.7D, 1.0D);

        final RehookedMobGrappling.PullShares smallShares =
                RehookedMobGrappling.pullShares(playerWeight, smallMobWeight);
        final RehookedMobGrappling.PullShares largeShares =
                RehookedMobGrappling.pullShares(playerWeight, largeResistantMobWeight);

        assertTrue(smallShares.mobShare() > smallShares.playerShare());
        assertTrue(largeShares.playerShare() > largeShares.mobShare());
        assertTrue(largeShares.playerShare() > smallShares.playerShare());
    }

    @Test
    void effectiveWeightClampsInvalidAndExtremeInputs() {
        assertEquals(
                RehookedMobGrappling.MIN_EFFECTIVE_WEIGHT,
                RehookedMobGrappling.effectiveWeight(-1.0D, 0.0D, -5.0D),
                EPSILON);
        assertEquals(
                RehookedMobGrappling.MAX_EFFECTIVE_WEIGHT,
                RehookedMobGrappling.effectiveWeight(100.0D, 100.0D, 50.0D),
                EPSILON);
    }
}
