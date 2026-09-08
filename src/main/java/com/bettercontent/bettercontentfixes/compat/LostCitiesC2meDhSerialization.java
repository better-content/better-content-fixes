package com.bettercontent.bettercontentfixes.compat;

import com.bettercontent.bettercontentfixes.config.BcFixesConfig;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.WorldGenLevel;

public final class LostCitiesC2meDhSerialization {
    private static final ResourceLocation LOSTCITY_DIMENSION = new ResourceLocation("lostcities", "lostcity");
    private static final ReentrantLock LOSTCITY_GENERATION_LOCK = new ReentrantLock();

    private LostCitiesC2meDhSerialization() {
    }

    public static boolean shouldSerialize(final WorldGenLevel level) {
        return BcFixesConfig.lostCitiesSerializeDhC2meFeaturePlacement()
                && LOSTCITY_DIMENSION.equals(level.getLevel().dimension().location());
    }

    public static boolean dependenciesAvailable(
            final boolean enabled,
            final Predicate<String> modLoaded) {
        return enabled && modLoaded.test("lostcities") && modLoaded.test("c2me");
    }

    public static void runSerialized(final Runnable operation) {
        LOSTCITY_GENERATION_LOCK.lock();
        try {
            operation.run();
        } finally {
            LOSTCITY_GENERATION_LOCK.unlock();
        }
    }

    public static <T> T callSerialized(final Supplier<T> operation) {
        LOSTCITY_GENERATION_LOCK.lock();
        try {
            return operation.get();
        } finally {
            LOSTCITY_GENERATION_LOCK.unlock();
        }
    }
}
