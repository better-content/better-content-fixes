package io.github.btmfixes.compat;

import io.github.btmfixes.config.BtmFixesConfig;
import java.util.concurrent.locks.ReentrantLock;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.WorldGenLevel;

public final class LostCitiesC2meDhSerialization {
    private static final ResourceLocation LOSTCITY_DIMENSION = new ResourceLocation("lostcities", "lostcity");
    private static final ReentrantLock LOSTCITY_GENERATION_LOCK = new ReentrantLock();

    private LostCitiesC2meDhSerialization() {
    }

    public static boolean shouldSerialize(final WorldGenLevel level) {
        return BtmFixesConfig.lostCitiesSerializeDhC2meFeaturePlacement()
                && LOSTCITY_DIMENSION.equals(level.getLevel().dimension().location());
    }

    public static void lock() {
        LOSTCITY_GENERATION_LOCK.lock();
    }

    public static void unlock() {
        LOSTCITY_GENERATION_LOCK.unlock();
    }
}
