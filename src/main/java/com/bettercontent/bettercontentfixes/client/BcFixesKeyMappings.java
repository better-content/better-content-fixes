package com.bettercontent.bettercontentfixes.client;

import com.bettercontent.bettercontentfixes.BetterContentFixes;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = BetterContentFixes.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class BcFixesKeyMappings {
    public static final KeyMapping TOGGLE_DOUBLE_TAP_DASH = new KeyMapping(
            "key.better_content_fixes.toggle_double_tap_dash",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            "key.categories.better_content_fixes");

    private BcFixesKeyMappings() {
    }

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_DOUBLE_TAP_DASH);
    }
}
