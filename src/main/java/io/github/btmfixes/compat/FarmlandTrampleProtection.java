package io.github.btmfixes.compat;

import io.github.btmfixes.config.BtmFixesConfig;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class FarmlandTrampleProtection {
    private FarmlandTrampleProtection() {
    }

    @SubscribeEvent
    public static void onFarmlandTrample(final BlockEvent.FarmlandTrampleEvent event) {
        if (!BtmFixesConfig.farmlandPreventTrample()) {
            return;
        }

        event.setCanceled(true);
    }
}
