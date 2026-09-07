package com.bettercontent.bettercontentfixes.client;

import com.bettercontent.bettercontentfixes.BetterContentFixes;
import com.bettercontent.bettercontentfixes.config.BcFixesClientConfig;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = BetterContentFixes.MOD_ID,
        value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ParCoolDirectionalDodgeClient {
    static final String PARCOOL_DODGE_MAPPING = "key.parcool.Dodge";

    private static final DirectionalDoubleTapTracker TRACKER = new DirectionalDoubleTapTracker();
    private static KeyMapping dodgeMapping;
    private static boolean syntheticDodgeDown;
    private static boolean previousDodgeDown;
    private static LocalPlayer trackedPlayer;
    private static ClientLevel trackedLevel;

    private ParCoolDirectionalDodgeClient() {
    }

    @SubscribeEvent
    public static void onClientTick(final TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            restoreDodgeKey();
            return;
        }

        restoreDodgeKey();
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != trackedPlayer || minecraft.level != trackedLevel) {
            TRACKER.reset();
            trackedPlayer = minecraft.player;
            trackedLevel = minecraft.level;
        }
        if (!canTrack(minecraft)) {
            TRACKER.reset();
            return;
        }

        final boolean forward = minecraft.options.keyUp.isDown();
        final boolean backward = minecraft.options.keyDown.isDown();
        final boolean left = minecraft.options.keyLeft.isDown();
        final boolean right = minecraft.options.keyRight.isDown();
        final boolean triggered = TRACKER.update(
                forward,
                backward,
                left,
                right,
                BcFixesClientConfig.parcoolDoubleTapWindowTicks());
        if (!triggered) {
            return;
        }

        final float forwardImpulse = DirectionalDoubleTapTracker.axisImpulse(forward, backward);
        final float leftImpulse = DirectionalDoubleTapTracker.axisImpulse(left, right);
        if (forwardImpulse == 0.0F && leftImpulse == 0.0F) {
            return;
        }

        minecraft.player.input.forwardImpulse = forwardImpulse;
        minecraft.player.input.leftImpulse = leftImpulse;
        final KeyMapping dodge = findDodgeMapping(minecraft);
        if (dodge == null) {
            return;
        }
        previousDodgeDown = dodge.isDown();
        dodge.setDown(true);
        syntheticDodgeDown = true;
    }

    private static boolean canTrack(final Minecraft minecraft) {
        return BcFixesClientConfig.parcoolDirectionalDoubleTapDodge()
                && exactVersion("parcool", "3.4.3.3")
                && minecraft.player != null
                && minecraft.level != null
                && minecraft.screen == null
                && !minecraft.isPaused()
                && minecraft.isWindowActive()
                && !minecraft.player.isPassenger()
                && !minecraft.player.isSpectator()
                && minecraft.player.isAlive()
                && !minecraft.player.getAbilities().flying
                && !minecraft.player.isShiftKeyDown();
    }

    private static KeyMapping findDodgeMapping(final Minecraft minecraft) {
        if (dodgeMapping != null) {
            return dodgeMapping;
        }
        for (final KeyMapping mapping : minecraft.options.keyMappings) {
            if (PARCOOL_DODGE_MAPPING.equals(mapping.getName())) {
                dodgeMapping = mapping;
                return mapping;
            }
        }
        return null;
    }

    private static void restoreDodgeKey() {
        if (!syntheticDodgeDown || dodgeMapping == null) {
            return;
        }
        dodgeMapping.setDown(previousDodgeDown);
        syntheticDodgeDown = false;
    }

    private static boolean exactVersion(final String modId, final String version) {
        return ModList.get().getModContainerById(modId)
                .map(container -> version.equals(container.getModInfo().getVersion().toString()))
                .orElse(false);
    }
}
