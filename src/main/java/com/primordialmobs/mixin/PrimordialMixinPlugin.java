package com.primordialmobs.mixin;

import net.minecraftforge.fml.loading.FMLLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Gates the {@code com.primordialmobs.mixin.compat.*} mixins on Alex's Caves actually being present.
 *
 * Alex's Caves is a mandatory dependency, but mixins are applied BEFORE Forge validates the mod list:
 * if a player installs this add-on without Alex's Caves, an ungated mixin would fail to resolve its
 * target class and (with {@code injectors.defaultRequire = 1}) abort the launch with a mixin crash.
 * Skipping the compat mixins here instead lets the load continue to Forge's friendly
 * "missing dependency: alexscaves" screen.
 *
 * The presence check uses the loading mod list rather than {@code ModList.isLoaded}: mixin config
 * plugins run during class transformation, long before {@code ModList} exists.
 */
public class PrimordialMixinPlugin implements IMixinConfigPlugin {

    /** Prefix (relative to the config's {@code package}) of every mixin that targets an Alex's Caves class. */
    private static final String COMPAT_PACKAGE = "com.primordialmobs.mixin.compat.";

    private static final boolean ALEXS_CAVES_PRESENT =
            FMLLoader.getLoadingModList() != null && FMLLoader.getLoadingModList().getModFileById("alexscaves") != null;

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.startsWith(COMPAT_PACKAGE)) {
            return ALEXS_CAVES_PRESENT;
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
