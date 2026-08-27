package com.bettercontent.bettercontentfixes.mixin.minecraft;

import com.bettercontent.bettercontentfixes.compat.SourceberryFarmlandCompat;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BushBlock.class)
public abstract class BushBlockMixin {
    @ModifyReturnValue(method = "mayPlaceOn", at = @At("RETURN"))
    private boolean betterContentFixes$allowSourceberriesOnCommonFarmland(
            final boolean originalResult,
            final BlockState substrate,
            final BlockGetter level,
            final BlockPos pos) {
        return SourceberryFarmlandCompat.mayPlaceOn(
                (BushBlock) (Object) this,
                substrate,
                originalResult);
    }
}
