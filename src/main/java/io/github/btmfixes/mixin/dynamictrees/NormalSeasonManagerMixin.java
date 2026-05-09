package io.github.btmfixes.mixin.dynamictrees;

import io.github.btmfixes.config.BtmFixesConfig;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.ferreusveritas.dynamictrees.compat.season.NormalSeasonManager", remap = false)
public abstract class NormalSeasonManagerMixin {
    @Mutable
    @Final
    @Shadow(remap = false)
    private Map<ResourceLocation, Object> seasonContextMap;

    @Inject(method = "<init>*", at = @At("RETURN"), remap = false)
    private void btmfixes$useConcurrentSeasonContextMap(final CallbackInfo ci) {
        if (!BtmFixesConfig.dynamicTreesSeasonContextConcurrentMap()) {
            return;
        }
        this.seasonContextMap = new ConcurrentHashMap<>(this.seasonContextMap);
    }
}
