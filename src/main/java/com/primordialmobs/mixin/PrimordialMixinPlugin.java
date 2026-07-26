package com.primordialmobs.mixin;

import net.minecraftforge.fml.loading.FMLLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Gates the {@code com.primordialmobs.mixin.compat.*} mixins on the full Alex's Caves mod being present.
 *
 * Those mixins target Alex's Caves' OWN classes (DinosaurEntity, RelicheirusEntity, ...), which do not exist
 * standalone. Without this gate Mixin would fail to resolve the target class and, with
 * {@code injectors.defaultRequire = 1}, abort the whole game — so the check must happen before the mixin is
 * ever prepared, which is exactly what {@link #shouldApplyMixin} is for.
 *
 * The presence check uses the loading mod list rather than {@code ModList.isLoaded}: mixin config plugins run
 * during class transformation, long before {@code ModList} exists. Same idiom as
 * {@code PrimordialMobs.ALEXS_CAVES_INSTALLED}, which cannot be reused here because touching that class from
 * a plugin would class-load the mod entrypoint far too early.
 */
public class PrimordialMixinPlugin implements IMixinConfigPlugin {

    /** Prefix (relative to the config's {@code package}) of every mixin that needs Alex's Caves loaded. */
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
