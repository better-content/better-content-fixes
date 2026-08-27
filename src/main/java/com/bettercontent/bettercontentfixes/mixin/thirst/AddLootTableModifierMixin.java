package com.bettercontent.bettercontentfixes.mixin.thirst;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Consumer;

@Pseudo
@Mixin(targets = "dev.ghen.thirst.foundation.common.loot.AddLootTableModifier", remap = false)
public abstract class AddLootTableModifierMixin {
    @Redirect(
            method = "doApply",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/storage/loot/LootTable;getRandomItems("
                            + "Lnet/minecraft/world/level/storage/loot/LootContext;"
                            + "Ljava/util/function/Consumer;)V",
                    remap = true
            ),
            remap = false,
            require = 1
    )
    private void betterContentFixes$generateNestedLootWithoutGlobalModifiers(
            final LootTable table,
            final LootContext context,
            final Consumer<ItemStack> output
    ) {
        table.getRandomItemsRaw(context, LootTable.createStackSplitter(context.getLevel(), output));
    }
}
