package io.github.btmfixes.mixin.burnt;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.btmfixes.compat.BurntGrassPalette;
import io.github.btmfixes.config.BtmFixesConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.pixelbank.burnt.procedures.GrassCatchProcedure", remap = false)
public abstract class GrassCatchProcedureMixin {
    @WrapOperation(
            method = "execute",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/LevelAccessor;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private static boolean btmfixes$replaceGenericBurntGrass(final LevelAccessor level,
                                                             final BlockPos pos,
                                                             BlockState newState,
                                                             final int flags,
                                                             final Operation<Boolean> original) {
        if (BtmFixesConfig.burntModdedGrassReplacements()) {
            ResourceLocation placedId = ForgeRegistries.BLOCKS.getKey(newState.getBlock());
            if (BurntGrassPalette.isVanillaBurntGrass(placedId)) {
                newState = BurntGrassPalette.resolveReplacementState(level.getBlockState(pos)).orElse(newState);
            }
        }
        return original.call(level, pos, newState, flags);
    }
}
