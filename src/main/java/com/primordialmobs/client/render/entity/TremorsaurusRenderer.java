package com.primordialmobs.client.render.entity;

import com.primordialmobs.PrimordialMobs;
import com.primordialmobs.client.model.TremorsaurusModel;
import com.primordialmobs.client.render.entity.layer.TremorsaurusHeldMobLayer;
import com.primordialmobs.client.render.entity.layer.TremorsaurusRiderLayer;
import com.primordialmobs.server.entity.living.TremorsaurusEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class TremorsaurusRenderer extends MobRenderer<TremorsaurusEntity, TremorsaurusModel> {
    private static final ResourceLocation TEXTURE_PRINCESS = new ResourceLocation(PrimordialMobs.NAMESPACE, "textures/entity/tremorsaurus_princess.png");

    public TremorsaurusRenderer(EntityRendererProvider.Context renderManagerIn) {
        super(renderManagerIn, new TremorsaurusModel(), 1.1F);
        this.addLayer(new TremorsaurusRiderLayer(this));
        this.addLayer(new TremorsaurusHeldMobLayer(this));
    }

    protected void scale(TremorsaurusEntity mob, PoseStack matrixStackIn, float partialTicks) {
    }

    public ResourceLocation getTextureLocation(TremorsaurusEntity entity) {
        if (entity.hasCustomName() && "princess".equalsIgnoreCase(entity.getName().getString())) {
            return TEXTURE_PRINCESS;
        }
        String name = "tremorsaurus" + DinosaurSkinTextures.skinSuffix(entity.getAltSkin());
        if (entity.isRecolored()) {
            name += "_variant";
        }
        return DinosaurSkinTextures.get(name);
    }


}

