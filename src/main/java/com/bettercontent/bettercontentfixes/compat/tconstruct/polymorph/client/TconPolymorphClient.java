package com.bettercontent.bettercontentfixes.compat.tconstruct.polymorph.client;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import slimeknights.tconstruct.tables.client.inventory.CraftingStationScreen;
import slimeknights.tconstruct.tables.menu.slot.PlayerSensitiveLazyResultSlot;

public final class TconPolymorphClient {
    private TconPolymorphClient() {
    }

    public static void register() {
        PolymorphApi.client().registerWidget(TconPolymorphClient::createWidget);
    }

    private static CraftingStationRecipesWidget createWidget(final AbstractContainerScreen<?> screen) {
        if (!(screen instanceof CraftingStationScreen)) return null;

        for (Slot slot : screen.getMenu().slots) {
            if (slot instanceof PlayerSensitiveLazyResultSlot) {
                return new CraftingStationRecipesWidget(screen, slot);
            }
        }
        return null;
    }
}
