package com.bettercontent.bettercontentfixes.compat.tconstruct.polymorph.client;

import com.illusivesoulworks.polymorph.client.recipe.widget.PersistentRecipesWidget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;

public final class CraftingStationRecipesWidget extends PersistentRecipesWidget {
    private final Slot outputSlot;

    public CraftingStationRecipesWidget(
            final AbstractContainerScreen<?> screen,
            final Slot outputSlot
    ) {
        super(screen);
        this.outputSlot = outputSlot;
    }

    @Override
    public Slot getOutputSlot() {
        return outputSlot;
    }
}
