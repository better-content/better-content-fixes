package com.bettercontent.bettercontentfixes.compat.rehooked;

/** Pure cadence for the Ratchet Reel's repeating pull-and-hold cycle. */
public final class IntroHookBehaviorPolicy {
    public static final int RATCHET_PULL_TICKS = 3;
    public static final int RATCHET_HOLD_TICKS = 2;
    public static final int RATCHET_PERIOD_TICKS = RATCHET_PULL_TICKS + RATCHET_HOLD_TICKS;
    public static final double VINE_FOLIAGE_PULL_FACTOR = 1.0D;
    public static final double VINE_NON_FOLIAGE_PULL_FACTOR = 0.65D;
    public static final double GRAPNEL_EDGE_CATCH_RADIUS = 0.2D;
    public static final double GAFF_BOARDING_DISTANCE = 2.5D;
    public static final double SOAP_VERTICAL_PULL_FACTOR = 0.55D;

    private IntroHookBehaviorPolicy() {
    }

    public static boolean ratchetPulls(final long gameTick) {
        return Math.floorMod(gameTick, RATCHET_PERIOD_TICKS) < RATCHET_PULL_TICKS;
    }

    public static double climbingVinePullFactor(final boolean foliageAnchor) {
        return foliageAnchor ? VINE_FOLIAGE_PULL_FACTOR : VINE_NON_FOLIAGE_PULL_FACTOR;
    }

    public static boolean shouldForgivingCatch(final double distanceSquared) {
        return distanceSquared >= 0.0D
                && distanceSquared <= GRAPNEL_EDGE_CATCH_RADIUS * GRAPNEL_EDGE_CATCH_RADIUS;
    }

    public static boolean shouldBoardBoat(final double distanceSquared) {
        return distanceSquared >= 0.0D
                && distanceSquared <= GAFF_BOARDING_DISTANCE * GAFF_BOARDING_DISTANCE;
    }

    public static double soapGuidedVerticalComponent(final double verticalPull) {
        return verticalPull * SOAP_VERTICAL_PULL_FACTOR;
    }
}
