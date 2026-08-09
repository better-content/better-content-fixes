package io.github.bcfixes.prestige;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public final class WorldCondenserMenu extends AbstractContainerMenu {
    private final BlockPos pos;
    private final boolean remote;
    private final int initialTab;

    public WorldCondenserMenu(int id, Inventory inventory, FriendlyByteBuf data) {
        this(id, inventory, data == null ? BlockPos.ZERO : data.readBlockPos(), data != null && data.readBoolean(),
                data == null ? 0 : data.readVarInt());
    }

    public WorldCondenserMenu(int id, Inventory inventory, BlockPos pos) {
        this(id, inventory, pos, false, 0);
    }

    public WorldCondenserMenu(int id, Inventory inventory, BlockPos pos, boolean remote, int initialTab) {
        super(PrestigeRegistry.WORLD_CONDENSER_MENU.get(), id);
        this.pos = pos;
        this.remote = remote;
        this.initialTab = Math.max(0, Math.min(2, initialTab));
    }

    public BlockPos pos() { return pos; }
    public boolean remote() { return remote; }
    public int initialTab() { return initialTab; }

    @Override public ItemStack quickMoveStack(Player player, int slot) { return ItemStack.EMPTY; }

    @Override public boolean stillValid(Player player) {
        if (remote) return true;
        return player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0;
    }
}
