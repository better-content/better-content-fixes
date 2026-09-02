package com.bettercontent.bettercontentfixes.client;

import com.bettercontent.bettercontentfixes.config.BcFixesConfig;
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

        if (!BcFixesConfig.toggleSneak()) {
            toggled = false;
            physicalKeyWasDown = false;
            return;
        }

        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null) {
            physicalKeyWasDown = false;
            notifyDynamicHud("cancelPhysicalSneak", null);
            return;
        }
        final KeyMapping sneak = minecraft.options.keyShift;
        final InputConstants.Key key = sneak.getKey();
        final boolean physicalKeyDown = physicalKeyDown(minecraft, key);
        notifyDynamicHud("onPhysicalSneak", physicalKeyDown);
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

    private static boolean physicalKeyDown(final Minecraft minecraft, final InputConstants.Key key) {
        if (key.getType() == InputConstants.Type.KEYSYM) {
            return GLFW.glfwGetKey(minecraft.getWindow().getWindow(), key.getValue()) == GLFW.GLFW_PRESS;
        }
        if (key.getType() == InputConstants.Type.MOUSE) {
            return GLFW.glfwGetMouseButton(minecraft.getWindow().getWindow(), key.getValue()) == GLFW.GLFW_PRESS;
        }
        return false;
    }

    private static void notifyDynamicHud(final String method, final Boolean value) {
        try {
            final Class<?> controller = Class.forName(
                    "com.bettercontent.dynamicsurvivalhud.client.hud.DynamicHudController");
            if (value == null) {
                controller.getMethod(method).invoke(null);
            } else {
                controller.getMethod(method, boolean.class).invoke(null, value);
            }
        } catch (ClassNotFoundException ignored) {
            // Dynamic Survival HUD is optional.
        } catch (ReflectiveOperationException failure) {
            throw new IllegalStateException("Could not notify optional Dynamic Survival HUD", failure);
        }
    }
}
