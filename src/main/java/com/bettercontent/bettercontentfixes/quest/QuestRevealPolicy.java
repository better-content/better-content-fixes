package com.bettercontent.bettercontentfixes.quest;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import net.minecraftforge.fml.loading.FMLPaths;

public record QuestRevealPolicy(long anchorQuest, Set<Long> liveChapters, Set<Long> previewChapters,
                                Map<Long, Unlock> chapterUnlocks, Map<Long, String> taskUnlockCriteria) {
    public static final QuestRevealPolicy EMPTY = new QuestRevealPolicy(0L, Set.of(), Set.of(), Map.of(), Map.of());

    public record Unlock(String criterion, long quest) {}

    public static QuestRevealPolicy load() {
        Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve("better_content_fixes/quest_reveal.json");
        if (!Files.isRegularFile(CONFIG_PATH)) return EMPTY;
        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            return parse(JsonParser.parseReader(reader).getAsJsonObject());
        } catch (RuntimeException | IOException exception) {
            throw new IllegalStateException("Invalid quest reveal policy " + CONFIG_PATH, exception);
        }
    }

    public static QuestRevealPolicy parse(JsonObject root) {
        long anchor = parseId(root.has("anchor_quest") ? root.get("anchor_quest").getAsString() : "");
        Set<Long> live = new LinkedHashSet<>();
        if (root.has("live_chapters")) root.getAsJsonArray("live_chapters").forEach(e -> live.add(parseId(e.getAsString())));
        Set<Long> previews = new LinkedHashSet<>();
        if (root.has("preview_chapters")) root.getAsJsonArray("preview_chapters").forEach(e -> previews.add(parseId(e.getAsString())));
        Map<Long, Unlock> unlocks = new LinkedHashMap<>();
        if (root.has("chapter_unlocks")) {
            for (Map.Entry<String, com.google.gson.JsonElement> entry : root.getAsJsonObject("chapter_unlocks").entrySet()) {
                JsonObject value = entry.getValue().getAsJsonObject();
                String criterion = value.has("criterion") ? value.get("criterion").getAsString() : "";
                long quest = value.has("quest") ? parseId(value.get("quest").getAsString()) : 0L;
                if (criterion.isBlank() == (quest == 0L)) throw new IllegalArgumentException("unlock must define exactly one of criterion or quest");
                unlocks.put(parseId(entry.getKey()), new Unlock(criterion, quest));
            }
        }
        Map<Long, String> taskUnlocks = new LinkedHashMap<>();
        if (root.has("task_unlock_criteria")) {
            for (Map.Entry<String, com.google.gson.JsonElement> entry : root.getAsJsonObject("task_unlock_criteria").entrySet()) {
                String criterion = entry.getValue().getAsString();
                if (criterion.isBlank()) throw new IllegalArgumentException("task unlock criterion cannot be blank");
                taskUnlocks.put(parseId(entry.getKey()), criterion);
            }
        }
        if (anchor != 0L && live.contains(anchor)) throw new IllegalArgumentException("anchor quest id cannot be a chapter id");
        return new QuestRevealPolicy(anchor, Collections.unmodifiableSet(live), Collections.unmodifiableSet(previews),
                Collections.unmodifiableMap(unlocks), Collections.unmodifiableMap(taskUnlocks));
    }

    static long parseId(String value) {
        if (value == null || value.isBlank()) return 0L;
        String normalized = value.startsWith("0x") ? value.substring(2) : value;
        if (!normalized.matches("[0-9a-fA-F]{1,16}")) throw new IllegalArgumentException("FTB IDs must be hexadecimal: " + value);
        return Long.parseUnsignedLong(normalized, 16);
    }
}
