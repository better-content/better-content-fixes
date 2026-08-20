package com.bettercontent.bettercontentfixes.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class DirectionalDoubleTapTrackerTest {
    @Test
    void secondPressOfSameDirectionWithinInclusiveWindowTriggers() {
        DirectionalDoubleTapTracker tracker = new DirectionalDoubleTapTracker();

        assertFalse(tracker.update(true, false, false, false, 7));
        assertFalse(tracker.update(false, false, false, false, 7));
        for (int tick = 0; tick < 5; tick++) {
            assertFalse(tracker.update(false, false, false, false, 7));
        }
        assertTrue(tracker.update(true, false, false, false, 7));
    }

    @Test
    void heldKeyAndDifferentDirectionDoNotTrigger() {
        DirectionalDoubleTapTracker tracker = new DirectionalDoubleTapTracker();

        assertFalse(tracker.update(true, false, false, false, 7));
        assertFalse(tracker.update(true, false, false, false, 7));
        assertFalse(tracker.update(false, false, false, false, 7));
        assertFalse(tracker.update(false, false, true, false, 7));
    }

    @Test
    void expiredTapAndResetDoNotTrigger() {
        DirectionalDoubleTapTracker tracker = new DirectionalDoubleTapTracker();

        assertFalse(tracker.update(false, false, false, true, 2));
        assertFalse(tracker.update(false, false, false, false, 2));
        assertFalse(tracker.update(false, false, false, false, 2));
        assertFalse(tracker.update(false, false, false, true, 2));
        tracker.reset();
        assertFalse(tracker.update(false, false, false, true, 2));
    }

    @Test
    void simultaneousDiagonalSecondPressCoalescesIntoOneTrigger() {
        DirectionalDoubleTapTracker tracker = new DirectionalDoubleTapTracker();

        assertFalse(tracker.update(true, false, true, false, 7));
        assertFalse(tracker.update(false, false, false, false, 7));
        assertTrue(tracker.update(true, false, true, false, 7));
        assertFalse(tracker.update(true, false, true, false, 7));
    }

    @Test
    void directionalKeysProduceSignedCombatRollImpulses() {
        assertEquals(1.0F, DirectionalDoubleTapTracker.axisImpulse(true, false));
        assertEquals(-1.0F, DirectionalDoubleTapTracker.axisImpulse(false, true));
        assertEquals(0.0F, DirectionalDoubleTapTracker.axisImpulse(false, false));
        assertEquals(0.0F, DirectionalDoubleTapTracker.axisImpulse(true, true));
    }
}
