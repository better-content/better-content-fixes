package io.github.btmfixes.mixin.apotheosis;

import io.github.btmfixes.config.BtmFixesConfig;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "dev.shadowsoffire.apotheosis.ench.EnchModuleClient", remap = false)
public abstract class EnchModuleClientMixin {
    @Inject(method = "tooltips", at = @At("HEAD"), cancellable = true, remap = false)
    private void btmfixes$skipOffThreadTooltips(final ItemTooltipEvent event, final CallbackInfo ci) {
        if (!BtmFixesConfig.apotheosisSkipOffThreadTooltips()) {
            return;
        }

        final Minecraft minecraft = Minecraft.getInstance();
        if (!minecraft.isSameThread()) {
            ci.cancel();
        }
    }
}
