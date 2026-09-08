package com.bettercontent.bettercontentfixes.learning;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Optional, reflection-only access to Better Content Threads' public integration API. */
final class ThreadSignalsBridge {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String API_CLASS = "com.bettercontent.threads.api.ThreadSignals";

    private static boolean resolved;
    private static boolean failureLogged;
    private static Method emit;
    private static Method activeCorrelation;

    private ThreadSignalsBridge() {
    }

    static boolean emit(
            final ServerPlayer player,
            final String type,
            final String value,
            final String correlationToken
    ) {
        resolve();
        if (emit == null) {
            return false;
        }
        try {
            emit.invoke(null, player, type, value, correlationToken);
            return true;
        } catch (IllegalAccessException | InvocationTargetException failure) {
            logFailure("emit", failure);
            return false;
        }
    }

    static String activeCorrelation(final ServerPlayer player, final String threadId) {
        resolve();
        if (activeCorrelation == null) {
            return null;
        }
        try {
            return (String) activeCorrelation.invoke(null, player, threadId);
        } catch (IllegalAccessException | InvocationTargetException | ClassCastException failure) {
            logFailure("read an active correlation", failure);
            return null;
        }
    }

    private static synchronized void resolve() {
        if (resolved) {
            return;
        }
        resolved = true;
        try {
            final Class<?> signals = Class.forName(API_CLASS, false, ThreadSignalsBridge.class.getClassLoader());
            emit = signals.getMethod(
                    "emit", ServerPlayer.class, String.class, String.class, String.class);
            activeCorrelation = signals.getMethod("activeCorrelation", ServerPlayer.class, String.class);
        } catch (ClassNotFoundException | NoSuchMethodException ignored) {
            emit = null;
            activeCorrelation = null;
        } catch (LinkageError failure) {
            logFailure("resolve the optional API", failure);
            emit = null;
            activeCorrelation = null;
        }
    }

    private static void logFailure(final String operation, final Throwable failure) {
        if (!failureLogged) {
            failureLogged = true;
            LOGGER.warn("Could not {} through {}", operation, API_CLASS, failure);
        }
    }
}
