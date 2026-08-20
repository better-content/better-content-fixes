package com.bettercontent.bettercontentfixes.client;

import com.bettercontent.bettercontentfixes.BetterContentFixes;
import com.bettercontent.bettercontentfixes.config.BcFixesClientConfig;
import net.combatroll.client.Keybindings;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BetterContentFixes.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CombatRollDoubleTapClient {
    private static final DirectionalDoubleTapTracker TRACKER = new DirectionalDoubleTapTracker();
    private static boolean syntheticRollDown;
    private static boolean previousRollDown;

    private CombatRollDoubleTapClient() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            restoreRollKey();
            return;
        }

        restoreRollKey();
        Minecraft minecraft = Minecraft.getInstance();
        if (!canTrack(minecraft)) {
            TRACKER.reset();
            return;
        }

        boolean roll = TRACKER.update(
                minecraft.options.keyUp.isDown(),
                minecraft.options.keyDown.isDown(),
                minecraft.options.keyLeft.isDown(),
                minecraft.options.keyRight.isDown(),
                BcFixesClientConfig.combatRollDoubleTapWindowTicks());
        if (roll) {
            KeyMapping rollKey = Keybindings.roll;
            previousRollDown = rollKey.isDown();
            rollKey.setDown(true);
            syntheticRollDown = true;
        }
    }

    private static boolean canTrack(Minecraft minecraft) {
        return ModList.get().isLoaded("combatroll")
                && BcFixesClientConfig.combatRollDirectionalDoubleTapEnabled()
                && minecraft.player != null
                && minecraft.level != null
                && minecraft.screen == null
                && !minecraft.isPaused()
                && minecraft.isWindowActive();
    }

    private static void restoreRollKey() {
        if (!syntheticRollDown) {
            return;
        }
        Keybindings.roll.setDown(previousRollDown);
        syntheticRollDown = false;
    }
}
