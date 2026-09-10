package com.bettercontent.bettercontentfixes.client;

import com.bettercontent.dynamicsurvivalhud.client.api.DynamicSurvivalHudClientApi;

/** Loaded only after Forge confirms the exact Dynamic Survival HUD client API is present. */
final class DynamicHudSneakIntegration {
    private DynamicHudSneakIntegration() {
    }

    static void setPhysicalSneakDown(final boolean down) {
        DynamicSurvivalHudClientApi.setPhysicalSneakDown(down);
    }

    static void cancelPhysicalSneak() {
        DynamicSurvivalHudClientApi.cancelPhysicalSneak();
    }
}
