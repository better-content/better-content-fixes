package io.github.btmfixes.compat;

import io.github.btmfixes.BoundToMatterFixes;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BoundToMatterFixes.MOD_ID)
public final class RealisticHandsCompat {
    private RealisticHandsCompat() {
    }

    @SubscribeEvent
    public static void onBreakSpeed(final PlayerEvent.BreakSpeed event) {
        if (!shouldDeny(event.getEntity(), event.getState())) {
            return;
        }
        event.setNewSpeed(0.0F);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onHarvestCheck(final PlayerEvent.HarvestCheck event) {
        final Player player = event.getEntity();
        final BlockState state = event.getTargetBlock();
        if (shouldDeny(player, state)) {
            event.setCanHarvest(false);
        } else if (shouldForceHarvest(player, state)) {
            event.setCanHarvest(true);
        }
    }

    @SubscribeEvent
    public static void onLeftClickBlock(final PlayerInteractEvent.LeftClickBlock event) {
        final BlockState state = event.getLevel().getBlockState(event.getPos());
        if (!shouldDeny(event.getEntity(), state)) {
            return;
        }
        event.setUseBlock(Event.Result.DENY);
        event.setUseItem(Event.Result.DENY);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onBreakBlock(final BlockEvent.BreakEvent event) {
        final Player player = event.getPlayer();
        final BlockState state = event.getState();
        if (shouldDeny(player, state)) {
            event.setCanceled(true);
            return;
        }
        damageKnife(player, state);
    }

    private static boolean shouldDeny(final Player player, final BlockState state) {
        if (player == null || state == null || player.isCreative()) {
            return false;
        }
        if (!isPolicyBlock(state)) {
            return false;
        }
        return !hasMatchingTool(player, state);
    }

    private static boolean shouldForceHarvest(final Player player, final BlockState state) {
        if (player == null || state == null || player.isCreative()) {
            return false;
        }
        return state.is(RealisticHandsTags.FORCE_HARVEST) && hasMatchingTool(player, state);
    }

    private static boolean hasMatchingTool(final Player player, final BlockState state) {
        if (player.isCreative()) {
            return true;
        }
        if (state.is(RealisticHandsTags.HAND)) {
            return true;
        }

        final ItemStack stack = player.getMainHandItem();
        return matches(state, stack, RealisticHandsTags.KNIFE, RealisticHandsTags.KNIFE_TOOLS)
                || matches(state, stack, RealisticHandsTags.AXE, RealisticHandsTags.AXE_TOOLS)
                || matches(state, stack, RealisticHandsTags.PICKAXE, RealisticHandsTags.PICKAXE_TOOLS)
                || matches(state, stack, RealisticHandsTags.SHOVEL, RealisticHandsTags.SHOVEL_TOOLS)
                || matches(state, stack, RealisticHandsTags.HOE, RealisticHandsTags.HOE_TOOLS)
                || matches(state, stack, RealisticHandsTags.SWORD, RealisticHandsTags.SWORD_TOOLS);
    }

    private static boolean matches(
            final BlockState state,
            final ItemStack stack,
            final TagKey<net.minecraft.world.level.block.Block> blockTag,
            final TagKey<Item> itemTag
    ) {
        return state.is(blockTag) && !stack.isEmpty() && stack.is(itemTag);
    }

    private static boolean isPolicyBlock(final BlockState state) {
        return state.is(RealisticHandsTags.HAND)
                || state.is(RealisticHandsTags.KNIFE)
                || state.is(RealisticHandsTags.AXE)
                || state.is(RealisticHandsTags.PICKAXE)
                || state.is(RealisticHandsTags.SHOVEL)
                || state.is(RealisticHandsTags.HOE)
                || state.is(RealisticHandsTags.SWORD);
    }

    private static void damageKnife(final Player player, final BlockState state) {
        if (player == null || player.isCreative() || !state.is(RealisticHandsTags.KNIFE)) {
            return;
        }
        final ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty() || !stack.is(RealisticHandsTags.KNIFE_TOOLS) || !stack.isDamageableItem()) {
            return;
        }
        stack.hurtAndBreak(1, player, broken -> broken.broadcastBreakEvent(InteractionHand.MAIN_HAND));
    }
}
