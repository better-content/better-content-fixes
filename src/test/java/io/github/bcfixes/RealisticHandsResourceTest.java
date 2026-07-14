package io.github.bcfixes;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

final class RealisticHandsResourceTest {
    private static final Path ROOT = Path.of("src/main/resources/data/bcfixes");

    @Test
    void requiredTagFilesExist() {
        for (String relative : new String[] {
                "tags/blocks/realistic_hands/hand.json",
                "tags/blocks/realistic_hands/knife.json",
                "tags/blocks/realistic_hands/axe.json",
                "tags/blocks/realistic_hands/pickaxe.json",
                "tags/blocks/realistic_hands/shovel.json",
                "tags/blocks/realistic_hands/hoe.json",
                "tags/blocks/realistic_hands/sword.json",
                "tags/blocks/realistic_hands/force_harvest.json",
                "tags/blocks/realistic_hands/knife_straw.json",
                "tags/blocks/realistic_hands/knife_extra_sticks.json",
                "tags/items/realistic_hands/tools/knife.json",
                "tags/items/realistic_hands/tools/axe.json",
                "tags/items/realistic_hands/tools/pickaxe.json",
                "tags/items/realistic_hands/tools/shovel.json",
                "tags/items/realistic_hands/tools/hoe.json",
                "tags/items/realistic_hands/tools/sword.json",
                "loot_modifiers/realistic_hands_knife_bonus.json"
        }) {
            assertTrue(Files.exists(ROOT.resolve(relative)), "missing Realistic Hands resource " + relative);
        }
        assertTrue(Files.exists(Path.of("src/main/resources/data/forge/loot_modifiers/global_loot_modifiers.json")));
    }

    @Test
    void representativePolicyMembershipStaysExplicit() throws IOException {
        final Set<String> hand = readValues(ROOT.resolve("tags/blocks/realistic_hands/hand.json"));
        final Set<String> shovel = readValues(ROOT.resolve("tags/blocks/realistic_hands/shovel.json"));
        final Set<String> knife = readValues(ROOT.resolve("tags/blocks/realistic_hands/knife.json"));
        final Set<String> sword = readValues(ROOT.resolve("tags/blocks/realistic_hands/sword.json"));
        final Set<String> forceHarvest = readValues(ROOT.resolve("tags/blocks/realistic_hands/force_harvest.json"));
        final Set<String> knifeTools = readValues(ROOT.resolve("tags/items/realistic_hands/tools/knife.json"));

        assertTrue(hand.contains("minecraft:gravel"), "gravel must remain explicitly hand-allowed");
        assertTrue(hand.contains("unearthed:siltstone_regolith"), "siltstone regolith must remain explicitly hand-allowed");
        assertTrue(shovel.contains("unearthed:siltstone_regolith"), "siltstone regolith must remain explicitly shovel-allowed");
        assertTrue(knife.contains("projectvibrantjourneys:short_grass"), "short grass must remain explicitly knife-gated");
        assertTrue(sword.contains("minecraft:cobweb"), "cobweb must remain explicitly sword-gated");
        assertTrue(forceHarvest.contains("unearthed:siltstone_regolith"), "siltstone regolith must remain force-harvest covered");
        assertTrue(knifeTools.contains("additionalweaponry:butcher_knife"), "butcher knife must remain an explicit knife tool");
    }

    private static Set<String> readValues(final Path path) throws IOException {
        final JsonObject root = JsonParser.parseReader(Files.newBufferedReader(path)).getAsJsonObject();
        final JsonArray values = root.getAsJsonArray("values");
        final Set<String> result = new LinkedHashSet<>();
        values.forEach(element -> result.add(element.getAsString()));
        return result;
    }
}
