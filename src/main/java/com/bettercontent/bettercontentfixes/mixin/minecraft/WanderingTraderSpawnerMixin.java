package com.bettercontent.bettercontentfixes.mixin.minecraft;

import com.bettercontent.bettercontentfixes.config.BcFixesConfig;
import com.bettercontent.bettercontentfixes.trader.WanderingTraderVisits;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.WanderingTraderSpawner;
import net.minecraft.world.level.storage.ServerLevelData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(WanderingTraderSpawner.class)
public abstract class WanderingTraderSpawnerMixin {
    @Shadow
    @Final
    private ServerLevelData serverLevelData;

    @Invoker("spawn")
    protected abstract boolean betterContentFixes$invokeSpawn(ServerLevel level);

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true, require = 1)
    private void betterContentFixes$runRecurringVisitSchedule(
            final ServerLevel level,
            final boolean spawnEnemies,
            final boolean spawnFriendlies,
            final CallbackInfoReturnable<Integer> callback) {
        if (!BcFixesConfig.wanderingTraderRecurringVisits()) {
            return;
        }
        callback.setReturnValue(WanderingTraderVisits.tickScheduledVisit(
                level,
                serverLevelData,
                this::betterContentFixes$invokeSpawn));
    }

    @ModifyConstant(method = "spawn", constant = @Constant(intValue = 10), require = 1)
    private int betterContentFixes$removeVanillaOneInTenGate(final int vanillaBound) {
        return BcFixesConfig.wanderingTraderRecurringVisits() ? 1 : vanillaBound;
    }
}
