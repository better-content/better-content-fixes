package com.bettercontent.bettercontentfixes.learning;

import com.alrex.parcool.api.unstable.action.ParCoolActionEvent;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Emits learning evidence only after ParCool has accepted and started a configured custom action. */
public final class ParCoolControlLearning {
    static final String THREAD_ID = "the_body_learns_new_motions";
    static final String PERSISTED_KEY = "BetterContentFixesCustomControlEpisode";
    static final String TOKEN_KEY = "token";
    static final String FIRST_ACTION_KEY = "firstAction";

    private static final Map<String, String> QUALIFYING_ACTIONS = Map.of(
            "Dodge", "parcool:dodge",
            "FastRun", "parcool:fast_run",
            "ClingToCliff", "parcool:cling_to_cliff",
            "Vault", "parcool:vault",
            "RideZipline", "parcool:ride_zipline",
            "HangDown", "parcool:hang_down",
            "WallSlide", "parcool:wall_slide");

    private ParCoolControlLearning() {
    }

    @SubscribeEvent
    public static void onActionStarted(final ParCoolActionEvent.Start.Post event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) {
            return;
        }
        final String actionId = QUALIFYING_ACTIONS.get(event.getAction().getClass().getSimpleName());
        if (actionId != null) {
            recordAuthoritativeAction(player, actionId);
        }
    }

    static void recordAuthoritativeAction(final ServerPlayer player, final String actionId) {
        final CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        final CompoundTag episode = persisted.getCompound(PERSISTED_KEY);
        String token = episode.getString(TOKEN_KEY);
        String firstAction = episode.getString(FIRST_ACTION_KEY);

        final String activeToken = ThreadSignalsBridge.activeCorrelation(player, THREAD_ID);
        if (activeToken != null && !activeToken.isBlank() && !activeToken.equals(token)) {
            token = activeToken;
            firstAction = "";
        }

        switch (DistinctControlEpisode.advance(token, firstAction, actionId)) {
            case FIRST -> beginEpisode(player, persisted, episode, activeToken, actionId);
            case REPEAT -> {
                // A repeated reflex is not evidence of a second learned control.
            }
            case DISTINCT_SECOND -> {
                if (ThreadSignalsBridge.emit(
                        player, "custom_control_mastered", "distinct_second", token)) {
                    persisted.remove(PERSISTED_KEY);
                    player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
                }
            }
        }
    }

    private static void beginEpisode(
            final ServerPlayer player,
            final CompoundTag persisted,
            final CompoundTag episode,
            final String activeToken,
            final String actionId
    ) {
        final String token = activeToken == null || activeToken.isBlank()
                ? player.getUUID() + ":custom-control:" + player.server.getTickCount()
                : activeToken;
        if (activeToken == null || activeToken.isBlank()) {
            if (!ThreadSignalsBridge.emit(player, "custom_control_used", actionId, token)) {
                return;
            }
        }
        episode.putString(TOKEN_KEY, token);
        episode.putString(FIRST_ACTION_KEY, actionId);
        persisted.put(PERSISTED_KEY, episode);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
    }
}
