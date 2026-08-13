package com.bettercontent.bettercontentfixes.quest;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Named, version-tolerant stack checks used by authored quest data. */
public final class NamedStackPredicates {
    public static final Set<String> SUPPORTED = Set.of(
            "fiahi_temperature_changed", "water_purity_3", "food_pouch_loaded", "armor_with_inserted_insulation",
            "tempered_waterskin", "any_tcon_sand_cast", "any_tcon_permanent_cast", "tcon_functional_metal_part",
            "tcon_tool_with_metal_functional_part");
    private static final Set<String> METALS = Set.of(
            "iron", "copper", "gold", "netherite", "cobalt", "manyullyn", "queens_slime", "hepatizon",
            "rose_gold", "pig_iron", "amethyst_bronze", "slimesteel", "brass", "bronze", "steel");

    private NamedStackPredicates() {}

    public static boolean test(String name, ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        return switch (name) {
            case "water_purity_3" -> integerValue(stack.getTag(), "purity", "water_purity") == 3;
            case "fiahi_temperature_changed" -> stack.isEdible()
                    && hasNonZeroNumber(stack.getTag(), "temperature", "food_temperature", "fiahi");
            case "food_pouch_loaded" -> id(stack).contains("food_pouch") && distinctStoredFoods(stack.getTag()) >= 2;
            case "tempered_waterskin" -> id(stack).contains("waterskin")
                    && hasNonZeroNumber(stack.getTag(), "temperature", "temperature_value", "waterskin_temperature");
            case "armor_with_inserted_insulation" -> hasKeyLike(stack.getTag(), "insulat");
            case "any_tcon_sand_cast" -> inTag(stack, "tconstruct:casts/sand") || id(stack).contains("sand_cast");
            case "any_tcon_permanent_cast" -> inTag(stack, "tconstruct:casts/gold")
                    || id(stack).contains("gold_cast") || id(stack).contains("red_sand_cast");
            case "tcon_functional_metal_part" -> isFunctionalPart(stack) && containsMetalMaterial(stack.getTag());
            case "tcon_tool_with_metal_functional_part" -> isTconstructTool(stack) && functionalMaterialIsMetal(stack.getTag());
            default -> false;
        };
    }

    private static String id(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().toLowerCase(Locale.ROOT);
    }

    private static boolean inTag(ItemStack stack, String id) {
        return stack.is(TagKey.create(BuiltInRegistries.ITEM.key(), new ResourceLocation(id)));
    }

    private static boolean isTconstructTool(ItemStack stack) {
        String id = id(stack);
        return id.startsWith("tconstruct:") && !id.contains("part") && !id.contains("cast") && stack.getTag() != null
                && (stack.getTag().contains("tic_materials") || stack.getTag().contains("tic_modifiers"));
    }

    private static boolean isFunctionalPart(ItemStack stack) {
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        return path.endsWith("_head") || path.endsWith("_blade") || path.endsWith("_plate")
                || path.endsWith("_bowlimb") || path.endsWith("_limb") || path.endsWith("_maille")
                || path.endsWith("_shield_core") || path.endsWith("_tool_part");
    }

    private static boolean functionalMaterialIsMetal(CompoundTag tag) {
        if (tag == null) return false;
        Tag materials = findTagLike(tag, "tic_materials");
        if (materials instanceof ListTag list && !list.isEmpty()) {
            // TConstruct serializes functional/head materials first; handles and bindings follow.
            return stringIsMetal(list.get(0).getAsString());
        }
        return false;
    }

    private static boolean containsMetalMaterial(Tag tag) {
        if (tag == null) return false;
        if (!(tag instanceof CompoundTag) && stringIsMetal(tag.getAsString())) return true;
        if (tag instanceof CompoundTag compound) {
            for (String key : compound.getAllKeys()) if (containsMetalMaterial(compound.get(key))) return true;
        } else if (tag instanceof ListTag list) {
            for (Tag child : list) if (containsMetalMaterial(child)) return true;
        }
        return false;
    }

    private static boolean stringIsMetal(String value) {
        String lower = value.toLowerCase(Locale.ROOT);
        return METALS.stream().anyMatch(metal -> lower.equals(metal) || lower.endsWith(":" + metal));
    }

    private static int integerValue(CompoundTag tag, String... fragments) {
        if (tag == null) return Integer.MIN_VALUE;
        for (String key : tag.getAllKeys()) {
            String lower = key.toLowerCase(Locale.ROOT);
            for (String fragment : fragments) {
                if (lower.contains(fragment) && tag.contains(key, Tag.TAG_ANY_NUMERIC)) return tag.getInt(key);
            }
            Tag child = tag.get(key);
            if (child instanceof CompoundTag nested) {
                int found = integerValue(nested, fragments);
                if (found != Integer.MIN_VALUE) return found;
            }
        }
        return Integer.MIN_VALUE;
    }

    private static boolean hasNonZeroNumber(CompoundTag tag, String... fragments) {
        int value = integerValue(tag, fragments);
        return value != Integer.MIN_VALUE && value != 0;
    }

    private static boolean hasKeyLike(Tag tag, String fragment) {
        if (tag instanceof CompoundTag compound) {
            for (String key : compound.getAllKeys()) {
                if (key.toLowerCase(Locale.ROOT).contains(fragment)) return true;
                if (hasKeyLike(compound.get(key), fragment)) return true;
            }
        } else if (tag instanceof ListTag list) {
            for (Tag child : list) if (hasKeyLike(child, fragment)) return true;
        }
        return false;
    }

    private static Tag findTagLike(Tag tag, String fragment) {
        if (tag instanceof CompoundTag compound) {
            for (String key : compound.getAllKeys()) {
                if (key.toLowerCase(Locale.ROOT).contains(fragment)) return compound.get(key);
                Tag found = findTagLike(compound.get(key), fragment);
                if (found != null) return found;
            }
        } else if (tag instanceof ListTag list) {
            for (Tag child : list) {
                Tag found = findTagLike(child, fragment);
                if (found != null) return found;
            }
        }
        return null;
    }

    private static int distinctStoredFoods(Tag tag) {
        Set<String> items = new HashSet<>();
        collectStoredItemIds(tag, items);
        return items.size();
    }

    private static void collectStoredItemIds(Tag tag, Set<String> result) {
        if (tag instanceof CompoundTag compound) {
            if (compound.contains("id", Tag.TAG_STRING) && compound.contains("Count", Tag.TAG_ANY_NUMERIC)) {
                String item = compound.getString("id");
                if (!item.isBlank() && compound.getInt("Count") > 0) result.add(item);
            }
            for (String key : compound.getAllKeys()) collectStoredItemIds(compound.get(key), result);
        } else if (tag instanceof ListTag list) {
            for (Tag child : list) collectStoredItemIds(child, result);
        }
    }
}
