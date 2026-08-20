package com.bettercontent.bettercontentfixes.compat.tconstruct.polymorph;

import com.illusivesoulworks.polymorph.common.capability.AbstractBlockEntityRecipeData;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.tables.block.entity.inventory.CraftingContainerWrapper;
import slimeknights.tconstruct.tables.block.entity.table.CraftingStationBlockEntity;

public final class CraftingStationRecipeData
        extends AbstractBlockEntityRecipeData<CraftingStationBlockEntity> {
    private static final int GRID_WIDTH = 3;
    private static final int GRID_HEIGHT = 3;

    public CraftingStationRecipeData(final CraftingStationBlockEntity owner) {
        super(owner);
    }

    @Override
    protected NonNullList<ItemStack> getInput() {
        final CraftingStationBlockEntity owner = getOwner();
        final NonNullList<ItemStack> input = NonNullList.withSize(GRID_WIDTH * GRID_HEIGHT, ItemStack.EMPTY);
        final int size = Math.min(owner.getContainerSize(), input.size());
        for (int slot = 0; slot < size; slot++) {
            input.set(slot, owner.getItem(slot));
        }
        return input;
    }

    @Override
    public void selectRecipe(final Recipe<?> recipe) {
        final CraftingStationBlockEntity owner = getOwner();
        final Level level = owner.getLevel();
        if (!(recipe instanceof CraftingRecipe craftingRecipe) || level == null) return;

        final CraftingContainerWrapper input =
                new CraftingContainerWrapper(owner, GRID_WIDTH, GRID_HEIGHT);
        if (!craftingRecipe.matches(input, level)) return;

        super.selectRecipe(craftingRecipe);
        owner.updateRecipe(craftingRecipe);
        owner.setChanged();
    }
}
