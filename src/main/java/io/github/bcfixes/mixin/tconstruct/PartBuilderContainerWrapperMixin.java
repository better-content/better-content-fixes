package io.github.bcfixes.mixin.tconstruct;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import slimeknights.tconstruct.tables.block.entity.inventory.PartBuilderContainerWrapper;
import io.github.bcfixes.tconstruct.FreehandPattern;

/** Supplies the same synthetic substrate while recipes match and assemble. */
@Mixin(value = PartBuilderContainerWrapper.class, remap = false)
public abstract class PartBuilderContainerWrapperMixin {
    @Inject(method = "getPatternStack", at = @At("RETURN"), cancellable = true, require = 1)
    private void bcfixes$allowFreehandRecipeMatching(CallbackInfoReturnable<ItemStack> callback) {
        callback.setReturnValue(FreehandPattern.syntheticIfEmpty(callback.getReturnValue()));
    }
}
