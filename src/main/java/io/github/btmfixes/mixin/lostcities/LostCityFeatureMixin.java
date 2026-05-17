package io.github.btmfixes.mixin.lostcities;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.btmfixes.compat.LostCitiesC2meDhSerialization;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets = "mcjty.lostcities.worldgen.LostCityFeature", remap = false)
public abstract class LostCityFeatureMixin {
    @WrapMethod(method = "m_142674_", remap = false)
    private boolean btmfixes$serializeLostCityFeaturePlacement(
            final FeaturePlaceContext<NoneFeatureConfiguration> context,
            final Operation<Boolean> original) {
        if (!LostCitiesC2meDhSerialization.shouldSerialize(context.level())) {
            return original.call(context);
        }

        LostCitiesC2meDhSerialization.lock();
        try {
            return original.call(context);
        } finally {
            LostCitiesC2meDhSerialization.unlock();
        }
    }
}
