package io.github.bcfixes.compat;

import io.github.bcfixes.config.BcFixesConfig;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class FarmlandTrampleProtection {
    private FarmlandTrampleProtection() {
    }

    @SubscribeEvent
    public static void onFarmlandTrample(final BlockEvent.FarmlandTrampleEvent event) {
        if (!BcFixesConfig.farmlandPreventTrample()) {
            return;
        }

        event.setCanceled(true);
    }
}
