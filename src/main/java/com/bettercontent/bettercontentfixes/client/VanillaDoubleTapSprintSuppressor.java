package com.bettercontent.bettercontentfixes.client;

import com.bettercontent.bettercontentfixes.BetterContentFixes;
import com.bettercontent.bettercontentfixes.config.BcFixesClientConfig;
import java.lang.reflect.Field;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = BetterContentFixes.MOD_ID,
        value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class VanillaDoubleTapSprintSuppressor {
    private static final Field SPRINT_TRIGGER_TIME =
            ObfuscationReflectionHelper.findField(LocalPlayer.class, "f_108583_");

    private VanillaDoubleTapSprintSuppressor() {
    }

    @SubscribeEvent
    public static void onMovementInputUpdate(final MovementInputUpdateEvent event) {
        if (!ModList.get().isLoaded("parcool") || !BcFixesClientConfig.replaceForwardDoubleTapSprint()) {
            return;
        }
        if (!(event.getEntity() instanceof LocalPlayer player)) {
            return;
        }
        try {
            SPRINT_TRIGGER_TIME.setInt(player, 0);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Unable to clear the vanilla sprint trigger window", exception);
        }
    }
}
