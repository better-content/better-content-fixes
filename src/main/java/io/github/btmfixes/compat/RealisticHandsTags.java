package io.github.btmfixes.compat;

import io.github.btmfixes.BoundToMatterFixes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public final class RealisticHandsTags {
    public static final TagKey<Block> HAND = blockTag("hand");
    public static final TagKey<Block> KNIFE = blockTag("knife");
    public static final TagKey<Block> AXE = blockTag("axe");
    public static final TagKey<Block> PICKAXE = blockTag("pickaxe");
    public static final TagKey<Block> SHOVEL = blockTag("shovel");
    public static final TagKey<Block> HOE = blockTag("hoe");
    public static final TagKey<Block> SWORD = blockTag("sword");
    public static final TagKey<Block> FORCE_HARVEST = blockTag("force_harvest");
    public static final TagKey<Block> KNIFE_STRAW = blockTag("knife_straw");
    public static final TagKey<Block> KNIFE_EXTRA_STICKS = blockTag("knife_extra_sticks");

    public static final TagKey<Item> KNIFE_TOOLS = itemTag("knife");
    public static final TagKey<Item> AXE_TOOLS = itemTag("axe");
    public static final TagKey<Item> PICKAXE_TOOLS = itemTag("pickaxe");
    public static final TagKey<Item> SHOVEL_TOOLS = itemTag("shovel");
    public static final TagKey<Item> HOE_TOOLS = itemTag("hoe");
    public static final TagKey<Item> SWORD_TOOLS = itemTag("sword");

    private RealisticHandsTags() {
    }

    private static TagKey<Block> blockTag(final String name) {
        return TagKey.create(Registries.BLOCK, id("realistic_hands/" + name));
    }

    private static TagKey<Item> itemTag(final String name) {
        return TagKey.create(Registries.ITEM, id("realistic_hands/tools/" + name));
    }

    private static ResourceLocation id(final String path) {
        return new ResourceLocation(BoundToMatterFixes.MOD_ID, path);
    }
}
