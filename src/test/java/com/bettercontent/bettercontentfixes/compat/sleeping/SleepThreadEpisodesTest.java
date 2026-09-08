package com.bettercontent.bettercontentfixes.compat.sleeping;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

final class SleepThreadEpisodesTest {
    @Test
    void detectsOnlyTimelapseBoundaries() {
        assertEquals(SleepThreadEpisodes.Transition.START, SleepThreadEpisodes.transition(false, true));
        assertEquals(SleepThreadEpisodes.Transition.FINISH, SleepThreadEpisodes.transition(true, false));
        assertEquals(SleepThreadEpisodes.Transition.NONE, SleepThreadEpisodes.transition(false, false));
        assertEquals(SleepThreadEpisodes.Transition.NONE, SleepThreadEpisodes.transition(true, true));
    }

    @Test
    void reusesOneValidEpisodeToken() {
        assertEquals("active", SleepThreadEpisodes.episodeToken("active", "persisted", "generated"));
        assertEquals("persisted", SleepThreadEpisodes.episodeToken(null, "persisted", "generated"));
        assertEquals("generated", SleepThreadEpisodes.episodeToken(null, "", "generated"));
    }

    @Test
    void exposesTheExpectedThreadsDataContract() {
        assertEquals("sleep_is_not_an_anchor", SleepThreadEpisodes.THREAD_ID);
        assertEquals("sleep_started", SleepThreadEpisodes.START_TYPE);
        assertEquals("night", SleepThreadEpisodes.START_VALUE);
        assertEquals("sleep_finished", SleepThreadEpisodes.FINISH_TYPE);
        assertEquals("simulated_time", SleepThreadEpisodes.FINISH_VALUE);
    }
}
