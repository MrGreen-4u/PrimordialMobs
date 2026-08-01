package com.primordialmobs.client.render.item;

import com.primordialmobs.PrimordialMobs;
import com.primordialmobs.client.model.PrimitiveClubModel;
import com.primordialmobs.server.item.PMItemRegistry;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class PMItemstackRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation PRIMITIVE_CLUB_TEXTURE = new ResourceLocation(PrimordialMobs.NAMESPACE, "textures/entity/primitive_club.png");
    private static final PrimitiveClubModel PRIMITIVE_CLUB_MODEL = new PrimitiveClubModel();
    private static final ResourceLocation EXTINCTION_SPEAR_TEXTURE = new ResourceLocation(PrimordialMobs.NAMESPACE, "textures/entity/extinction_spear.png");
    private static final com.primordialmobs.client.model.ExtinctionSpearModel EXTINCTION_SPEAR_MODEL = new com.primordialmobs.client.model.ExtinctionSpearModel();

    public PMItemstackRenderer() {
        super(null, null);
    }

    @Override
    public void renderByItem(ItemStack itemStackIn, ItemDisplayContext transformType, PoseStack poseStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        ClientLevel level = Minecraft.getInstance().level;
        boolean heldIn3d = transformType == ItemDisplayContext.THIRD_PERSON_LEFT_HAND || transformType == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND || transformType == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND || transformType == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;

        if (itemStackIn.is(PMItemRegistry.PRIMITIVE_CLUB.get())) {
            poseStack.translate(0.5F, 0.5F, 0.5F);
            ItemStack spriteItem = new ItemStack(PMItemRegistry.PRIMITIVE_CLUB_SPRITE.get());
            spriteItem.setTag(itemStackIn.getTag());
            if (heldIn3d) {
                poseStack.pushPose();
                poseStack.mulPose(Axis.XP.rotationDegrees(-180));
                poseStack.translate(0, -1.15F, -0.1F);
                if (transformType.firstPerson()) {
                    poseStack.translate(0, 0.1F, 0);
                    poseStack.scale(0.8F, 0.8F, 0.8F);
                }
                VertexConsumer vertexconsumer = ItemRenderer.getArmorFoilBuffer(bufferIn, RenderType.armorCutoutNoCull(PRIMITIVE_CLUB_TEXTURE), false, itemStackIn.hasFoil());
                PRIMITIVE_CLUB_MODEL.renderToBuffer(poseStack, vertexconsumer, combinedLightIn, combinedOverlayIn, 1.0F, 1.0F, 1.0F, 1.0F);
                poseStack.popPose();
            } else {
                renderStaticItemSprite(spriteItem, transformType, combinedLightIn, combinedOverlayIn, poseStack, bufferIn, level);
            }
        }

        if (itemStackIn.is(PMItemRegistry.EXTINCTION_SPEAR.get())) {
            poseStack.translate(0.5F, 0.5F, 0.5F);
            ItemStack spriteItem = new ItemStack(PMItemRegistry.EXTINCTION_SPEAR_SPRITE.get());
            spriteItem.setTag(itemStackIn.getTag());
            if (heldIn3d) {
                poseStack.pushPose();
                poseStack.mulPose(Axis.XP.rotationDegrees(-180));
                poseStack.translate(0, -0.85F, -0.1F);
                if (transformType.firstPerson()) {
                    poseStack.translate(0, 0.5F, 0F);
                    poseStack.scale(0.75F, 0.75F, 0.75F);
                }
                EXTINCTION_SPEAR_MODEL.resetToDefaultPose();
                VertexConsumer vertexconsumer1 = ItemRenderer.getArmorFoilBuffer(bufferIn, RenderType.entityCutoutNoCull(EXTINCTION_SPEAR_TEXTURE), false, itemStackIn.hasFoil());
                EXTINCTION_SPEAR_MODEL.renderToBuffer(poseStack, vertexconsumer1, 240, combinedOverlayIn, 1.0F, 1.0F, 1.0F, 1.0F);
                VertexConsumer vertexconsumer2 = ItemRenderer.getArmorFoilBuffer(bufferIn, net.minecraftforge.client.ForgeRenderTypes.getUnlitTranslucent(EXTINCTION_SPEAR_TEXTURE), false, itemStackIn.hasFoil());
                EXTINCTION_SPEAR_MODEL.renderToBuffer(poseStack, vertexconsumer2, 240, combinedOverlayIn, 1.0F, 1.0F, 1.0F, 1.0F);
                poseStack.popPose();
            } else {
                renderStaticItemSprite(spriteItem, transformType, combinedLightIn, combinedOverlayIn, poseStack, bufferIn, level);
            }
        }
    }

    private void renderStaticItemSprite(ItemStack spriteItem, ItemDisplayContext transformType, int combinedLightIn, int combinedOverlayIn, PoseStack poseStack, MultiBufferSource bufferIn, ClientLevel level) {
        Minecraft.getInstance().getItemRenderer().renderStatic(spriteItem, transformType, transformType == ItemDisplayContext.GROUND ? combinedLightIn : 240, combinedOverlayIn, poseStack, bufferIn, level, 0);
    }
}
