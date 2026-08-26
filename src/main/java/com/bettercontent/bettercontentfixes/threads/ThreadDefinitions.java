package com.bettercontent.bettercontentfixes.threads;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import java.util.*;

public final class ThreadDefinitions extends SimpleJsonResourceReloadListener {
    public static final ThreadDefinitions INSTANCE = new ThreadDefinitions();
    private volatile Map<String, ThreadDefinition> definitions = Map.of();
    private ThreadDefinitions() { super(new Gson(), "threads"); }
    @Override protected void apply(Map<ResourceLocation, JsonElement> resources, ResourceManager manager, ProfilerFiller profiler) {
        var loaded = new LinkedHashMap<String, ThreadDefinition>();
        resources.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
            JsonElement root = entry.getValue();
            if (root.isJsonArray()) root.getAsJsonArray().forEach(e -> add(loaded, e.getAsJsonObject()));
            else add(loaded, root.getAsJsonObject());
        });
        // The reusable mod's isolated GameTest lane intentionally has no pack-owned catalogue.
        if (!loaded.isEmpty() && loaded.size() != 18) throw new IllegalStateException("Threads catalogue must contain exactly 18 definitions, found " + loaded.size());
        definitions = Collections.unmodifiableMap(loaded);
    }
    private static void add(Map<String, ThreadDefinition> loaded, JsonObject json) {
        var definition = ThreadDefinition.parse(json);
        if (loaded.putIfAbsent(definition.id(), definition) != null) throw new IllegalStateException("duplicate thread " + definition.id());
    }
    public Collection<ThreadDefinition> all() { return definitions.values(); }
    public ThreadDefinition get(String id) { return definitions.get(id); }
    public boolean contains(String id) { return definitions.containsKey(id); }
}
