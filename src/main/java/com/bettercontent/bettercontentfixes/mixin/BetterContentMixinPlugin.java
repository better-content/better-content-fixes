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

    @Override
    public void onLoad(final String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(final String targetClassName, final String mixinClassName) {
        if (!CRAFTING_STATION_POLYMORPH_MIXIN.equals(mixinClassName)) return true;

        final LoadingModList mods = FMLLoader.getLoadingModList();
        return mods != null
                && mods.getModFileById("tconstruct") != null
                && mods.getModFileById("polymorph") != null;
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
