package com.bettercontent.bettercontentfixes.mixin.jsonthings;

import net.minecraft.server.packs.PackType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PackType.class, priority = 2000)
public abstract class PackTypeMixin {
    @Inject(method = "<clinit>", at = @At("TAIL"), require = 1)
    private static void betterContentFixes$initializeVanillaPackRoots(final CallbackInfo ci) {
        try {
            Class.forName(
                    "net.minecraft.server.packs.VanillaPackResourcesBuilder",
                    true,
                    PackType.class.getClassLoader()
            );
        } catch (ClassNotFoundException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }
}
