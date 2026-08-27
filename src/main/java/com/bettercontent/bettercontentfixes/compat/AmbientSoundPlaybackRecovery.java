package com.bettercontent.bettercontentfixes.compat;

public final class AmbientSoundPlaybackRecovery {
    private AmbientSoundPlaybackRecovery() {
    }

    public static boolean shouldRetireUnstarted(final boolean active, final boolean playedOnce) {
        return !active && !playedOnce;
    }
}
