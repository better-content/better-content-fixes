package com.bettercontent.bettercontentfixes.compat.tconstruct;

import com.bettercontent.bettercontentfixes.BetterContentFixes;
import com.bettercontent.bettercontentfixes.compat.tconstruct.polymorph.TconPolymorphCompat;
import com.bettercontent.bettercontentfixes.gametest.TconCompatGameTests;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterGameTestsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = BetterContentFixes.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class TconCompatBootstrap {
    private TconCompatBootstrap() {
    }

    @SubscribeEvent
    public static void onCommonSetup(final FMLCommonSetupEvent event) {
        if (!ModList.get().isLoaded("tconstruct")) return;

        MinecraftForge.EVENT_BUS.register(TconLoginToolSync.class);
        if (ModList.get().isLoaded("polymorph")) {
            event.enqueueWork(TconPolymorphCompat::register);
        }
    }

    @SubscribeEvent
    public static void onRegisterGameTests(final RegisterGameTestsEvent event) {
        if (ModList.get().isLoaded("tconstruct") && ModList.get().isLoaded("polymorph")) {
            event.register(TconCompatGameTests.class);
        }
    }
}
