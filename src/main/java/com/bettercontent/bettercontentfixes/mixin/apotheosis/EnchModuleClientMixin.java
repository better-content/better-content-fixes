package com.bettercontent.bettercontentfixes.mixin.apotheosis;

import com.bettercontent.bettercontentfixes.config.BcFixesConfig;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "dev.shadowsoffire.apotheosis.ench.EnchModuleClient", remap = false)
public abstract class EnchModuleClientMixin {
    @Inject(method = "tooltips", at = @At("HEAD"), cancellable = true, remap = false)
    private void better_content_fixes$skipOffThreadTooltips(final ItemTooltipEvent event, final CallbackInfo ci) {
        if (!BcFixesConfig.apotheosisSkipOffThreadTooltips()) {
            return;
        }

        final Minecraft minecraft = Minecraft.getInstance();
        if (!minecraft.isSameThread()) {
            ci.cancel();
        }
    }
}
