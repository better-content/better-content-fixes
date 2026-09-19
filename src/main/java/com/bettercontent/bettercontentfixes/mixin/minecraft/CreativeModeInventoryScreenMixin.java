package com.bettercontent.bettercontentfixes.mixin.minecraft;

import com.bettercontent.bettercontentfixes.config.BcFixesConfig;
import java.util.List;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryScreenMixin {
    @Redirect(
            method = {"mouseClicked", "mouseReleased", "render", "renderBg", "getTooltipFromContainerItem"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/CreativeModeTabs;tabs()Ljava/util/List;"),
            require = 1)
    private List<CreativeModeTab> betterContentFixes$visibleCreativeTabs() {
        final List<CreativeModeTab> tabs = CreativeModeTabs.tabs();
        if (!BcFixesConfig.hideCreativeInventorySearchTab()) {
            return tabs;
        }
        return tabs.stream()
                .filter(tab -> tab != CreativeModeTabs.searchTab())
                .toList();
    }
}
