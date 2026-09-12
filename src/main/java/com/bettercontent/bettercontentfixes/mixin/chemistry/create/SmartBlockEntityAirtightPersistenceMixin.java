package com.bettercontent.bettercontentfixes.mixin.chemistry.create;

import com.bettercontent.bettercontentfixes.chemistry.AirtightUpgradeHolder;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SmartBlockEntity.class)
abstract class SmartBlockEntityAirtightPersistenceMixin {
    @Unique private static final String BETTER_CONTENT_FIXES$AIRTIGHT_TAG = "better_content_fixes:Airtight";

    @Inject(method = {"saveAdditional", "m_183515_"}, at = @At("TAIL"), require = 1, remap = false)
    private void betterContentFixes$writeAirtight(final CompoundTag tag, final CallbackInfo callback) {
        if ((Object) this instanceof AirtightUpgradeHolder holder) {
            tag.putBoolean(BETTER_CONTENT_FIXES$AIRTIGHT_TAG, holder.isAirtight());
        }
    }

    @Inject(method = {"load", "m_142466_"}, at = @At("TAIL"), require = 1, remap = false)
    private void betterContentFixes$readAirtight(final CompoundTag tag, final CallbackInfo callback) {
        if ((Object) this instanceof AirtightUpgradeHolder holder) {
            holder.betterContentFixes$setAirtight(tag.getBoolean(BETTER_CONTENT_FIXES$AIRTIGHT_TAG));
        }
    }
}
