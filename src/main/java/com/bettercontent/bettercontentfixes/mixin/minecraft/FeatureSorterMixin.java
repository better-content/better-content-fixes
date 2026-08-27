package com.bettercontent.bettercontentfixes.mixin.minecraft;

import com.bettercontent.bettercontentfixes.compat.FeatureOrderSanitizer;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.biome.FeatureSorter;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;
import java.util.IdentityHashMap;
import java.util.Map;
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
        final Map<T, List<HolderSet<PlacedFeature>>> sanitized = new IdentityHashMap<>();
        int stepCount = 0;
        for (final T source : sources) {
            final List<HolderSet<PlacedFeature>> steps = FeatureOrderSanitizer.deduplicate(features.apply(source));
            sanitized.put(source, steps);
            stepCount = Math.max(stepCount, steps.size());
        }
        try {
            return original.call(sources, (Function<T, List<HolderSet<PlacedFeature>>>) sanitized::get, indexFeatures);
        } catch (final IllegalStateException cycle) {
            if (cycle.getMessage() == null || !cycle.getMessage().startsWith("Feature order cycle found")) throw cycle;
            final java.util.ArrayList<FeatureSorter.StepFeatureData> result = new java.util.ArrayList<>(stepCount);
            for (int step = 0; step < stepCount; step++) {
                final int isolatedStep = step;
                final List<FeatureSorter.StepFeatureData> isolated = original.call(sources,
                        (Function<T, List<HolderSet<PlacedFeature>>>) source -> isolate(sanitized.get(source), isolatedStep), false);
                result.add(isolated.get(step));
            }
            return List.copyOf(result);
        }
    }

    private static List<HolderSet<PlacedFeature>> isolate(final List<HolderSet<PlacedFeature>> steps, final int selected) {
        final java.util.ArrayList<HolderSet<PlacedFeature>> isolated = new java.util.ArrayList<>(steps.size());
        for (int step = 0; step < steps.size(); step++) isolated.add(step == selected ? steps.get(step) : HolderSet.direct());
        return List.copyOf(isolated);
    }
}
