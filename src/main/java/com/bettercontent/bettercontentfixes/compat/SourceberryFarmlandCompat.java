package com.bettercontent.bettercontentfixes.compat;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

public final class SourceberryFarmlandCompat {
    private static final ResourceLocation SOURCEBERRY_BUSH =
            new ResourceLocation("ars_nouveau", "sourceberry_bush");
    public static final TagKey<Block> COMMON_FARMLAND = TagKey.create(
            Registries.BLOCK,
            new ResourceLocation("c", "farmland"));

    private SourceberryFarmlandCompat() {
    }

    public static boolean mayPlaceOn(
            final Block bush,
            final BlockState substrate,
            final boolean originalResult) {
        if (originalResult) {
            return true;
        }
        return SOURCEBERRY_BUSH.equals(ForgeRegistries.BLOCKS.getKey(bush))
                && substrate.is(COMMON_FARMLAND);
    }
}
