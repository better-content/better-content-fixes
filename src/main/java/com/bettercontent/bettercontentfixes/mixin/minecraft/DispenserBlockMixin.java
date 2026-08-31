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
    // This project intentionally ships without a refmap, so target the 1.20.1 SRG field directly.
    @Shadow(remap = false)
    @Final
    @Mutable
    private static Map<Item, DispenseItemBehavior> f_52661_;

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void better_content_fixes$makeBehaviorRegistryThreadSafe(final CallbackInfo callback) {
        f_52661_ = Collections.synchronizedMap(f_52661_);
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
