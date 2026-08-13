package com.bettercontent.bettercontentfixes.mixin.ftbquests;

import com.bettercontent.bettercontentfixes.quest.QuestIntegration;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TeamData.class, remap = false)
public abstract class TeamDataTaskGateMixin {
    @Inject(method = "setProgress", at = @At("HEAD"), cancellable = true)
    private void better_content_fixes$gateEarlyNativeProgress(Task task, long progress, CallbackInfo ci) {
        if (progress > 0 && QuestIntegration.taskIsLocked(task.id, (TeamData) (Object) this)) ci.cancel();
    }
}
