package com.bettercontent.bettercontentfixes.mixin.minecraft;

import com.bettercontent.bettercontentfixes.compat.FeatureOrderSanitizer;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.biome.FeatureSorter;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.function.Function;

@Mixin(FeatureSorter.class)
public abstract class FeatureSorterMixin {
    @SuppressWarnings("unchecked")
    @WrapOperation(
            method = "buildFeaturesPerStep",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/function/Function;apply(Ljava/lang/Object;)Ljava/lang/Object;"
            ),
            require = 1
    )
    private static Object betterContentFixes$removeRepeatedPlacedFeatures(
            final Function<Object, Object> source,
            final Object biome,
            final Operation<Object> original
    ) {
        final Object result = original.call(source, biome);
        if (!(result instanceof List<?> steps)
                || steps.stream().anyMatch(step -> !(step instanceof HolderSet<?>))) {
            return result;
        }
        return FeatureOrderSanitizer.deduplicate((List<HolderSet<PlacedFeature>>) (List<?>) steps);
    }
}
