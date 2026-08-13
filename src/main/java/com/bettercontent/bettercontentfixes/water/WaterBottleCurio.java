package com.bettercontent.bettercontentfixes.water;

import dev.ghen.thirst.api.ThirstHelper;
import dev.ghen.thirst.content.purity.WaterPurity;
import dev.ghen.thirst.content.thirst.PlayerThirst;
import dev.ghen.thirst.foundation.common.capability.ModCapabilities;
import com.bettercontent.bettercontentfixes.BetterContentFixes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import top.theillusivec4.curios.api.CuriosApi;

public final class WaterBottleCurio {
    public static final String SLOT = "water";
    public static final ResourceLocation PREDICATE = new ResourceLocation(BetterContentFixes.MOD_ID, "water_bottle");

    private WaterBottleCurio() {}

    public static void registerPredicate() {
        CuriosApi.registerCurioPredicate(PREDICATE, result -> isWaterBottle(result.stack()));
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide || event.player.tickCount % 10 != 0) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        CuriosApi.getCuriosInventory(player).ifPresent(handler -> handler.getStacksHandler(SLOT).ifPresent(slot -> {
            ItemStack stack = slot.getStacks().getStackInSlot(0);
            if (!isWaterBottle(stack) || !ThirstHelper.itemRestoresThirst(stack)) return;
            player.getCapability(ModCapabilities.PLAYER_THIRST).ifPresent(thirst -> {
                int restored = Math.max(0, ThirstHelper.getThirst(stack));
                if (restored == 0 || thirst.getThirst() > 20 - restored) return;
                ItemStack drink = stack.copy();
                drink.setCount(1);
                PlayerThirst.drink(drink, player);
                stack.shrink(1);
                ItemStack empty = new ItemStack(Items.GLASS_BOTTLE);
                if (!player.getInventory().add(empty)) player.drop(empty, false);
                slot.getStacks().setStackInSlot(0, stack);
            });
        }));
    }

    public static boolean isWaterBottle(ItemStack stack) {
        return stack.is(Items.POTION)
                && PotionUtils.getPotion(stack) == Potions.WATER
                && WaterPurity.isWaterFilledContainer(stack);
    }
}
