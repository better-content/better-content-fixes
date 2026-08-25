package com.bettercontent.bettercontentfixes.mixin.jsonthings;

import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.VanillaPackResourcesBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(VanillaPackResourcesBuilder.class)
public abstract class VanillaPackResourcesBuilderMixin {
    @Redirect(
            method = "lambda$static$1",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/packs/PackType;values()[Lnet/minecraft/server/packs/PackType;"
            ),
            require = 1
    )
    private static PackType[] betterContentFixes$builtInPackTypes() {
        return new PackType[]{PackType.CLIENT_RESOURCES, PackType.SERVER_DATA};
    }
}
