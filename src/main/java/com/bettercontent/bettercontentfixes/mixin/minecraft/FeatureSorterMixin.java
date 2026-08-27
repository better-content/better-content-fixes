package com.bettercontent.bettercontentfixes.mixin.minecraft;

import com.bettercontent.bettercontentfixes.compat.FeatureOrderSanitizer;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.biome.FeatureSorter;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;
import java.util.function.Function;

@Mixin(FeatureSorter.class)
public abstract class FeatureSorterMixin {
    @WrapMethod(method = "buildFeaturesPerStep")
    private static <T> List<FeatureSorter.StepFeatureData> betterContentFixes$removeRepeatedPlacedFeatures(
            final List<T> sources,
            final Function<T, List<HolderSet<PlacedFeature>>> features,
            final boolean indexFeatures,
            final Operation<List<FeatureSorter.StepFeatureData>> original
    ) {
        return original.call(sources, (Function<T, List<HolderSet<PlacedFeature>>>) source ->
                FeatureOrderSanitizer.deduplicate(features.apply(source)), indexFeatures);
    }
}
