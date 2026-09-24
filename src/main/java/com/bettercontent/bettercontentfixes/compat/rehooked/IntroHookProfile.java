package com.bettercontent.bettercontentfixes.compat.rehooked;

import java.util.List;

/** Fixed early-hook profiles. Their ordinary mobility stays below the iron hook. */
public record IntroHookProfile(
        String itemId,
        String hookType,
        int count,
        float range,
        float travelSpeed,
        float pullSpeed
) {
    public static final float WOOD_RANGE = 16.0F;
    public static final float WOOD_TRAVEL_SPEED = 12.0F;
    public static final float WOOD_PULL_SPEED = 6.0F;
    public static final int IRON_COUNT = 2;
    public static final float IRON_RANGE = 32.0F;
    public static final float IRON_TRAVEL_SPEED = 24.0F;
    public static final float IRON_PULL_SPEED = 12.0F;

    public static final List<IntroHookProfile> ALL = List.of(
            new IntroHookProfile("soap_on_a_rope", "soap_on_a_rope", 1, 16.0F, 12.0F, 4.0F),
            new IntroHookProfile("block_and_tackle", "block_and_tackle", 2, 16.0F, 8.0F, 3.0F),
            new IntroHookProfile("grapnel_bundle", "grapnel_bundle", 2, 16.0F, 10.0F, 4.0F),
            new IntroHookProfile("climbing_vine", "climbing_vine", 1, 12.0F, 8.0F, 3.0F),
            new IntroHookProfile("ratchet_reel", "ratchet_reel", 1, 16.0F, 8.0F, 3.0F),
            new IntroHookProfile("anglers_gaff", "anglers_gaff", 1, 12.0F, 10.0F, 4.0F)
    );

    public boolean staysBelowIronAndAtOrBelowWood() {
        return count <= IRON_COUNT
                && range <= WOOD_RANGE
                && travelSpeed <= WOOD_TRAVEL_SPEED
                && pullSpeed <= WOOD_PULL_SPEED;
    }
}
