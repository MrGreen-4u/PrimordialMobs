package com.primordialmobs.client.render.entity;

import com.primordialmobs.PrimordialMobs;
import com.primordialmobs.client.model.TrilocarisModel;
import com.primordialmobs.server.entity.living.TrilocarisEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class TrilocarisRenderer extends MobRenderer<TrilocarisEntity, TrilocarisModel> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(PrimordialMobs.NAMESPACE, "textures/entity/trilocaris.png");

    public TrilocarisRenderer(EntityRendererProvider.Context renderManagerIn) {
        super(renderManagerIn, new TrilocarisModel(), 0.3F);
    }

    protected float getFlipDegrees(TrilocarisEntity centipede) {
        return 180.0F;
    }


    protected void scale(TrilocarisEntity mob, PoseStack matrixStackIn, float partialTicks) {
    }

    public ResourceLocation getTextureLocation(TrilocarisEntity entity) {
        return TEXTURE;
    }
}

