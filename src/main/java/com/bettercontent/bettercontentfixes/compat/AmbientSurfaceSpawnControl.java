package com.bettercontent.bettercontentfixes.compat;

import com.bettercontent.bettercontentfixes.config.BcFixesConfig;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Keeps ordinary ambient monsters below the Overworld surface pressure band. */
public final class AmbientSurfaceSpawnControl {
    private AmbientSurfaceSpawnControl() {
    }

    @SubscribeEvent
    public static void onPositionCheck(final MobSpawnEvent.PositionCheck event) {
        if (!BcFixesConfig.mobsBlockNaturalSurfaceHostiles()) {
            return;
        }

        final boolean overworld = event.getLevel().getLevel().dimension().equals(Level.OVERWORLD);
        final MobCategory category = event.getEntity().getType().getCategory();
        final MobSpawnType spawnType = event.getSpawnType();
        if (!isAmbientSurfaceCandidate(overworld, category, spawnType)) {
            return;
        }

        final int spawnX = Mth.floor(event.getX());
        final int spawnY = Mth.floor(event.getY());
        final int spawnZ = Mth.floor(event.getZ());
        final int surfaceY = event.getLevel().getHeight(
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                spawnX,
                spawnZ);

        if (shouldDeny(
                overworld,
                category,
                spawnType,
                spawnY,
                surfaceY,
                BcFixesConfig.mobsNaturalSurfaceDepth())) {
            event.setResult(Event.Result.DENY);
        }
    }

    static boolean shouldDeny(
            final boolean overworld,
            final MobCategory category,
            final MobSpawnType spawnType,
            final int spawnY,
            final int surfaceY,
            final int surfaceDepth) {
        return isAmbientSurfaceCandidate(overworld, category, spawnType)
                && spawnY >= surfaceY - surfaceDepth;
    }

    private static boolean isAmbientSurfaceCandidate(
            final boolean overworld,
            final MobCategory category,
            final MobSpawnType spawnType) {
        return overworld
                && category == MobCategory.MONSTER
                && (spawnType == MobSpawnType.NATURAL || spawnType == MobSpawnType.CHUNK_GENERATION);
    }
}
