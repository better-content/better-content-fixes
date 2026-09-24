package com.bettercontent.bettercontentfixes.compat.epicfight;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.Style;

public final class StyleDiscovery {
    private static final String DATA_KEY = "better_content_fixes.epicfight_style_discoveries";

    private StyleDiscovery() {
    }

    public static void observeAttackStart(final ServerPlayerPatch patch) {
        final ServerPlayer player = patch.getOriginal();
        if (player == null || player.level().isClientSide) {
            return;
        }

        // Snapshot the held stack and its resolved Epic Fight capability now. Later hand
        // swaps cannot change the identity recorded for this attack.
        final InteractionHand hand = patch.getAttackingHand() == null
                ? InteractionHand.MAIN_HAND : patch.getAttackingHand();
        final ItemStack held = player.getItemInHand(hand).copy();
        if (held.isEmpty()) {
            return;
        }
        final CapabilityItem capability = patch.getHoldingItemCapability(hand);
        if (capability == null || capability.isEmpty()) {
            return;
        }
        final Style style = capability.getStyle(patch);
        final ResourceLocation itemId = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(held.getItem());
        final String key = StyleDiscoveryPolicy.key(itemId, style == null ? null : style.toString());
        if (key == null) {
            return;
        }

        final CompoundTag persistent = player.getPersistentData();
        final ListTag entries = persistent.getList(DATA_KEY, Tag.TAG_STRING);
        final java.util.List<String> discovered = new java.util.ArrayList<>(entries.size());
        for (int i = 0; i < entries.size(); i++) {
            discovered.add(entries.getString(i));
        }
        if (!StyleDiscoveryPolicy.isNew(key, discovered)) {
            return;
        }
        entries.add(StringTag.valueOf(key));
        persistent.put(DATA_KEY, entries);

        final Component weaponName = held.getHoverName().copy();
        final String styleName = Style.ENUM_MANAGER.toTranslated(style);
        player.displayClientMessage(Component.literal("STYLE DISCOVERED · " + styleName + " · "
                + weaponName.getString() + " — attack with this weapon to learn its moves"), true);
    }
}
