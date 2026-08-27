package com.bettercontent.bettercontentfixes.mixin.weather2;

import com.bettercontent.bettercontentfixes.compat.Weather2ShaderFogCompat;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "weather2.weathersystem.fog.FogAdjuster", remap = false)
public abstract class FogAdjusterMixin {
    @WrapOperation(
            method = {"onFogColors", "onFogRender"},
            at = @At(
                    value = "INVOKE",
                    target = "Lweather2/client/SceneEnhancer;isFogOverridding()Z"),
            remap = false)
    private boolean better_content_fixes$letShadersOwnWeatherFog(final Operation<Boolean> original) {
        if (Weather2ShaderFogCompat.shouldDisableWeather2FogOverride()) {
            return false;
        }
        return original.call();
    }
}
