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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class BurntGrassPalette {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final ResourceLocation VANILLA_BURNT_GRASS_ID =
            ResourceLocation.fromNamespaceAndPath("burnt", "burnt_grass");

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, BetterContentFixes.MOD_ID);
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, BetterContentFixes.MOD_ID);

    private static final List<BurntGrassReplacementDefinitions.Entry> ENTRIES = BurntGrassReplacementDefinitions.entries();
    private static final Map<ResourceLocation, BurntGrassReplacementDefinitions.Entry> BY_SOURCE = new LinkedHashMap<>();
    private static final Map<ResourceLocation, RegistryObject<Block>> CUSTOM_BLOCKS = new LinkedHashMap<>();

    static {
        for (BurntGrassReplacementDefinitions.Entry entry : ENTRIES) {
            BY_SOURCE.put(entry.sourceId(), entry);
            if (!BetterContentFixes.MOD_ID.equals(entry.targetId().getNamespace())) {
                continue;
            }
            RegistryObject<Block> block = BLOCKS.register(entry.targetId().getPath(),
                    () -> new Block(resolveSourceProperties(entry.sourceId())));
            CUSTOM_BLOCKS.put(entry.targetId(), block);
            ITEMS.register(entry.targetId().getPath(), () -> new BlockItem(block.get(), new Item.Properties()));
        }
    }

    private BurntGrassPalette() {
    }

    public static List<BurntGrassReplacementDefinitions.Entry> entries() {
        return ENTRIES;
    }

    public static boolean isVanillaBurntGrass(final ResourceLocation blockId) {
        return VANILLA_BURNT_GRASS_ID.equals(blockId);
    }

    public static Optional<BurntGrassReplacementDefinitions.Entry> lookup(final ResourceLocation sourceId) {
        return Optional.ofNullable(BY_SOURCE.get(sourceId));
    }

    public static Optional<BlockState> resolveReplacementState(final BlockState sourceState) {
        ResourceLocation sourceId = ForgeRegistries.BLOCKS.getKey(sourceState.getBlock());
        if (sourceId == null) {
            return Optional.empty();
        }
        BurntGrassReplacementDefinitions.Entry entry = BY_SOURCE.get(sourceId);
        if (entry == null) {
            return Optional.empty();
        }
        Block block;
        if (BetterContentFixes.MOD_ID.equals(entry.targetId().getNamespace())) {
            RegistryObject<Block> registryObject = CUSTOM_BLOCKS.get(entry.targetId());
            if (registryObject == null) {
                LOGGER.warn("Missing custom burnt grass registry object for {}", entry.targetId());
                return Optional.empty();
            }
            block = registryObject.get();
        } else {
            block = ForgeRegistries.BLOCKS.getValue(entry.targetId());
            if (block == null) {
                LOGGER.warn("Missing target burnt grass block {}", entry.targetId());
                return Optional.empty();
            }
        }
        return Optional.of(block.defaultBlockState());
    }

    private static BlockBehaviour.Properties resolveSourceProperties(final ResourceLocation sourceId) {
        Block sourceBlock = ForgeRegistries.BLOCKS.getValue(sourceId);
        if (sourceBlock == null || sourceBlock == Blocks.AIR) {
            LOGGER.warn("Falling back to grass properties for missing burnt source block {}", sourceId);
            return BlockBehaviour.Properties.copy(Blocks.GRASS_BLOCK);
        }
        return BlockBehaviour.Properties.copy(sourceBlock);
    }

}
