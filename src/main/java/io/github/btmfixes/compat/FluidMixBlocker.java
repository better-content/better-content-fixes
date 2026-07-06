package io.github.btmfixes.compat;

import io.github.btmfixes.BoundToMatterFixes;
import io.github.btmfixes.config.BtmFixesConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class FluidMixBlocker {
    public static final TagKey<Block> ALLOWED_FLUID_GENERATED_BLOCKS =
            BlockTags.create(new ResourceLocation(BoundToMatterFixes.MOD_ID, "allowed_fluid_generated_blocks"));

    private FluidMixBlocker() {
    }

    @SubscribeEvent
    public static void onFluidPlaceBlock(final BlockEvent.FluidPlaceBlockEvent event) {
        if (!BtmFixesConfig.fluidMixingBlockGeneratedBlocks()) {
            return;
        }

        final BlockState newState = event.getNewState();
        if (!newState.is(ALLOWED_FLUID_GENERATED_BLOCKS)) {
            event.setNewState(event.getOriginalState());
        }
    }
}
