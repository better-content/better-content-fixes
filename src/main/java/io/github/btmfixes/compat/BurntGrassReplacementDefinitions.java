package io.github.btmfixes.compat;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.btmfixes.BoundToMatterFixes;
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
                        .getResourceAsStream("data/" + BoundToMatterFixes.MOD_ID + "/burnt_grass_replacements.json"),
                "Missing burnt grass replacement resource"), StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray values = root.getAsJsonArray("values");
            List<Entry> entries = new ArrayList<>(values.size());
            for (int i = 0; i < values.size(); i++) {
                JsonObject value = values.get(i).getAsJsonObject();
                ResourceLocation sourceId = ResourceLocation.tryParse(value.get("source").getAsString());
                ResourceLocation targetId = value.has("target")
                        ? ResourceLocation.tryParse(value.get("target").getAsString())
                        : ResourceLocation.fromNamespaceAndPath(BoundToMatterFixes.MOD_ID, "burnt_" + sourceId.getPath());
                if (sourceId == null || targetId == null) {
                    throw new IllegalArgumentException("Invalid burnt replacement entry: " + value);
                }
                entries.add(new Entry(sourceId, targetId));
            }
            return Collections.unmodifiableList(entries);
        } catch (IOException | NullPointerException e) {
            throw new IllegalStateException("Failed to load burnt grass replacement definitions", e);
        }
    }

    public record Entry(ResourceLocation sourceId, ResourceLocation targetId) {
    }
}
