package com.bettercontent.bettercontentfixes.client.hud;

import dev.ghen.thirst.foundation.common.capability.IThirst;
import dev.ghen.thirst.foundation.common.capability.ModCapabilities;
import net.minecraft.client.player.LocalPlayer;

final class ThirstHudTelemetry {
    static final int MAX_THIRST = 20;

    private ThirstHudTelemetry() {
    }

    static int sample(final LocalPlayer player) {
        final IThirst thirst = player.getCapability(ModCapabilities.PLAYER_THIRST).orElse(null);
        return thirst == null ? MAX_THIRST : thirst.getThirst();
    }
}
