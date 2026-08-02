package com.primordialmobs.client.render.entity;

import com.primordialmobs.PrimordialMobs;
import com.primordialmobs.client.model.PMBoatChestModel;
import com.primordialmobs.client.model.PMBoatModel;
import com.primordialmobs.client.model.PewenBoatModel;
import com.primordialmobs.server.entity.util.PMBoat;
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
import net.minecraft.world.entity.vehicle.Boat;
import org.joml.Quaternionf;

import java.util.HashMap;

public class PMBoatRenderer<T extends Boat & PMBoat> extends EntityRenderer<T> {

    private final HashMap<PMBoat.Type, ResourceLocation> textureMap = new HashMap<>();
    private final HashMap<PMBoat.Type, PMBoatModel> modelMap = new HashMap<>();

    private static final ResourceLocation CHEST_TEXTURE = new ResourceLocation(PrimordialMobs.NAMESPACE, "textures/entity/boat/chest.png");
    private static final PMBoatChestModel CHEST_MODEL = new PMBoatChestModel();

    private final boolean isChest;

    public PMBoatRenderer(EntityRendererProvider.Context context, boolean isChest) {
        super(context);
        for (PMBoat.Type type : PMBoat.Type.values()) {
            textureMap.put(type, new ResourceLocation(PrimordialMobs.NAMESPACE, "textures/entity/boat/" + type.getName() + "_boat.png"));
        }
        modelMap.put(PMBoat.Type.PEWEN, new PewenBoatModel());
        this.isChest = isChest;
    }

    @Override
    public void render(T entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferIn, int packedLightIn) {
        PMBoatModel model = modelMap.get(entity.getACBoatType());
        poseStack.pushPose();
        poseStack.translate(0.0F, 1.5F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw));
        float f = (float) entity.getHurtTime() - partialTicks;
        float f1 = entity.getDamage() - partialTicks;
        if (f1 < 0.0F) {
            f1 = 0.0F;
        }

        if (f > 0.0F) {
            poseStack.mulPose(Axis.XP.rotationDegrees(Mth.sin(f) * f * f1 / 10.0F * (float) entity.getHurtDir()));
        }

        float f2 = entity.getBubbleAngle(partialTicks);
        if (!Mth.equal(f2, 0.0F)) {
            poseStack.mulPose((new Quaternionf()).setAngleAxis(entity.getBubbleAngle(partialTicks) * ((float) Math.PI / 180F), 1.0F, 0.0F, 1.0F));
        }

        poseStack.scale(-1.0F, -1.0F, 1.0F);
        if (isChest) {
            poseStack.pushPose();
            poseStack.translate(0.0F, -0.25F, 0.5F);
            CHEST_MODEL.renderToBuffer(poseStack, bufferIn.getBuffer(RenderType.entityCutoutNoCull(CHEST_TEXTURE)), packedLightIn, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
            poseStack.popPose();
        }
        model.setupAnim(entity, partialTicks, 0.0F, -0.1F, 0.0F, 0.0F);
        VertexConsumer vertexconsumer = bufferIn.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(entity)));
        model.renderToBuffer(poseStack, vertexconsumer, packedLightIn, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        if (!entity.isUnderWater()) {
            VertexConsumer vertexconsumer1 = bufferIn.getBuffer(RenderType.waterMask());
            model.getWaterMask().render(poseStack, vertexconsumer1, packedLightIn, OverlayTexture.NO_OVERLAY);
        }
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, bufferIn, packedLightIn);
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return textureMap.get(entity.getACBoatType());
    }
}
