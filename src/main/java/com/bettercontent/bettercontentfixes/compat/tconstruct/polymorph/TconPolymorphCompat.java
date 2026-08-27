package com.bettercontent.bettercontentfixes.compat.tconstruct.polymorph;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import slimeknights.tconstruct.tables.block.entity.table.CraftingStationBlockEntity;
import slimeknights.tconstruct.tables.menu.CraftingStationContainerMenu;

public final class TconPolymorphCompat {
    private TconPolymorphCompat() {
    }

    public static void register() {
        PolymorphApi.common().registerBlockEntity2RecipeData(blockEntity -> {
            if (blockEntity instanceof CraftingStationBlockEntity station) {
                return new CraftingStationRecipeData(station);
            }
            return null;
        });
        PolymorphApi.common().registerContainer2BlockEntity(container -> {
            if (container instanceof CraftingStationContainerMenu stationMenu) {
                return stationMenu.getTile();
            }
            return null;
        });
    }
}
