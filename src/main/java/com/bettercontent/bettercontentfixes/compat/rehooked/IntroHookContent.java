package com.bettercontent.bettercontentfixes.compat.rehooked;

import com.bettercontent.bettercontentfixes.BetterContentFixes;
import com.oe.rehooked.data.HookData;
import com.oe.rehooked.data.HookRegistry;
import com.oe.rehooked.item.hook.HookItem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.LinkedHashMap;
import java.util.Map;

/** Registers loot-only ReHooked variants without changing ReHooked's ordinary grapple path. */
public final class IntroHookContent {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, BetterContentFixes.MOD_ID);
    private static final Map<String, RegistryObject<Item>> HOOK_ITEMS = registerItems();

    private IntroHookContent() {
    }

    private static Map<String, RegistryObject<Item>> registerItems() {
        final Map<String, RegistryObject<Item>> items = new LinkedHashMap<>();
        for (IntroHookProfile profile : IntroHookProfile.ALL) {
            items.put(profile.itemId(), ITEMS.register(profile.itemId(),
                    () -> new HookItem(new Item.Properties().stacksTo(1), profile.hookType())));
        }
        return Map.copyOf(items);
    }

    public static void register(final IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        modEventBus.addListener(IntroHookContent::onCommonSetup);
    }

    static void onCommonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> IntroHookProfile.ALL.forEach(profile -> HookRegistry.registerHook(
                profile.hookType(),
                new HookData(
                        profile.hookType(),
                        profile.count(),
                        profile.range(),
                        profile.travelSpeed(),
                        profile.pullSpeed(),
                        false,
                        new ResourceLocation("rehooked", "textures/entity/hook/wood/wood.png"),
                        () -> ParticleTypes.CRIT,
                        1,
                        2,
                        0.1D,
                        4
                )
        )));
    }

    public static RegistryObject<Item> item(final String itemId) {
        return HOOK_ITEMS.get(itemId);
    }
}
