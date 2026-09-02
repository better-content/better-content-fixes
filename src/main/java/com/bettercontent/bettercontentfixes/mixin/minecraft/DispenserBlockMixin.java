package com.bettercontent.bettercontentfixes.mixin.minecraft;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import java.util.Collections;
import java.util.Map;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DispenserBlock.class)
public abstract class DispenserBlockMixin {
    @Shadow
    @Final
    @Mutable
    private static Map<Item, DispenseItemBehavior> DISPENSER_REGISTRY;

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void better_content_fixes$makeBehaviorRegistryThreadSafe(final CallbackInfo callback) {
        DISPENSER_REGISTRY = Collections.synchronizedMap(DISPENSER_REGISTRY);
    }

    @WrapMethod(method = "registerBehavior")
    private static void better_content_fixes$serializeBehaviorRegistration(
            final ItemLike item,
            final DispenseItemBehavior behavior,
            final Operation<Void> original
    ) {
        synchronized (DispenserBlock.class) {
            original.call(item, behavior);
        }
    }
}
