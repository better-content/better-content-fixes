package com.bettercontent.bettercontentfixes.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

final class BcFixesClientConfigTest {
    @Test
    void secondsConvertToTwentyTickTiming() {
        assertEquals(100, BcFixesClientConfig.secondsToTicks(5.0D));
        assertEquals(10, BcFixesClientConfig.secondsToTicks(0.5D));
        assertEquals(0, BcFixesClientConfig.secondsToTicks(0.0D));
    }
}
