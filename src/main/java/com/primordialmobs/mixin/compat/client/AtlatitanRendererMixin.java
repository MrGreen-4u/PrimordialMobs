package com.primordialmobs.mixin.compat.client;

import com.github.alexmodguy.alexscaves.client.render.entity.AtlatitanRenderer;
import com.github.alexmodguy.alexscaves.server.entity.living.AtlatitanEntity;
import com.primordialmobs.compat.client.CompatSkins;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Client only: hands back our recoloured-variant texture for a Rammer (Atlatitan) that carries the flag.
 *
 * Same pattern as the other five: injected into Alex's Caves' own {@code getTextureLocation} (whose
 * name survives reobfuscation, hence {@code remap = false}) rather than registering a replacement
 * renderer, which would race Alex's Caves' parallel-dispatched registration. Alex's Caves' method
 * already resolves the retro/tectonic base skins; this only swaps in the {@code _variant} recolour
 * of whichever skin is active.
 */
@Mixin(AtlatitanRenderer.class)
public abstract class AtlatitanRendererMixin {

    @Inject(method = "getTextureLocation", at = @At("HEAD"), cancellable = true, remap = false)
    private void primordialmobs$variantTexture(AtlatitanEntity entity, CallbackInfoReturnable<ResourceLocation> cir) {
        if (CompatSkins.isRecolored(entity)) {
            cir.setReturnValue(CompatSkins.texture(entity, "atlatitan", ""));
        }
    }
}
