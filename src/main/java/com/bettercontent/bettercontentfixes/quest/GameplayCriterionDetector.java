package com.bettercontent.bettercontentfixes.quest;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class GameplayCriterionDetector {
    private GameplayCriterionDetector() {}

    @SubscribeEvent
    public static void onCropBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player) || !(event.getState().getBlock() instanceof CropBlock crop)
                || !crop.isMaxAge(event.getState())) return;
        String below = BuiltInRegistries.BLOCK.getKey(player.level().getBlockState(event.getPos().below()).getBlock()).toString();
        if (below.contains("regolith") && below.contains("farmland")) QuestCriteria.trigger(player, "regolith_crop_harvest");
    }

    @SubscribeEvent
    public static void onCrank(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        String id = BuiltInRegistries.BLOCK.getKey(event.getLevel().getBlockState(event.getPos()).getBlock()).toString();
        if (id.equals("create:hand_crank") && isConnectedManualWorkcell(player, event.getPos())) {
            QuestCriteria.trigger(player, "manual_workcell_run");
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player instanceof ServerPlayer player && player.tickCount % 40 == 0
                && hasFormedSmeltery(player)) QuestCriteria.trigger(player, "formed_tcon_smeltery");
    }

    static boolean hasFormedSmeltery(ServerPlayer player) {
        BlockPos center = player.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-8, -5, -8), center.offset(8, 5, 8))) {
            String id = BuiltInRegistries.BLOCK.getKey(player.level().getBlockState(pos).getBlock()).toString();
            if (!id.equals("tconstruct:smeltery_controller")) continue;
            BlockEntity entity = player.level().getBlockEntity(pos);
            if (reflectBoolean(entity, "isFormed", "isStructureValid", "isActive")) return true;
        }
        return false;
    }

    static boolean isConnectedManualWorkcell(ServerPlayer player, BlockPos crank) {
        EnumSet<Part> found = EnumSet.noneOf(Part.class);
        Set<Object> networks = new HashSet<>();
        for (BlockPos pos : BlockPos.betweenClosed(crank.offset(-16, -16, -16), crank.offset(16, 16, 16))) {
            BlockState state = player.level().getBlockState(pos);
            String id = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
            Part part = Part.from(id);
            if (part == null) continue;
            found.add(part);
            if (part.kinetic) {
                Object network = reflectNetwork(player.level().getBlockEntity(pos));
                if (network != null) networks.add(network);
            }
        }
        return found.containsAll(EnumSet.allOf(Part.class)) && networks.size() == 1;
    }

    private static boolean reflectBoolean(Object target, String... methods) {
        if (target == null) return false;
        for (String methodName : methods) {
            try {
                Method method = target.getClass().getMethod(methodName);
                if (method.invoke(target) instanceof Boolean result && result) return true;
            } catch (ReflectiveOperationException ignored) {}
        }
        return false;
    }

    private static Object reflectNetwork(Object target) {
        if (target == null) return null;
        for (String methodName : new String[]{"getNetwork", "getNetworkId"}) {
            try {
                Method method = target.getClass().getMethod(methodName);
                Object value = method.invoke(target);
                if (value != null) return value;
            } catch (ReflectiveOperationException ignored) {}
        }
        Class<?> type = target.getClass();
        while (type != null) {
            for (String fieldName : new String[]{"network", "networkId"}) {
                try {
                    Field field = type.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    Object value = field.get(target);
                    if (value != null) return value;
                } catch (ReflectiveOperationException ignored) {}
            }
            type = type.getSuperclass();
        }
        return null;
    }

    private enum Part {
        SHAFT(true), COG(true), BELT(true), PRESS(true), MIXER(true), DEPLOYER(true), DEPOT(false), BASIN(false), CASING(false);
        final boolean kinetic;
        Part(boolean kinetic) { this.kinetic = kinetic; }

        static Part from(String id) {
            if (id.equals("create:shaft")) return SHAFT;
            if (id.contains("cogwheel")) return COG;
            if (id.equals("create:belt")) return BELT;
            if (id.equals("create:mechanical_press")) return PRESS;
            if (id.equals("create:mechanical_mixer")) return MIXER;
            if (id.equals("create:deployer")) return DEPLOYER;
            if (id.equals("create:depot")) return DEPOT;
            if (id.equals("create:basin")) return BASIN;
            if (id.equals("create:andesite_casing")) return CASING;
            return null;
        }
    }
}
