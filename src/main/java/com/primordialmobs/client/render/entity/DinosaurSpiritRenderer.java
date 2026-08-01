package com.primordialmobs.client.render.entity;

import com.primordialmobs.PrimordialMobs;
import com.primordialmobs.client.model.GrottoceratopsModel;
import com.primordialmobs.client.model.SubterranodonModel;
import com.primordialmobs.client.model.TremorsaurusModel;
import com.primordialmobs.server.entity.item.DinosaurSpiritEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * The Extinction Spear's ghost dinosaurs. Upstream draws these with a custom "red ghost" core shader;
 * this port approximates it with the vanilla translucent-emissive render type tinted red, which reads
 * the same in-game (glowing red translucent silhouettes) without shipping shader files.
 */
public class DinosaurSpiritRenderer extends EntityRenderer<DinosaurSpiritEntity> {

    private static final ResourceLocation SUBTERRANODON_TEXTURE = new ResourceLocation(PrimordialMobs.NAMESPACE, "textures/entity/subterranodon.png");
    private static final ResourceLocation TREMORSAURUS_TEXTURE = new ResourceLocation(PrimordialMobs.NAMESPACE, "textures/entity/tremorsaurus.png");
    private static final ResourceLocation GROTTOCERATOPS_TEXTURE = new ResourceLocation(PrimordialMobs.NAMESPACE, "textures/entity/grottoceratops.png");
    private static final SubterranodonModel SUBTERRANODON_MODEL = new SubterranodonModel();
    private static final TremorsaurusModel TREMORSAURUS_MODEL = new TremorsaurusModel();
    private static final GrottoceratopsModel GROTTOCERATOPS_MODEL = new GrottoceratopsModel();
    private static final float GHOST_RED = 1.0F;
    private static final float GHOST_GREEN = 0.3F;
    private static final float GHOST_BLUE = 0.25F;

    public DinosaurSpiritRenderer(EntityRendererProvider.Context renderManagerIn) {
        super(renderManagerIn);
    }

    private static RenderType ghostRenderType(ResourceLocation texture) {
        return RenderType.entityTranslucentEmissive(texture);
    }

    public void render(DinosaurSpiritEntity entityIn, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferIn, int packedLightIn) {
        poseStack.pushPose();
        poseStack.translate(0.0D, 1.5D, 0.0D);
        if (entityIn.getDinosaurType() == DinosaurSpiritEntity.DinosaurType.GROTTOCERATOPS) {
            Player player = entityIn.getUsingPlayer();
            if (player != null) {
                Vec3 playerPos = player.getPosition(partialTicks);
                Vec3 dinoPos = entityIn.getPosition(partialTicks);
                double d1 = playerPos.z - dinoPos.z;
                double d2 = playerPos.x - dinoPos.x;
                float f = (-((float) Mth.atan2(d2, d1)) * (180F / (float) Math.PI));
                poseStack.mulPose(Axis.YP.rotationDegrees(-f));
            }
        } else {
            poseStack.mulPose(Axis.YP.rotationDegrees(180 - entityIn.getViewYRot(partialTicks)));
        }
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.mulPose(Axis.XN.rotationDegrees(entityIn.getViewXRot(partialTicks)));
        VertexConsumer ivertexbuilder;
        float ghostAlpha = entityIn.getFadeIn(partialTicks);
        boolean prevBaby;
        switch (entityIn.getDinosaurType()) {
            case SUBTERRANODON:
                prevBaby = SUBTERRANODON_MODEL.young;
                SUBTERRANODON_MODEL.young = false;
                ivertexbuilder = bufferIn.getBuffer(ghostRenderType(SUBTERRANODON_TEXTURE));
                SUBTERRANODON_MODEL.animateSpirit(entityIn, partialTicks);
                SUBTERRANODON_MODEL.renderToBuffer(poseStack, ivertexbuilder, 240, OverlayTexture.NO_OVERLAY, GHOST_RED, GHOST_GREEN, GHOST_BLUE, ghostAlpha);
                SUBTERRANODON_MODEL.young = prevBaby;
                break;
            case GROTTOCERATOPS:
                prevBaby = GROTTOCERATOPS_MODEL.young;
                GROTTOCERATOPS_MODEL.young = false;
                ivertexbuilder = bufferIn.getBuffer(ghostRenderType(GROTTOCERATOPS_TEXTURE));
                GROTTOCERATOPS_MODEL.animateSpirit(entityIn, partialTicks);
                GROTTOCERATOPS_MODEL.renderSpiritToBuffer(poseStack, ivertexbuilder, 240, OverlayTexture.NO_OVERLAY, GHOST_RED, GHOST_GREEN, GHOST_BLUE, ghostAlpha);
                GROTTOCERATOPS_MODEL.young = prevBaby;
                break;
            case TREMORSAURUS:
                prevBaby = TREMORSAURUS_MODEL.young;
                TREMORSAURUS_MODEL.young = false;
                ivertexbuilder = bufferIn.getBuffer(ghostRenderType(TREMORSAURUS_TEXTURE));
                TREMORSAURUS_MODEL.animateSpirit(entityIn, partialTicks);
                TREMORSAURUS_MODEL.renderSpiritToBuffer(poseStack, ivertexbuilder, 240, OverlayTexture.NO_OVERLAY, GHOST_RED, GHOST_GREEN, GHOST_BLUE, ghostAlpha);
                TREMORSAURUS_MODEL.young = prevBaby;
                break;
        }
        poseStack.popPose();
        super.render(entityIn, entityYaw, partialTicks, poseStack, bufferIn, packedLightIn);
    }

    public ResourceLocation getTextureLocation(DinosaurSpiritEntity entity) {
        return SUBTERRANODON_TEXTURE;
    }
}
