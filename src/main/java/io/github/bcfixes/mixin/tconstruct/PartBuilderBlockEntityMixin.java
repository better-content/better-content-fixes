package io.github.bcfixes.mixin.tconstruct;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import slimeknights.tconstruct.tables.block.entity.table.PartBuilderBlockEntity;
import io.github.bcfixes.tconstruct.FreehandPattern;

/** Makes an empty Part Builder pattern slot behave like its reusable blank-pattern substrate. */
@Mixin(value = PartBuilderBlockEntity.class, remap = false)
public abstract class PartBuilderBlockEntityMixin {
    @ModifyExpressionValue(
        method = "getCurrentRecipes",
        require = 1,
        at = @At(
            value = "INVOKE",
            target = "Lslimeknights/tconstruct/tables/block/entity/table/PartBuilderBlockEntity;m_8020_(I)Lnet/minecraft/world/item/ItemStack;",
            remap = false
        )
    )
    private ItemStack bcfixes$allowFreehandRecipeDiscovery(ItemStack patternStack) {
        return FreehandPattern.syntheticIfEmpty(patternStack);
    }
}
