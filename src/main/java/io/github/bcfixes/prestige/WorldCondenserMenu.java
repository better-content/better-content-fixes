package io.github.bcfixes.prestige;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public final class WorldCondenserMenu extends AbstractContainerMenu {
    private final BlockPos pos;

    public WorldCondenserMenu(int id, Inventory inventory, FriendlyByteBuf data) {
        this(id, inventory, data.readBlockPos());
    }

    public WorldCondenserMenu(int id, Inventory inventory, BlockPos pos) {
        super(PrestigeRegistry.WORLD_CONDENSER_MENU.get(), id);
        this.pos = pos;
    }

    public BlockPos pos() { return pos; }

    @Override public ItemStack quickMoveStack(Player player, int slot) { return ItemStack.EMPTY; }

    @Override public boolean stillValid(Player player) {
        return player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0;
    }
}
