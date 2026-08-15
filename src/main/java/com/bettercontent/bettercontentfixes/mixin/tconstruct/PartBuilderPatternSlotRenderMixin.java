package com.bettercontent.bettercontentfixes.mixin.tconstruct;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Hides the obsolete physical pattern slot while retaining its container behavior. */
@Mixin(AbstractContainerScreen.class)
public abstract class PartBuilderPatternSlotRenderMixin {
    private static final String PATTERN_SLOT =
        "slimeknights.tconstruct.tables.menu.PartBuilderContainerMenu$PatternSlot";

    @Inject(
        method = {
            "renderSlot(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/inventory/Slot;)V",
            "m_280092_(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/inventory/Slot;)V"
        },
        at = @At("HEAD"),
        cancellable = true,
        require = 1,
        remap = false
    )
    private void better_content_fixes$hidePatternSlot(GuiGraphics graphics, Slot slot, CallbackInfo callback) {
        if (slot.getClass().getName().equals(PATTERN_SLOT)) {
            callback.cancel();
        }
    }
}
