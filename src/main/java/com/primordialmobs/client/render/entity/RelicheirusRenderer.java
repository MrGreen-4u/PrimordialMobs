package com.primordialmobs.client.render.entity;

import com.primordialmobs.PrimordialMobs;
import com.primordialmobs.client.model.RelicheirusModel;
import com.primordialmobs.client.render.entity.layer.RelicheirusHeldFishLayer;
import com.primordialmobs.server.entity.living.RelicheirusEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class RelicheirusRenderer extends MobRenderer<RelicheirusEntity, RelicheirusModel> {

    public RelicheirusRenderer(EntityRendererProvider.Context renderManagerIn) {
        super(renderManagerIn, new RelicheirusModel(), 1.0F);
        this.addLayer(new RelicheirusHeldFishLayer(this));
    }

    protected void scale(RelicheirusEntity mob, PoseStack matrixStackIn, float partialTicks) {
    }

    public ResourceLocation getTextureLocation(RelicheirusEntity entity) {
        String name = "relicheirus" + DinosaurSkinTextures.skinSuffix(entity.getAltSkin());
        if (entity.isRecolored()) {
            name += "_variant";
        }
        return DinosaurSkinTextures.get(name);
    }
}
