package com.bettercontent.bettercontentfixes.client.hud;

import com.momosoftworks.coldsweat.api.temperature.modifier.FoodTempModifier;
import com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.client.gui.Overlays;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;

import java.util.Set;
import java.util.TreeSet;

final class ColdSweatHudTelemetry {
    private static final double SEVERE_BODY_TEMPERATURE = 100.0D;

    private ColdSweatHudTelemetry() {
    }

    static Snapshot sample(final LocalPlayer player) {
        final double bodyTemperature = Temperature.get(player, Temperature.Trait.BODY);
        final double worldTemperature = Temperature.get(player, Temperature.Trait.WORLD);
        final double freezingPoint = Temperature.get(player, Temperature.Trait.FREEZING_POINT);
        final double burningPoint = Temperature.get(player, Temperature.Trait.BURNING_POINT);
        final int worldSeverity = Overlays.getGaugeSeverity(worldTemperature, freezingPoint, burningPoint);

        final Set<FoodEffect> foodEffects = new TreeSet<>();
        Temperature.getModifiers(player).forEach((trait, modifiers) -> {
            for (TempModifier modifier : modifiers) {
                if (modifier instanceof FoodTempModifier) {
                    final CompoundTag tag = modifier.getNBT();
                    foodEffects.add(new FoodEffect(
                            trait.name(),
                            tag.getString("item"),
                            tag.getDouble("temperature"),
                            tag.getDouble("duration")
                    ));
                }
            }
        });

        return new Snapshot(
                Math.round(bodyTemperature),
                worldSeverity,
                Set.copyOf(foodEffects),
                Math.abs(bodyTemperature) >= SEVERE_BODY_TEMPERATURE,
                Math.abs(worldSeverity) >= 3
        );
    }

    record Snapshot(
            long bodyTemperature,
            int worldSeverity,
            Set<FoodEffect> foodEffects,
            boolean dangerousBodyTemperature,
            boolean dangerousWorldTemperature
    ) {
    }

    private record FoodEffect(String trait, String item, double temperature, double duration)
            implements Comparable<FoodEffect> {
        @Override
        public int compareTo(final FoodEffect other) {
            int comparison = trait.compareTo(other.trait);
            if (comparison == 0) comparison = item.compareTo(other.item);
            if (comparison == 0) comparison = Double.compare(temperature, other.temperature);
            if (comparison == 0) comparison = Double.compare(duration, other.duration);
            return comparison;
        }
    }
}
