package com.bettercontent.bettercontentfixes.mixin.ftbquests;

import com.bettercontent.bettercontentfixes.quest.QuestIntegration;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Quest.class, remap = false)
public abstract class QuestVisibilityMixin {
    @Inject(method = "isVisible", at = @At("HEAD"), cancellable = true)
    private void better_content_fixes$visibility(TeamData data, CallbackInfoReturnable<Boolean> cir) {
        Quest self = (Quest) (Object) this;
        if (QuestIntegration.forceVisible(self, data)) cir.setReturnValue(true);
        else if (QuestIntegration.forceHidden(self, data)) cir.setReturnValue(false);
    }
}
