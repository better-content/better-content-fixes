package com.bettercontent.bettercontentfixes.mixin;

import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public final class BetterContentMixinPlugin implements IMixinConfigPlugin {
    private static final String CRAFTING_STATION_POLYMORPH_MIXIN =
            "com.bettercontent.bettercontentfixes.mixin.tconstruct.CraftingStationPolymorphMixin";
    private static final String EPIC_FIGHT_VS_MIXIN_PREFIX =
            "com.bettercontent.bettercontentfixes.mixin.epicfightvs.";
    private static final String REHOOKED_MIXIN_PREFIX =
            "com.bettercontent.bettercontentfixes.mixin.rehooked.";
    private static final String ALI_MIXIN_PREFIX =
            "com.bettercontent.bettercontentfixes.mixin.ali.";
    private static final String APOTHEOSIS_MIXIN_PREFIX =
            "com.bettercontent.bettercontentfixes.mixin.apotheosis.";
    private static final String PATCHOULI_MIXIN_PREFIX =
            "com.bettercontent.bettercontentfixes.mixin.patchouli.";

    @Override
    public void onLoad(final String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(final String targetClassName, final String mixinClassName) {
        final LoadingModList mods = FMLLoader.getLoadingModList();
        if (CRAFTING_STATION_POLYMORPH_MIXIN.equals(mixinClassName)) {
            return mods != null
                    && mods.getModFileById("tconstruct") != null
                    && mods.getModFileById("polymorph") != null;
        }
        if (mixinClassName.startsWith(EPIC_FIGHT_VS_MIXIN_PREFIX)) {
            return mods != null
                    && mods.getModFileById("epicfight") != null
                    && mods.getModFileById("valkyrienskies") != null;
        }
        if (mixinClassName.startsWith(REHOOKED_MIXIN_PREFIX)) {
            return mods != null && mods.getModFileById("rehooked") != null;
        }
        if (mixinClassName.startsWith(ALI_MIXIN_PREFIX)) {
            return mods != null && mods.getModFileById("ali") != null;
        }
        if (mixinClassName.startsWith(APOTHEOSIS_MIXIN_PREFIX)) {
            return mods != null && mods.getModFileById("apotheosis") != null;
        }
        if (mixinClassName.startsWith(PATCHOULI_MIXIN_PREFIX)) {
            return mods != null && mods.getModFileById("patchouli") != null;
        }
        return true;
    }

    @Override
    public void acceptTargets(final Set<String> myTargets, final Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(
            final String targetClassName,
            final ClassNode targetClass,
            final String mixinClassName,
            final IMixinInfo mixinInfo
    ) {
    }

    @Override
    public void postApply(
            final String targetClassName,
            final ClassNode targetClass,
            final String mixinClassName,
            final IMixinInfo mixinInfo
    ) {
    }
}
