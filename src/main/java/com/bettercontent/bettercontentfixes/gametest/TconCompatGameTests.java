package com.bettercontent.bettercontentfixes.gametest;

import com.bettercontent.bettercontentfixes.BetterContentFixes;
import com.bettercontent.bettercontentfixes.compat.tconstruct.TconLoginToolSync;
import com.bettercontent.bettercontentfixes.compat.tconstruct.polymorph.CraftingStationRecipeData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.tables.block.entity.table.CraftingStationBlockEntity;

@PrefixGameTestTemplate(false)
public final class TconCompatGameTests {
    private static final ResourceLocation PICKAXE = new ResourceLocation("tconstruct", "pickaxe");
    private static final ResourceLocation CRAFTING_STATION =
            new ResourceLocation("tconstruct", "crafting_station");
    private static final ResourceLocation FIRST_CONFLICT =
            new ResourceLocation(BetterContentFixes.MOD_ID, "gametest_station_first");
    private static final ResourceLocation SECOND_CONFLICT =
            new ResourceLocation(BetterContentFixes.MOD_ID, "gametest_station_second");
    private static final ResourceLocation INVALID_RECIPE =
            new ResourceLocation(BetterContentFixes.MOD_ID, "gametest_station_invalid");

    private TconCompatGameTests() {
    }

    @GameTest(templateNamespace = BetterContentFixes.MOD_ID, template = "empty")
    public static void loginRebuildPreservesOwnedToolData(final GameTestHelper helper) {
        final Item item = ForgeRegistries.ITEMS.getValue(PICKAXE);
        if (item == null || item == Items.AIR) {
            helper.fail("Missing required TConstruct pickaxe");
            return;
        }

        helper.assertTrue(
                !TconLoginToolSync.rebuildStack(new ItemStack(Items.IRON_PICKAXE)),
                "Non-TCon tools must not be changed");
        helper.assertTrue(
                !TconLoginToolSync.rebuildStack(new ItemStack(item)),
                "Unmaterialized TCon templates must not be changed");

        final ItemStack stack = new ItemStack(item);
        final CompoundTag tag = new CompoundTag();
        final ListTag materials = new ListTag();
        materials.add(StringTag.valueOf("tconstruct:copper"));
        materials.add(StringTag.valueOf("tconstruct:wood"));
        materials.add(StringTag.valueOf("tconstruct:wood"));
        tag.put(ToolStack.TAG_MATERIALS, materials.copy());
        tag.putBoolean(ToolStack.TAG_BROKEN, false);
        tag.putInt("better_content_fixes:test_marker", 42);
        final CompoundTag staleStats = new CompoundTag();
        staleStats.putFloat("tconstruct:durability", 1.0F);
        tag.put("tic_stats", staleStats);
        stack.setTag(tag);
        // Simulate the persisted player-owned damage after normal item-tag validation.
        stack.getOrCreateTag().putInt("Damage", 7);

        helper.assertTrue(TconLoginToolSync.rebuildStack(stack), "Materialized TCon tool must rebuild");
        final CompoundTag rebuilt = stack.getTag();
        helper.assertTrue(rebuilt != null, "Rebuilt tool must retain NBT");
        helper.assertTrue(
                rebuilt.getInt("Damage") == 7,
                "Rebuild must preserve current damage (observed " + rebuilt.getInt("Damage") + ")");
        helper.assertTrue(!rebuilt.getBoolean(ToolStack.TAG_BROKEN), "Rebuild must preserve usable state");
        helper.assertTrue(
                rebuilt.getInt("better_content_fixes:test_marker") == 42,
                "Rebuild must preserve unrelated root data");
        helper.assertTrue(
                rebuilt.getList(ToolStack.TAG_MATERIALS, Tag.TAG_STRING).equals(materials),
                "Rebuild must preserve authored materials");
        helper.assertTrue(
                rebuilt.getCompound("tic_stats").getFloat("tconstruct:durability") > 1.0F,
                "Rebuild must replace stale durability with current server stats");
        helper.succeed();
    }

    @GameTest(templateNamespace = BetterContentFixes.MOD_ID, template = "empty")
    public static void craftingStationSelectionValidatesAndPersists(final GameTestHelper helper) {
        final Block stationBlock = ForgeRegistries.BLOCKS.getValue(CRAFTING_STATION);
        if (stationBlock == null || stationBlock == Blocks.AIR) {
            helper.fail("Missing required TConstruct crafting station");
            return;
        }

        final BlockPos relativePos = new BlockPos(2, 2, 2);
        helper.setBlock(relativePos, stationBlock.defaultBlockState());
        final BlockEntity blockEntity = helper.getLevel().getBlockEntity(helper.absolutePos(relativePos));
        if (!(blockEntity instanceof CraftingStationBlockEntity station)) {
            helper.fail("TConstruct crafting station did not create its block entity");
            return;
        }
        station.setItem(0, new ItemStack(Items.DIRT));

        final CraftingRecipe first = shapeless(FIRST_CONFLICT, Items.DIRT, Items.APPLE);
        final CraftingRecipe second = shapeless(SECOND_CONFLICT, Items.DIRT, Items.DIAMOND);
        final CraftingRecipe invalid = shapeless(INVALID_RECIPE, Items.COBBLESTONE, Items.STICK);
        final CraftingStationRecipeData data = new CraftingStationRecipeData(station);
        data.selectRecipe(first);
        data.selectRecipe(second);

        helper.assertTrue(
                data.getSelectedRecipe().filter(recipe -> recipe.getId().equals(SECOND_CONFLICT)).isPresent(),
                "Alternate matching recipe must become the station selection");
        helper.assertTrue(
                station.calcResult(null).is(Items.DIAMOND),
                "Selected conflicting recipe must update the station result");

        data.selectRecipe(invalid);
        helper.assertTrue(
                data.getSelectedRecipe().filter(recipe -> recipe.getId().equals(SECOND_CONFLICT)).isPresent(),
                "Non-matching recipe must not replace the station selection");

        final CraftingStationRecipeData restored = new CraftingStationRecipeData(station);
        restored.readNBT(data.writeNBT());
        helper.assertTrue(
                restored.getLoadedRecipe().filter(SECOND_CONFLICT::equals).isPresent(),
                "Selected station recipe ID must persist in capability data");
        helper.succeed();
    }

    private static CraftingRecipe shapeless(
            final ResourceLocation recipeId,
            final Item ingredient,
            final Item result
    ) {
        return new ShapelessRecipe(
                recipeId,
                "",
                CraftingBookCategory.MISC,
                new ItemStack(result),
                NonNullList.of(Ingredient.EMPTY, Ingredient.of(ingredient)));
    }
}
