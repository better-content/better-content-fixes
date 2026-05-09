package io.github.btmfixes.mixin.hyle;

import io.github.btmfixes.config.BtmFixesConfig;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "lilypuree.hyle.world.feature.gen.RoughNoiseSampler", remap = false)
public abstract class RoughNoiseSamplerMixin {
    @Redirect(
            method = "selectTertiary",
            at = @At(value = "INVOKE", target = "Ljava/util/List;get(I)Ljava/lang/Object;"),
            remap = false)
    private Object btmfixes$safeTertiaryListGet(final List<?> list, final int index) {
        if (!BtmFixesConfig.hyleSafeTertiarySelection()) {
            return list.get(index);
        }

        final int size = list.size();
        if (size <= 0) {
            return list.get(index);
        }

        return list.get(Math.floorMod(index, size));
    }
}
