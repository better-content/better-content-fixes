package com.bettercontent.bettercontentfixes.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/**
 * Toggles the vanilla sneak mapping instead of replacing player movement. This preserves
 * crawling, combat movement, and vehicle input ownership in their respective mods.
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ToggleSneakHandler {
    private static boolean toggled;
    private static boolean physicalKeyWasDown;

    private ToggleSneakHandler() {}

    @SubscribeEvent
    public static void onClientTick(final TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null) return;
        final KeyMapping sneak = minecraft.options.keyShift;
        final InputConstants.Key key = sneak.getKey();
        if (key.getType() != InputConstants.Type.KEYSYM) return;

        final boolean physicalKeyDown = GLFW.glfwGetKey(minecraft.getWindow().getWindow(), key.getValue()) == GLFW.GLFW_PRESS;
        if (physicalKeyDown && !physicalKeyWasDown) {
            toggled = !toggled;
        }
        physicalKeyWasDown = physicalKeyDown;

        // Do not keep crouch active while another system owns mounted movement.
        if (minecraft.player.isPassenger()) {
            toggled = false;
        }
        sneak.setDown(toggled);
    }
}
