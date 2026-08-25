package com.bettercontent.bettercontentfixes.client.hud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class HudFadeStateTest {
    @Test
    void holdsThenFadesLinearly() {
        final HudFadeState state = new HudFadeState();
        tick(state, 99);
        assertEquals(1.0F, state.alpha(1.0F, 100, 10, false));

        state.tick();
        assertEquals(1.0F, state.alpha(0.0F, 100, 10, false));
        assertEquals(0.95F, state.alpha(0.5F, 100, 10, false), 0.0001F);

        tick(state, 5);
        assertEquals(0.5F, state.alpha(0.0F, 100, 10, false), 0.0001F);
        tick(state, 5);
        assertEquals(0.0F, state.alpha(0.0F, 100, 10, false));
    }

    @Test
    void revealRestartsTheFullHold() {
        final HudFadeState state = new HudFadeState();
        tick(state, 107);
        assertTrue(state.alpha(0.0F, 100, 10, false) < 1.0F);

        state.reveal();
        assertEquals(0, state.ageTicks());
        assertEquals(1.0F, state.alpha(0.0F, 100, 10, false));
    }

    @Test
    void dangerPinsVisibilityAndRecoveryRestartsTheHold() {
        final HudFadeState state = new HudFadeState();
        tick(state, 120);
        state.setDangerous(true);
        assertTrue(state.isDangerous());
        assertEquals(1.0F, state.alpha(0.5F, 100, 10, false));

        tick(state, 40);
        state.setDangerous(false);
        assertFalse(state.isDangerous());
        assertEquals(0, state.ageTicks());
        assertEquals(1.0F, state.alpha(0.0F, 100, 10, false));
    }

    @Test
    void peekOverridesHiddenStateWithoutMutatingItsTimer() {
        final HudFadeState state = new HudFadeState();
        tick(state, 120);
        final int hiddenAge = state.ageTicks();

        assertEquals(1.0F, state.alpha(0.0F, 100, 10, true));
        assertEquals(hiddenAge, state.ageTicks());
        assertEquals(0.0F, state.alpha(0.0F, 100, 10, false));
    }

    @Test
    void zeroLengthFadeHidesImmediatelyAfterHold() {
        final HudFadeState state = new HudFadeState();
        tick(state, 100);
        assertEquals(0.0F, state.alpha(0.0F, 100, 0, false));
    }

    private static void tick(final HudFadeState state, final int count) {
        for (int tick = 0; tick < count; tick++) {
            state.tick();
        }
    }
}
