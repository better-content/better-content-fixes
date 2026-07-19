package io.github.bcfixes.compat;

import io.github.bcfixes.BetterContentFixes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import team.creative.playerrevive.client.ReviveEventClient;
import team.creative.playerrevive.server.PlayerReviveServer;

@Mod.EventBusSubscriber(modid = BetterContentFixes.MOD_ID, value = Dist.CLIENT)
public final class ClientReviveOverlayCleanup {
    private static LocalPlayer previousLocalPlayer;

    private ClientReviveOverlayCleanup() {
    }

    @SubscribeEvent
    public static void onClientTick(final TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !ReviveEventClient.helpActive) return;
        final Minecraft minecraft = Minecraft.getInstance();
        final LocalPlayer local = minecraft.player;
        final boolean playerReplaced = previousLocalPlayer != null && previousLocalPlayer != local;
        previousLocalPlayer = local;
        final Player target = local == null || local.level() == null || ReviveEventClient.helpTarget == null
                ? null
                : local.level().getPlayerByUUID(ReviveEventClient.helpTarget);
        final boolean invalid = playerReplaced
                || local == null
                || !local.isAlive()
                || target == null
                || !target.isAlive()
                || target.level() != local.level()
                || !PlayerReviveServer.isBleeding(target);
        if (invalid) {
            ReviveEventClient.helpActive = false;
            ReviveEventClient.helpTarget = null;
        }
    }
}
