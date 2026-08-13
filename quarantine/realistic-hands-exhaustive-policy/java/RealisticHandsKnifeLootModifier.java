package com.bettercontent.bettercontentfixes.compat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Iterator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

public final class RealisticHandsKnifeLootModifier extends LootModifier {
    private static final ResourceLocation STRAW_ID = new ResourceLocation("farmersdelight", "straw");
    public static final Codec<RealisticHandsKnifeLootModifier> CODEC =
            RecordCodecBuilder.create(instance -> codecStart(instance).apply(instance, RealisticHandsKnifeLootModifier::new));

    public RealisticHandsKnifeLootModifier(final LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(
            final ObjectArrayList<ItemStack> generatedLoot,
            final LootContext context
    ) {
        final BlockState state = context.getParamOrNull(LootContextParams.BLOCK_STATE);
        final ItemStack tool = context.getParamOrNull(LootContextParams.TOOL);
        if (state == null || tool == null || tool.isEmpty()) {
            return generatedLoot;
        }
        if (!state.is(RealisticHandsTags.KNIFE) || !tool.is(RealisticHandsTags.KNIFE_TOOLS)) {
            return generatedLoot;
        }

        if (state.is(RealisticHandsTags.KNIFE_STRAW)) {
            final Iterator<ItemStack> iterator = generatedLoot.iterator();
            while (iterator.hasNext()) {
                final ItemStack stack = iterator.next();
                if (stack.is(Items.WHEAT_SEEDS)) {
                    iterator.remove();
                }
            }
            final Item straw = ForgeRegistries.ITEMS.getValue(STRAW_ID);
            if (straw != null) {
                generatedLoot.add(new ItemStack(straw));
            }
        }

        if (state.is(RealisticHandsTags.KNIFE_EXTRA_STICKS) && context.getRandom().nextFloat() < 0.5F) {
            generatedLoot.add(new ItemStack(Items.STICK));
        }

        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
