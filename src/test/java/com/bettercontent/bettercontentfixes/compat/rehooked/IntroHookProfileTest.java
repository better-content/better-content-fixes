package com.bettercontent.bettercontentfixes.compat.rehooked;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IntroHookProfileTest {
    @Test
    void sixDistinctLootPrototypesRetainNativeMobilityLimits() {
        assertEquals(6, IntroHookProfile.ALL.size());
        assertEquals(6, new HashSet<>(IntroHookProfile.ALL.stream().map(IntroHookProfile::itemId).toList()).size());
        assertEquals(6, new HashSet<>(IntroHookProfile.ALL.stream().map(IntroHookProfile::hookType).toList()).size());
        assertTrue(IntroHookProfile.ALL.stream().allMatch(IntroHookProfile::staysBelowIronAndAtOrBelowWood));
        assertTrue(IntroHookProfile.ALL.stream().noneMatch(profile -> profile.range() > IntroHookProfile.IRON_RANGE
                || profile.travelSpeed() > IntroHookProfile.IRON_TRAVEL_SPEED
                || profile.pullSpeed() > IntroHookProfile.IRON_PULL_SPEED));
    }

    @Test
    void profilesUseDistinctOrdinaryHookLimitsAndDoNotGrantExtraPullPower() {
        final Set<String> profiles = new HashSet<>(IntroHookProfile.ALL.stream()
                .map(profile -> profile.count() + ":" + profile.range() + ":" + profile.travelSpeed() + ":" + profile.pullSpeed())
                .toList());
        assertEquals(6, profiles.size());
        assertFalse(IntroHookProfile.ALL.stream().anyMatch(profile -> profile.travelSpeed() > 0.0F
                && profile.pullSpeed() > IntroHookProfile.WOOD_PULL_SPEED));
    }
}
