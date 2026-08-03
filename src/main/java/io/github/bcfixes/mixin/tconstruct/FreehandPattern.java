package io.github.bcfixes.mixin.tconstruct;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

final class FreehandPattern {
    private static final ResourceLocation PATTERN_ID = new ResourceLocation("tconstruct", "pattern");

    private FreehandPattern() {}

    static ItemStack syntheticIfEmpty(ItemStack stack) {
        if (!stack.isEmpty()) return stack;
        Item pattern = ForgeRegistries.ITEMS.getValue(PATTERN_ID);
        return pattern == null || pattern == Items.AIR ? ItemStack.EMPTY : new ItemStack(pattern);
    }
}
