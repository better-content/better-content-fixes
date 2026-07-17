package io.github.bcfixes.compat;

import com.mojang.serialization.Codec;
import io.github.bcfixes.BetterContentFixes;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class RealisticHandsLootModifiers {
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> REGISTRY =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, BetterContentFixes.MOD_ID);

    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> KNIFE_BONUS =
            REGISTRY.register("realistic_hands_knife_bonus", () -> RealisticHandsKnifeLootModifier.CODEC);

    private RealisticHandsLootModifiers() {
    }
}
