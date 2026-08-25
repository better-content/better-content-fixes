package com.bettercontent.bettercontentfixes.mixin.valkyrienskies;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.vibrations.VibrationInfo;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

@Mixin(VibrationSystem.Ticker.class)
public interface VibrationSystemTickerMixin {
    @WrapOperation(
            method = "receiveVibration",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/gameevent/vibrations/VibrationInfo;pos()Lnet/minecraft/world/phys/Vec3;"
            ),
            require = 1
    )
    static Vec3 betterContentFixes$destinationWorldPosition(
            final VibrationInfo info,
            final Operation<Vec3> original,
            @Local(argsOnly = true) final ServerLevel level
    ) {
        return VSGameUtilsKt.toWorldCoordinates(level, original.call(info));
    }

    @WrapOperation(
            method = "receiveVibration",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/gameevent/vibrations/VibrationSystem$User;getPositionSource()Lnet/minecraft/world/level/gameevent/PositionSource;"
            ),
            require = 1
    )
    static PositionSource betterContentFixes$destinationSourcePosition(
            final VibrationSystem.User user,
            final Operation<PositionSource> original,
            @Local(argsOnly = true) final ServerLevel level
    ) {
        final PositionSource source = original.call(user);
        final Optional<Vec3> position = source.getPosition(level);
        if (position.isEmpty()) {
            return source;
        }
        return new BlockPositionSource(BlockPos.containing(
                (Position) VSGameUtilsKt.toWorldCoordinates(level, position.get())
        ));
    }

    @WrapOperation(
            method = "tryReloadVibrationParticle",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/gameevent/vibrations/VibrationSystem$User;getPositionSource()Lnet/minecraft/world/level/gameevent/PositionSource;"
            ),
            require = 1
    )
    static PositionSource betterContentFixes$reloadDestinationSourcePosition(
            final VibrationSystem.User user,
            final Operation<PositionSource> original,
            @Local(argsOnly = true) final ServerLevel level
    ) {
        return betterContentFixes$destinationSourcePosition(user, original, level);
    }
}
