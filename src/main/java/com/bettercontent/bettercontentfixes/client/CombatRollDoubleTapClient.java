package com.bettercontent.bettercontentfixes.client;

import com.bettercontent.bettercontentfixes.BetterContentFixes;
import com.bettercontent.bettercontentfixes.config.BcFixesClientConfig;
import net.combatroll.client.Keybindings;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
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
        handleToggle(minecraft);
        if (!canTrack(minecraft)) {
            TRACKER.reset();
            return;
        }

        boolean forward = minecraft.options.keyUp.isDown();
        boolean backward = minecraft.options.keyDown.isDown();
        boolean left = minecraft.options.keyLeft.isDown();
        boolean right = minecraft.options.keyRight.isDown();
        boolean roll = TRACKER.update(
                forward,
                backward,
                left,
                right,
                BcFixesClientConfig.combatRollDoubleTapWindowTicks());
        if (roll) {
            // Combat Roll samples LocalPlayer.input later in this client tick, before vanilla refreshes it from the keys.
            minecraft.player.input.forwardImpulse = DirectionalDoubleTapTracker.axisImpulse(forward, backward);
            minecraft.player.input.leftImpulse = DirectionalDoubleTapTracker.axisImpulse(left, right);
            KeyMapping rollKey = Keybindings.roll;
            previousRollDown = rollKey.isDown();
            rollKey.setDown(true);
            syntheticRollDown = true;
        }
    }

    private static void handleToggle(Minecraft minecraft) {
        if (minecraft.player == null || !BcFixesKeyMappings.TOGGLE_DOUBLE_TAP_DASH.consumeClick()) {
            return;
        }

        boolean enabled = !BcFixesClientConfig.combatRollDirectionalDoubleTapEnabled();
        BcFixesClientConfig.COMBAT_ROLL_DIRECTIONAL_DOUBLE_TAP_ENABLED.set(enabled);
        BcFixesClientConfig.COMBAT_ROLL_DIRECTIONAL_DOUBLE_TAP_ENABLED.save();
        TRACKER.reset();
        minecraft.player.displayClientMessage(Component.translatable(enabled
                ? "message.better_content_fixes.double_tap_dash.enabled"
                : "message.better_content_fixes.double_tap_dash.disabled"), true);
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
