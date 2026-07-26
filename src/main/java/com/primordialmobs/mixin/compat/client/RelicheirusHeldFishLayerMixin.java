package com.primordialmobs.mixin.compat.client;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.model.RelicheirusModel;
import com.github.alexmodguy.alexscaves.client.render.entity.layer.RelicheirusHeldTrilocarisLayer;
import com.github.alexmodguy.alexscaves.server.entity.living.RelicheirusEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.primordialmobs.compat.CompatDinosaurs;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Compat only, client only: draws the fish in the Logger's beak for anything it fishes, not just the
 * Trilocaris.
 *
 * THE BUG THIS FIXES (reported from play): while the Logger was eating a cod you saw the fish rise TWICE —
 * once in its mouth and once, in parallel, somewhere else. Alex's Caves' layer is gated on
 * {@code heldMob instanceof TrilocarisEntity}, so for any other fish it neither drew it at the mouth nor —
 * crucially — called {@code blockRenderingEntity}, which is what suppresses the entity's own world render.
 * The prey therefore stayed visible at its real position while the eating animation played, and our
 * {@code setPos} in RelicheirusEntityMixin was walking it up a slightly different path. Standalone never had
 * this because our own layer accepts any mob and does the blocking.
 *
 * Cancels Alex's Caves' method and replays it verbatim for our extra fish; a Trilocaris still falls through
 * to Alex's Caves' own branch, so nothing is drawn or blocked twice.
 */
@Mixin(RelicheirusHeldTrilocarisLayer.class)
public abstract class RelicheirusHeldFishLayerMixin extends RenderLayer<RelicheirusEntity, RelicheirusModel> {

    /** Discarded by Mixin; it exists only so this class can extend RenderLayer to reach getParentModel(). */
    public RelicheirusHeldFishLayerMixin(RenderLayerParent<RelicheirusEntity, RelicheirusModel> parent) {
        super(parent);
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true, remap = false)
    private void primordialmobs$renderAnyFish(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                                              RelicheirusEntity relicheirus, float limbSwing, float limbSwingAmount,
                                              float partialTicks, float ageInTicks, float netHeadYaw, float headPitch,
                                              CallbackInfo ci) {
        Entity heldMob = relicheirus.getHeldMob();
        if (heldMob == null || !CompatDinosaurs.isExtraFish(heldMob)
                || relicheirus.getAnimation() != RelicheirusEntity.ANIMATION_EAT_TRILOCARIS
                || relicheirus.getAnimationTick() <= 15) {
            return;
        }
        ci.cancel();
        // Same transform Alex's Caves uses for the Trilocaris, so the fish sits in the beak identically.
        float riderRot = heldMob.yRotO + (heldMob.getYRot() - heldMob.yRotO) * partialTicks;
        AlexsCaves.PROXY.releaseRenderingEntity(heldMob.getUUID());
        poseStack.pushPose();
        this.getParentModel().translateToMouth(poseStack);
        poseStack.translate(0.0F, -1.34F, -1.0F);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(riderRot + 180.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        poseStack.translate(0.0F, -heldMob.getBbHeight() * 0.5F, 0.0F);
        ((RelicheirusHeldTrilocarisLayer) (Object) this).renderEntity(heldMob, 0.0D, 0.0D, 0.0D, 0.0F,
                partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
        // Suppress the prey's own world render for this frame -- without this you see the fish twice.
        AlexsCaves.PROXY.blockRenderingEntity(heldMob.getUUID());
    }
}
