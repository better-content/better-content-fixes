package io.github.bcfixes.water;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class SnowMeltHandler {
    private static final long MELT_DELAY_TICKS = 5L * 20L;
    private static final Map<ResourceKey<Level>, Map<BlockPos, Long>> PENDING = new HashMap<>();

    private SnowMeltHandler() {
    }

    @SubscribeEvent
    public static void onBlockPlaced(final BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        scheduleCandidate(level, event.getPos());
        scheduleCandidate(level, event.getPos().below());
    }

    @SubscribeEvent
    public static void onCampfireInteraction(final PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        scheduleCandidate(level, event.getPos().below());
    }

    @SubscribeEvent
    public static void onLevelTick(final TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level)) return;
        final Map<BlockPos, Long> positions = PENDING.get(level.dimension());
        if (positions == null || positions.isEmpty()) return;
        final long now = level.getGameTime();
        final Iterator<Map.Entry<BlockPos, Long>> iterator = positions.entrySet().iterator();
        while (iterator.hasNext()) {
            final Map.Entry<BlockPos, Long> entry = iterator.next();
            if (entry.getValue() > now) continue;
            final BlockPos snowPos = entry.getKey();
            final BlockState snow = level.getBlockState(snowPos);
            final BlockState above = level.getBlockState(snowPos.above());
            if (!snow.is(Blocks.SNOW_BLOCK) || !(above.getBlock() instanceof CampfireBlock)) {
                iterator.remove();
                continue;
            }
            if (above.getValue(CampfireBlock.LIT)) {
                level.setBlockAndUpdate(snowPos, Blocks.WATER.defaultBlockState());
                iterator.remove();
            } else {
                entry.setValue(now + MELT_DELAY_TICKS);
            }
        }
        if (positions.isEmpty()) PENDING.remove(level.dimension());
    }

    static void scheduleCandidate(final ServerLevel level, final BlockPos candidate) {
        if (!level.getBlockState(candidate).is(Blocks.SNOW_BLOCK)) return;
        final BlockState above = level.getBlockState(candidate.above());
        if (!(above.getBlock() instanceof CampfireBlock)) return;
        PENDING.computeIfAbsent(level.dimension(), ignored -> new HashMap<>())
                .put(candidate.immutable(), level.getGameTime() + MELT_DELAY_TICKS);
    }
}
