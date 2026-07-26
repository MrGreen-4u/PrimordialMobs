package com.primordialmobs.client.render.entity;

import com.primordialmobs.PrimordialMobs;
import com.primordialmobs.client.model.GrottoceratopsModel;
import com.primordialmobs.server.entity.living.GrottoceratopsEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class GrottoceratopsRenderer extends MobRenderer<GrottoceratopsEntity, GrottoceratopsModel> {

    public GrottoceratopsRenderer(EntityRendererProvider.Context renderManagerIn) {
        super(renderManagerIn, new GrottoceratopsModel(), 1.1F);
    }

    protected void scale(GrottoceratopsEntity mob, PoseStack matrixStackIn, float partialTicks) {
    }

    public ResourceLocation getTextureLocation(GrottoceratopsEntity entity) {
        String name = "grottoceratops" + DinosaurSkinTextures.skinSuffix(entity.getAltSkin());
        if (entity.isBaby()) {
            name += "_baby";
        }
        if (entity.isRecolored()) {
            name += "_variant";
        }
        return DinosaurSkinTextures.get(name);
    }
}

