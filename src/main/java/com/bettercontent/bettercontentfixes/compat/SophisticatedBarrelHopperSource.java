package com.bettercontent.bettercontentfixes.compat;

import net.minecraftforge.items.IItemHandlerModifiable;

public interface SophisticatedBarrelHopperSource {
    boolean betterContent$isBarrel();

    IItemHandlerModifiable betterContent$getInventoryForInputOutput();
}
