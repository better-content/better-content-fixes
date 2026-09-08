package com.bettercontent.bettercontentfixes.learning;

import java.util.Objects;

/** Pure transition policy for a two-distinct-control learning episode. */
final class DistinctControlEpisode {
    private DistinctControlEpisode() {
    }

    static Transition advance(final String token, final String firstAction, final String currentAction) {
        Objects.requireNonNull(currentAction, "currentAction");
        if (token == null || token.isBlank() || firstAction == null || firstAction.isBlank()) {
            return Transition.FIRST;
        }
        return firstAction.equals(currentAction) ? Transition.REPEAT : Transition.DISTINCT_SECOND;
    }

    enum Transition {
        FIRST,
        REPEAT,
        DISTINCT_SECOND
    }
}
