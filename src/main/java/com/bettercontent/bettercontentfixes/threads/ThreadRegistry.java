package com.bettercontent.bettercontentfixes.threads;

import com.bettercontent.bettercontentfixes.BetterContentFixes;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ThreadRegistry {
    public static final DeferredRegister<Item> ITEMS=DeferredRegister.create(ForgeRegistries.ITEMS,BetterContentFixes.MOD_ID);
    public static final RegistryObject<Item> FACSIMILE=ITEMS.register("thread_facsimile",()->new ThreadFacsimileItem(new Item.Properties().stacksTo(16)));
    private ThreadRegistry() {}
}
