package io.github.btmfixes.compat;

import io.github.btmfixes.BoundToMatterFixes;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class RegolithFarmlandPalette {
    private static final Logger LOGGER = LogManager.getLogger();

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, BoundToMatterFixes.MOD_ID);
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, BoundToMatterFixes.MOD_ID);

    private static final List<RegolithFarmlandDefinitions.Entry> ENTRIES = RegolithFarmlandDefinitions.entries();
    private static final Map<ResourceLocation, RegolithFarmlandDefinitions.Entry> BY_GRASSY_REGOLITH = new LinkedHashMap<>();
    private static final Map<ResourceLocation, RegistryObject<Block>> FARMLAND_BLOCKS = new LinkedHashMap<>();

    static {
        for (RegolithFarmlandDefinitions.Entry entry : ENTRIES) {
            BY_GRASSY_REGOLITH.put(entry.grassyRegolithId(), entry);
            final RegistryObject<Block> block = BLOCKS.register(entry.farmlandId().getPath(), () ->
                    new RegolithFarmlandBlock(resolvePlainRegolithBlock(entry.plainRegolithId())));
            FARMLAND_BLOCKS.put(entry.farmlandId(), block);
            ITEMS.register(entry.farmlandId().getPath(), () -> new BlockItem(block.get(), new Item.Properties()));
        }
    }

    private RegolithFarmlandPalette() {
    }

    public static List<RegolithFarmlandDefinitions.Entry> entries() {
        return ENTRIES;
    }

    public static Optional<RegolithFarmlandDefinitions.Entry> lookupByGrassyRegolith(final ResourceLocation blockId) {
        return Optional.ofNullable(BY_GRASSY_REGOLITH.get(blockId));
    }

    public static Optional<Block> getFarmlandBlock(final ResourceLocation farmlandId) {
        final RegistryObject<Block> registryObject = FARMLAND_BLOCKS.get(farmlandId);
        return registryObject == null ? Optional.empty() : Optional.of(registryObject.get());
    }

    private static Block resolvePlainRegolithBlock(final ResourceLocation blockId) {
        final Block block = ForgeRegistries.BLOCKS.getValue(blockId);
        if (block == null || block == Blocks.AIR) {
            LOGGER.warn("Falling back to dirt for missing regolith source block {}", blockId);
            return Blocks.DIRT;
        }
        return block;
    }
}
