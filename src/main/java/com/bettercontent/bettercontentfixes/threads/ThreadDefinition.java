package com.bettercontent.bettercontentfixes.threads;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import java.util.List;

public record ThreadDefinition(String id, String title, ResourceLocation symbol, List<String> phases,
                               List<Predicate> contact, List<Predicate> lived, Doorway doorway) {
    public static final int MAX_TEXT = 256;
    public ThreadDefinition {
        if (!id.matches("[a-z0-9_]{1,48}")) throw new IllegalArgumentException("invalid thread id");
        if (title.isBlank() || title.length() > 64) throw new IllegalArgumentException("invalid thread title");
        if (phases.size() != 3 || phases.stream().anyMatch(s -> s.isBlank() || s.length() > MAX_TEXT))
            throw new IllegalArgumentException("thread must have exactly three bounded phases");
        if (doorway.target().length() > 128) throw new IllegalArgumentException("doorway target too long");
    }
    public record Predicate(String type, String value) {
        public Predicate { if (type.length() > 24 || value.length() > 128) throw new IllegalArgumentException("predicate too long"); }
    }
    public record Doorway(String type, String target) {
        private static final java.util.Set<String> TYPES = java.util.Set.of("trace_sight", "rpg", "diet", "tconstruct", "ftb", "emi", "font", "campaign", "guideme", "powers", "lifecycle");
        public Doorway { if (!TYPES.contains(type)) throw new IllegalArgumentException("unknown doorway action " + type); }
    }
    public static ThreadDefinition parse(JsonObject json) {
        var phases = strings(json.getAsJsonArray("phases"));
        var doorway = json.getAsJsonObject("doorway");
        return new ThreadDefinition(json.get("id").getAsString(), json.get("title").getAsString(), new ResourceLocation(json.get("symbol").getAsString()), phases,
            predicates(json.getAsJsonArray("contact")), predicates(json.getAsJsonArray("lived")),
            new Doorway(doorway.get("type").getAsString(), doorway.get("target").getAsString()));
    }
    private static List<String> strings(JsonArray array) { return java.util.stream.StreamSupport.stream(array.spliterator(), false).map(e -> e.getAsString()).toList(); }
    private static List<Predicate> predicates(JsonArray array) {
        if (array == null) return List.of();
        return java.util.stream.StreamSupport.stream(array.spliterator(), false).map(e -> e.getAsJsonObject())
            .map(o -> new Predicate(o.get("type").getAsString(), o.get("value").getAsString())).toList();
    }
}
