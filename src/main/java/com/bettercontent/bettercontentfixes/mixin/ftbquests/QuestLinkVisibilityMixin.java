package com.bettercontent.bettercontentfixes.mixin.ftbquests;

import com.bettercontent.bettercontentfixes.quest.QuestIntegration;
import dev.ftb.mods.ftbquests.quest.QuestLink;
import dev.ftb.mods.ftbquests.quest.TeamData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = QuestLink.class, remap = false)
public abstract class QuestLinkVisibilityMixin {
    @Inject(method = "isVisible", at = @At("HEAD"), cancellable = true)
    private void better_content_fixes$preview(TeamData data, CallbackInfoReturnable<Boolean> cir) {
        QuestLink self = (QuestLink) (Object) this;
        if (QuestIntegration.isPreviewChapter(self.getParentID())) cir.setReturnValue(true);
    }
}
