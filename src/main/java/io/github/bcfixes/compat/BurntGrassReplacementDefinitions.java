package io.github.bcfixes.compat;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.bcfixes.BetterContentFixes;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import net.minecraft.resources.ResourceLocation;

public final class BurntGrassReplacementDefinitions {
    private static final List<Entry> ENTRIES = loadEntries();

    private BurntGrassReplacementDefinitions() {
    }

    public static List<Entry> entries() {
        return ENTRIES;
    }

    private static List<Entry> loadEntries() {
        try (Reader reader = new InputStreamReader(Objects.requireNonNull(
                BurntGrassReplacementDefinitions.class.getClassLoader()
                        .getResourceAsStream("data/" + BetterContentFixes.MOD_ID + "/burnt_grass_replacements.json"),
                "Missing burnt grass replacement resource"), StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            return Collections.unmodifiableList(parseEntries(root));
        } catch (IOException | NullPointerException e) {
            throw new IllegalStateException("Failed to load burnt grass replacement definitions", e);
        }
    }

    static List<Entry> parseEntries(final JsonObject root) {
        JsonArray values = root.getAsJsonArray("values");
        List<Entry> entries = new ArrayList<>(values.size());
        for (int i = 0; i < values.size(); i++) {
            JsonObject value = values.get(i).getAsJsonObject();
            entries.add(parseEntry(value));
        }
        return entries;
    }

    static Entry parseEntry(final JsonObject value) {
        ResourceLocation sourceId = ResourceLocation.tryParse(value.get("source").getAsString());
        if (sourceId == null) {
            throw new IllegalArgumentException("Invalid burnt replacement entry: " + value);
        }
        ResourceLocation targetId = value.has("target")
                ? ResourceLocation.tryParse(value.get("target").getAsString())
                : defaultTargetId(sourceId);
        if (targetId == null) {
            throw new IllegalArgumentException("Invalid burnt replacement entry: " + value);
        }
        return new Entry(sourceId, targetId);
    }

    static ResourceLocation defaultTargetId(final ResourceLocation sourceId) {
        return ResourceLocation.fromNamespaceAndPath(BetterContentFixes.MOD_ID, "burnt_" + sourceId.getPath());
    }

    public record Entry(ResourceLocation sourceId, ResourceLocation targetId) {
    }
}
