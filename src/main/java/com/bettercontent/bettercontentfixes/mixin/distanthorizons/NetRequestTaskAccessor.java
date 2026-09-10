package com.bettercontent.bettercontentfixes.mixin.distanthorizons;

import java.util.concurrent.CompletableFuture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(
        targets = "com.seibel.distanthorizons.core.multiplayer.client.AbstractFullDataNetworkRequestQueue$NetRequestTask",
        remap = false)
public interface NetRequestTaskAccessor {
    @Accessor("future")
    CompletableFuture<?> better_content_fixes$getFuture();
}
