package com.bettercontent.bettercontentfixes;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraftforge.common.CreativeModeTabRegistry;
import org.junit.jupiter.api.Test;

class CreativeInventorySearchTabMixinTest {
    private static final Path MIXIN = Path.of(
            "src/main/java/com/bettercontent/bettercontentfixes/mixin/minecraft/CreativeModeInventoryScreenMixin.java");

    @Test
    void filtersTheForgePageSourceInsteadOfAnObsoleteVanillaTabIteration() throws IOException, NoSuchMethodException {
        // Forge 47.4.13 constructs CreativeTabsScreenPage instances from this exact call during init.
        CreativeModeInventoryScreen.class.getDeclaredMethod("init");
        CreativeModeTabRegistry.class.getDeclaredMethod("getSortedCreativeModeTabs");

        final String source = Files.readString(MIXIN);
        assertTrue(source.contains("method = \"init\""));
        assertTrue(source.contains("CreativeModeTabRegistry;getSortedCreativeModeTabs()Ljava/util/List;"));
        assertTrue(source.contains("tab != CreativeModeTabs.searchTab()"));
        assertFalse(source.contains("CreativeModeTabs;tabs()Ljava/util/List;"));
    }
}
