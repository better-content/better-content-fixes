package com.bettercontent.bettercontentfixes.compat;

import com.bettercontent.bettercontentfixes.BetterContentFixes;
import java.util.List;
import net.minecraft.resources.ResourceLocation;

public final class RegolithFarmlandDefinitions {
    private static final String UNEARTHED = "unearthed";

    private static final List<Entry> ENTRIES = List.of(
            entry("beige_limestone"),
            entry("conglomerate"),
            entry("dolomite"),
            entry("gabbro"),
            entry("granodiorite"),
            entry("grey_limestone"),
            entry("kimberlite"),
            entry("limestone"),
            entry("mudstone"),
            entry("phyllite"),
            entry("quartzite"),
            entry("rhyolite"),
            entry("sandstone"),
            entry("siltstone"),
            entry("slate"),
            entry("stone"),
            entry("white_granite"));

    private RegolithFarmlandDefinitions() {
    }

    public static List<Entry> entries() {
        return ENTRIES;
    }

    private static Entry entry(final String stem) {
        return new Entry(
                id(UNEARTHED, stem + "_grassy_regolith"),
                id(UNEARTHED, stem + "_regolith"),
                id(BetterContentFixes.MOD_ID, stem + "_regolith_farmland"),
                title(stem) + " Regolith Farmland");
    }

    private static ResourceLocation id(final String namespace, final String path) {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }

    private static String title(final String raw) {
        final String[] parts = raw.split("_");
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                builder.append(' ');
            }
            final String part = parts[i];
            builder.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return builder.toString();
    }

    public record Entry(
            ResourceLocation grassyRegolithId,
            ResourceLocation plainRegolithId,
            ResourceLocation farmlandId,
            String displayName) {
    }
}
