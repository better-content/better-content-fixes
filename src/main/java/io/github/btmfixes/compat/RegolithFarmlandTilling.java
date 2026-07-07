package io.github.btmfixes.compat;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class RegolithFarmlandTilling {
    private static final Logger LOGGER = LogManager.getLogger();

    private RegolithFarmlandTilling() {
    }

    @SubscribeEvent
    public static void onRightClickBlock(final PlayerInteractEvent.RightClickBlock event) {
        final Level level = event.getLevel();
        final Player player = event.getEntity();
        final ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof HoeItem) || event.getFace() == null) {
            return;
        }
        if (event.getFace().getAxis().isVertical() && event.getFace().getStepY() < 0) {
            return;
        }

        final BlockPos pos = event.getPos();
        if (!level.getBlockState(pos.above()).isAir()) {
            return;
        }

        final BlockState state = level.getBlockState(pos);
        final ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        if (blockId == null) {
            return;
        }

        final Optional<RegolithFarmlandDefinitions.Entry> entry = RegolithFarmlandPalette.lookupByGrassyRegolith(blockId);
        if (entry.isEmpty()) {
            return;
        }

        final Optional<Block> farmlandBlock = RegolithFarmlandPalette.getFarmlandBlock(entry.get().farmlandId());
        if (farmlandBlock.isEmpty()) {
            LOGGER.warn("Missing registered regolith farmland block {}", entry.get().farmlandId());
            return;
        }

        level.playSound(player, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
        if (!level.isClientSide) {
            final BlockState farmlandState = farmlandBlock.get().defaultBlockState();
            level.setBlockAndUpdate(pos, farmlandState);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, farmlandState));
            stack.hurtAndBreak(1, player, living -> living.broadcastBreakEvent(event.getHand()));
        }

        event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
        event.setCanceled(true);
    }
}
