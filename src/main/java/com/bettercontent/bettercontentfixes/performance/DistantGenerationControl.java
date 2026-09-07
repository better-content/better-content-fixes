package com.bettercontent.bettercontentfixes.performance;

public interface DistantGenerationControl {
    boolean isAvailable();

    boolean baseEnabled();

    boolean overrideActive();

    boolean pause();

    void resume();

    String description();
}
