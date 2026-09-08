package com.bettercontent.bettercontentfixes.learning;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

final class DistinctControlEpisodeTest {
    @Test
    void startsWhenNoPersistedEpisodeExists() {
        assertEquals(
                DistinctControlEpisode.Transition.FIRST,
                DistinctControlEpisode.advance("", "", "parcool:dodge"));
    }

    @Test
    void repeatingTheFirstActionDoesNotComplete() {
        assertEquals(
                DistinctControlEpisode.Transition.REPEAT,
                DistinctControlEpisode.advance("episode-1", "parcool:dodge", "parcool:dodge"));
    }

    @Test
    void aDifferentActionCompletesTheEpisode() {
        assertEquals(
                DistinctControlEpisode.Transition.DISTINCT_SECOND,
                DistinctControlEpisode.advance("episode-1", "parcool:dodge", "parcool:vault"));
    }
}
