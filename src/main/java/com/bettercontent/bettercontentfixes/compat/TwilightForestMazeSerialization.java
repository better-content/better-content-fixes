package com.bettercontent.bettercontentfixes.compat;

import java.util.function.Predicate;

/** Serializes maze placement only among mazes that share one Twilight Forest random source. */
public final class TwilightForestMazeSerialization {
    private TwilightForestMazeSerialization() {
    }

    public static boolean dependenciesAvailable(
            final boolean enabled,
            final Predicate<String> modLoaded) {
        return enabled && modLoaded.test("twilightforest") && modLoaded.test("c2me");
    }

    public static void runSerialized(final Object randomSource, final Runnable operation) {
        synchronized (randomSource) {
            operation.run();
        }
    }
}
