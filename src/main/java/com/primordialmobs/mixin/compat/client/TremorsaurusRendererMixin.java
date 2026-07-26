package com.primordialmobs.mixin.compat.client;

import com.github.alexmodguy.alexscaves.client.render.entity.TremorsaurusRenderer;
import com.github.alexmodguy.alexscaves.server.entity.living.TremorsaurusEntity;
import com.primordialmobs.compat.client.CompatSkins;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Compat only, client only: hands back our recoloured-variant texture for a tremorsaurus that carries the flag.
 *
 * Injected into Alex's Caves' own {@code getTextureLocation} rather than registering a replacement renderer,
 * because renderer registration between the two mods is a parallel-dispatch race — see
 * {@link CompatSkins}. {@code remap = false}: this is Alex's Caves' specialised override, whose name
 * survives reobfuscation (only the synthetic bridge becomes m_5478_).
 */
@Mixin(TremorsaurusRenderer.class)
public abstract class TremorsaurusRendererMixin {

    @Inject(method = "getTextureLocation", at = @At("HEAD"), cancellable = true, remap = false)
    private void primordialmobs$variantTexture(TremorsaurusEntity entity, CallbackInfoReturnable<ResourceLocation> cir) {
        if (CompatSkins.hasEasterEggName(entity, "princess")) {
            return;
        }
        if (CompatSkins.isRecolored(entity)) {
            cir.setReturnValue(CompatSkins.texture(entity, "tremorsaurus", ""));
        }
    }
}
