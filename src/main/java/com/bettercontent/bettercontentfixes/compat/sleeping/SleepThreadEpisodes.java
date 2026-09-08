package com.bettercontent.bettercontentfixes.compat.sleeping;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Bridges Sleeping Overhaul's authoritative timelapse boundaries to optional Threads evidence. */
public final class SleepThreadEpisodes {
    private static final String ROOT = "BetterContentSleepThreadEpisode";
    private static final String TOKEN = "token";
    private static final String DIMENSION = "dimension";
    private static final String HAS_POSITION = "hasPosition";
    private static final String POSITION = "position";
    private static final String ANGLE = "angle";
    private static final String FORCED = "forced";
    static final String THREAD_ID = "sleep_is_not_an_anchor";
    static final String START_TYPE = "sleep_started";
    static final String START_VALUE = "night";
    static final String FINISH_TYPE = "sleep_finished";
    static final String FINISH_VALUE = "simulated_time";

    private static MinecraftServer observedServer;
    private static boolean previouslyActive;

    private SleepThreadEpisodes() {
    }

    public static void update(final MinecraftServer server, final boolean active) {
        if (server != observedServer) {
            observedServer = server;
            previouslyActive = false;
        }

        final Transition transition = transition(previouslyActive, active);
        previouslyActive = active;
        if (transition == Transition.START) {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (player.isSleeping()) {
                    start(player);
                }
            }
        } else if (transition == Transition.FINISH && server.overworld().isDay()) {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                finish(player);
            }
        }
    }

    static Transition transition(final boolean wasActive, final boolean active) {
        if (!wasActive && active) {
            return Transition.START;
        }
        if (wasActive && !active) {
            return Transition.FINISH;
        }
        return Transition.NONE;
    }

    static String episodeToken(final String activeToken, final String persistedToken, final String generatedToken) {
        if (validToken(activeToken)) {
            return activeToken;
        }
        if (validToken(persistedToken)) {
            return persistedToken;
        }
        return generatedToken;
    }

    private static void start(final ServerPlayer player) {
        final CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        final CompoundTag prior = persisted.getCompound(ROOT);
        final String active = ThreadSignalsReflection.activeCorrelation(player, THREAD_ID);
        final String token = episodeToken(
                active,
                prior.getString(TOKEN),
                player.getUUID() + ":sleep:" + player.server.getTickCount());
        if (!validToken(token)) {
            return;
        }

        final CompoundTag episode = respawnContract(player);
        episode.putString(TOKEN, token);
        persisted.put(ROOT, episode);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
        if (active == null) {
            ThreadSignalsReflection.emit(player, START_TYPE, START_VALUE, token);
        }
    }

    private static void finish(final ServerPlayer player) {
        final CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        if (!persisted.contains(ROOT)) {
            return;
        }
        final CompoundTag episode = persisted.getCompound(ROOT);
        if (!sameRespawnContract(player, episode)) {
            return;
        }
        final String token = episode.getString(TOKEN);
        final String active = ThreadSignalsReflection.activeCorrelation(player, THREAD_ID);
        if (!validToken(token) || !token.equals(active)) {
            return;
        }
        if (ThreadSignalsReflection.emit(player, FINISH_TYPE, FINISH_VALUE, token)) {
            persisted.remove(ROOT);
            player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
        }
    }

    private static CompoundTag respawnContract(final ServerPlayer player) {
        final CompoundTag contract = new CompoundTag();
        contract.putString(DIMENSION, player.getRespawnDimension().location().toString());
        final BlockPos position = player.getRespawnPosition();
        contract.putBoolean(HAS_POSITION, position != null);
        if (position != null) {
            contract.putLong(POSITION, position.asLong());
        }
        contract.putInt(ANGLE, Float.floatToIntBits(player.getRespawnAngle()));
        contract.putBoolean(FORCED, player.isRespawnForced());
        return contract;
    }

    private static boolean sameRespawnContract(final ServerPlayer player, final CompoundTag expected) {
        final BlockPos position = player.getRespawnPosition();
        return expected.getString(DIMENSION).equals(player.getRespawnDimension().location().toString())
                && expected.getBoolean(HAS_POSITION) == (position != null)
                && (position == null || expected.getLong(POSITION) == position.asLong())
                && expected.getInt(ANGLE) == Float.floatToIntBits(player.getRespawnAngle())
                && expected.getBoolean(FORCED) == player.isRespawnForced();
    }

    private static boolean validToken(final String value) {
        if (value == null || value.isBlank() || value.length() > 128) {
            return false;
        }
        for (int index = 0; index < value.length(); index++) {
            final char character = value.charAt(index);
            if (character < 0x21 || character > 0x7e) {
                return false;
            }
        }
        return true;
    }

    enum Transition {
        START,
        FINISH,
        NONE
    }

    private static final class ThreadSignalsReflection {
        private static final Logger LOGGER = LogManager.getLogger();
        private static final String API = "com.bettercontent.threads.api.ThreadSignals";
        private static boolean failureLogged;

        private ThreadSignalsReflection() {
        }

        static boolean emit(
                final ServerPlayer player,
                final String type,
                final String value,
                final String correlationToken
        ) {
            try {
                Class.forName(API, false, SleepThreadEpisodes.class.getClassLoader())
                        .getMethod(
                                "emit",
                                ServerPlayer.class,
                                String.class,
                                String.class,
                                String.class)
                        .invoke(null, player, type, value, correlationToken);
                return true;
            } catch (ClassNotFoundException | NoSuchMethodException ignored) {
                return false;
            } catch (IllegalAccessException | InvocationTargetException | LinkageError failure) {
                logFailure(failure);
                return false;
            }
        }

        static String activeCorrelation(final ServerPlayer player, final String threadId) {
            try {
                final Method method = Class.forName(API, false, SleepThreadEpisodes.class.getClassLoader())
                        .getMethod("activeCorrelation", ServerPlayer.class, String.class);
                return (String) method.invoke(null, player, threadId);
            } catch (ClassNotFoundException | NoSuchMethodException ignored) {
                return null;
            } catch (IllegalAccessException | InvocationTargetException | ClassCastException | LinkageError failure) {
                logFailure(failure);
                return null;
            }
        }

        private static void logFailure(final Throwable failure) {
            if (!failureLogged) {
                failureLogged = true;
                LOGGER.warn("Could not emit optional Sleeping Overhaul evidence through {}", API, failure);
            }
        }
    }
}
