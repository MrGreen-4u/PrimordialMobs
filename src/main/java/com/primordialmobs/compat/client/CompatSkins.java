package com.primordialmobs.compat.client;

import com.github.alexmodguy.alexscaves.server.entity.living.DinosaurEntity;
import com.primordialmobs.client.render.entity.DinosaurSkinTextures;
import com.primordialmobs.compat.PMRecolorable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

/**
 * Texture selection for compat-mode recoloured variants.
 *
 * Replaces the old {@code CompatRenderers}, which registered replacement renderer subclasses from
 * {@code FMLClientSetupEvent}. That never worked reliably: Alex's Caves registers its dinosaur renderers the
 * same way and in the same phase, and Forge dispatches that event to mods IN PARALLEL, so which
 * {@code EntityRenderers.register} call landed last was a race — {@code ordering="AFTER"} in mods.toml does
 * not govern it. (Moving to {@code EntityRenderersEvent.RegisterRenderers} would be worse: Forge fires it
 * from the Minecraft constructor, BEFORE mod loading.) The renderer mixins that call into this class inject
 * straight into Alex's Caves' {@code getTextureLocation}, which has no ordering to lose.
 *
 * The composed names match standalone exactly: {@code <mob>[_retro|_tectonic][_baby|_elder]_variant}.
 */
public final class CompatSkins {

    private CompatSkins() {
    }

    /** True when this dinosaur carries our recoloured-variant flag (see {@link PMRecolorable}). */
    public static boolean isRecolored(Entity entity) {
        return entity instanceof PMRecolorable recolorable && recolorable.pm_isRecolored();
    }

    /**
     * @param base  the mob's texture base name, e.g. "grottoceratops"
     * @param extra "_baby" / "_elder" / "" — the size or age qualifier, applied AFTER the skin suffix,
     *              because that is the order the shipped files use (grottoceratops_retro_baby_variant).
     */
    public static ResourceLocation texture(DinosaurEntity entity, String base, String extra) {
        return DinosaurSkinTextures.get(base + DinosaurSkinTextures.skinSuffix(entity.getAltSkin()) + extra + "_variant");
    }

    /**
     * Alex's Caves' two custom-name easter eggs ("alan" on a Vallumraptor, "princess" on a Tremorsaurus)
     * have dedicated textures with no recoloured counterpart, so they win over the variant — exactly as
     * they do standalone.
     */
    public static boolean hasEasterEggName(Entity entity, String name) {
        return entity.hasCustomName() && name.equalsIgnoreCase(entity.getName().getString());
    }
}
