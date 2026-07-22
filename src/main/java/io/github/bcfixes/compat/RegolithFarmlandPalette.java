package io.github.bcfixes.compat;

import io.github.bcfixes.BetterContentFixes;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class RegolithFarmlandPalette {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, BetterContentFixes.MOD_ID);
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, BetterContentFixes.MOD_ID);

    private static final List<RegolithFarmlandDefinitions.Entry> ENTRIES = RegolithFarmlandDefinitions.entries();
    private static final Map<ResourceLocation, RegolithFarmlandDefinitions.Entry> BY_TILLABLE_REGOLITH = new LinkedHashMap<>();
    private static final Map<ResourceLocation, RegistryObject<Block>> FARMLAND_BLOCKS = new LinkedHashMap<>();

    static {
        for (RegolithFarmlandDefinitions.Entry entry : ENTRIES) {
            BY_TILLABLE_REGOLITH.put(entry.grassyRegolithId(), entry);
            BY_TILLABLE_REGOLITH.put(entry.plainRegolithId(), entry);
            final RegistryObject<Block> block = BLOCKS.register(entry.farmlandId().getPath(), () ->
                    new RegolithFarmlandBlock(entry.plainRegolithId()));
            FARMLAND_BLOCKS.put(entry.farmlandId(), block);
            ITEMS.register(entry.farmlandId().getPath(), () -> new BlockItem(block.get(), new Item.Properties()));
        }
    }

    private RegolithFarmlandPalette() {
    }

    public static List<RegolithFarmlandDefinitions.Entry> entries() {
        return ENTRIES;
    }

    public static Optional<RegolithFarmlandDefinitions.Entry> lookupByTillableRegolith(final ResourceLocation blockId) {
        return Optional.ofNullable(BY_TILLABLE_REGOLITH.get(blockId));
    }

    public static Optional<Block> getFarmlandBlock(final ResourceLocation farmlandId) {
        final RegistryObject<Block> registryObject = FARMLAND_BLOCKS.get(farmlandId);
        return registryObject == null ? Optional.empty() : Optional.of(registryObject.get());
    }
}
