package com.bettercontent.bettercontentfixes.compat.rehooked;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class IntroHookBehaviorPolicyTest {
    @Test
    void ratchetStartsWithThreePullTicksThenHoldsForTwoAndRepeats() {
        assertTrue(IntroHookBehaviorPolicy.ratchetPulls(0));
        assertTrue(IntroHookBehaviorPolicy.ratchetPulls(1));
        assertTrue(IntroHookBehaviorPolicy.ratchetPulls(2));
        assertFalse(IntroHookBehaviorPolicy.ratchetPulls(3));
        assertFalse(IntroHookBehaviorPolicy.ratchetPulls(4));
        assertTrue(IntroHookBehaviorPolicy.ratchetPulls(5));
        assertTrue(IntroHookBehaviorPolicy.ratchetPulls(7));
        assertFalse(IntroHookBehaviorPolicy.ratchetPulls(8));
        assertFalse(IntroHookBehaviorPolicy.ratchetPulls(9));
    }

    @Test
    void cadenceUsesStableFloorModuloAtNegativeBoundaries() {
        assertFalse(IntroHookBehaviorPolicy.ratchetPulls(-1));
        assertFalse(IntroHookBehaviorPolicy.ratchetPulls(-2));
        assertTrue(IntroHookBehaviorPolicy.ratchetPulls(-3));
        assertTrue(IntroHookBehaviorPolicy.ratchetPulls(-5));
    }

    @Test
    void climbingVineKeepsFullPullOnLeavesAndLosesForceOnOtherAnchors() {
        assertEquals(1.0D, IntroHookBehaviorPolicy.climbingVinePullFactor(true));
        assertEquals(0.65D, IntroHookBehaviorPolicy.climbingVinePullFactor(false));
        assertTrue(IntroHookBehaviorPolicy.VINE_NON_FOLIAGE_PULL_FACTOR < 1.0D);
    }

    @Test
    void grapnelCatchesOnlyWithinTheSmallEdgeForgivenessRadius() {
        assertTrue(IntroHookBehaviorPolicy.shouldForgivingCatch(0.0D));
        assertTrue(IntroHookBehaviorPolicy.shouldForgivingCatch(0.04D));
        assertFalse(IntroHookBehaviorPolicy.shouldForgivingCatch(0.040001D));
        assertFalse(IntroHookBehaviorPolicy.shouldForgivingCatch(-0.001D));
    }

    @Test
    void anglersGaffBoardsOnlyAtTheBoatAndInsideTheBoundedApproachDistance() {
        assertTrue(IntroHookBehaviorPolicy.shouldBoardBoat(0.0D));
        assertTrue(IntroHookBehaviorPolicy.shouldBoardBoat(6.25D));
        assertFalse(IntroHookBehaviorPolicy.shouldBoardBoat(6.2501D));
        assertFalse(IntroHookBehaviorPolicy.shouldBoardBoat(-0.001D));
    }

    @Test
    void soapGuidanceSoftensVerticalPullAndPreservesItsDirection() {
        assertEquals(2.2D, IntroHookBehaviorPolicy.soapGuidedVerticalComponent(4.0D), 1.0E-9D);
        assertEquals(-2.2D, IntroHookBehaviorPolicy.soapGuidedVerticalComponent(-4.0D), 1.0E-9D);
        assertEquals(0.0D, IntroHookBehaviorPolicy.soapGuidedVerticalComponent(0.0D), 1.0E-9D);
    }
}
