package com.primordialmobs.mixin.compat.client;

import com.github.alexmodguy.alexscaves.client.model.GrottoceratopsModel;
import com.github.alexmodguy.alexscaves.server.entity.living.GrottoceratopsEntity;
import com.github.alexthe666.citadel.client.model.AdvancedEntityModel;
import com.github.alexthe666.citadel.client.model.AdvancedModelBox;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Compat only, client only: the Rammer's sit pose.
 *
 * Alex's Caves' Grottoceratops can never be told to sit (its canOwnerCommand returns false), so its model
 * has no rest pose at all — with Primordial Mobs' taming grafted on, a parked Rammer just stood there. This
 * ports the pose from our standalone GrottoceratopsModel verbatim, including the forward-kinematics
 * reasoning that produced the angles.
 *
 * Injected at TAIL rather than at the pose's original mid-method position: every call after that point
 * ({@code walk}, {@code flap}, the bury-eggs swings) ADDS to rotateAngle/rotationPoint, and
 * {@code progressRotationPrev}/{@code progressPositionPrev} add too, so the accumulated result is the same
 * and there is no fragile injection point to break on an Alex's Caves update.
 */
@Mixin(GrottoceratopsModel.class)
public abstract class GrottoceratopsModelMixin extends AdvancedEntityModel<GrottoceratopsEntity> {

    @Shadow(remap = false) @Final private AdvancedModelBox body;
    @Shadow(remap = false) @Final private AdvancedModelBox rleg;
    @Shadow(remap = false) @Final private AdvancedModelBox rleg2;
    @Shadow(remap = false) @Final private AdvancedModelBox rfoot;
    @Shadow(remap = false) @Final private AdvancedModelBox lleg;
    @Shadow(remap = false) @Final private AdvancedModelBox lleg2;
    @Shadow(remap = false) @Final private AdvancedModelBox lfoot;
    @Shadow(remap = false) @Final private AdvancedModelBox rarm;
    @Shadow(remap = false) @Final private AdvancedModelBox larm;
    @Shadow(remap = false) @Final private AdvancedModelBox tail;
    @Shadow(remap = false) @Final private AdvancedModelBox neck;
    @Shadow(remap = false) @Final private AdvancedModelBox head;

    @Inject(method = "setupAnim", at = @At("TAIL"), remap = false)
    private void primordialmobs$sitPose(GrottoceratopsEntity entity, float limbSwing, float limbSwingAmount,
                                        float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        float partialTick = ageInTicks - entity.tickCount;
        float sitAmount = entity.getSitProgress(partialTick);
        if (sitAmount <= 0.0F) {
            return;
        }
        // Couched rest with a LEVEL back, solved with forward kinematics:
        //  - the body stays HORIZONTAL (no tilt) with the belly resting on the ground;
        //  - the FRONT legs are a single rigid piece (no wrist). They LEAN FORWARD ("\", rotX -30) and the
        //    SHOULDER ANCHOR is RAISED into the chest (posY -10.25) so the leaning leg reaches the ground
        //    and the hoof rests on it, carrying the front hoof spur OUT in front of the chest instead of
        //    embedding it. A rigid forward-leaning leg must tilt the sole heel-down / spur-up;
        //  - the BACK legs are knee-flexed (">"), soles kept FLAT (level body => flat <=> sum of X = 0).
        progressPositionPrev(body, sitAmount, 0, 11, 0, 1F);
        progressPositionPrev(rarm, sitAmount, 0, -10.25F, 2, 1F);
        progressPositionPrev(larm, sitAmount, 0, -10.25F, 2, 1F);
        progressRotationPrev(rarm, sitAmount, (float) Math.toRadians(-30), 0, 0, 1F);
        progressRotationPrev(larm, sitAmount, (float) Math.toRadians(-30), 0, 0, 1F);
        // MODEL ASYMMETRY: rfoot is parented to the shin (rleg2) but lfoot is parented to the THIGH (lleg),
        // so each side is solved independently for a flat, grounded sole:
        //   right: thigh(-44) + shin(46) + foot(-2) = 0   (real knee)
        //   left : thigh(-18) + foot(18)            = 0   (lleg2 +20 is a cosmetic shin)
        progressPositionPrev(rleg, sitAmount, 0, -10, 0, 1F);
        progressPositionPrev(lleg, sitAmount, 0, -11, 0, 1F);
        progressRotationPrev(rleg, sitAmount, (float) Math.toRadians(-44), 0, 0, 1F);
        progressRotationPrev(lleg, sitAmount, (float) Math.toRadians(-18), 0, 0, 1F);
        progressRotationPrev(rleg2, sitAmount, (float) Math.toRadians(46), 0, 0, 1F);
        progressRotationPrev(lleg2, sitAmount, (float) Math.toRadians(20), 0, 0, 1F);
        progressRotationPrev(rfoot, sitAmount, (float) Math.toRadians(-2), 0, 0, 1F);
        progressRotationPrev(lfoot, sitAmount, (float) Math.toRadians(18), 0, 0, 1F);
        // settle the head and let the tail rest
        progressRotationPrev(neck, sitAmount, (float) Math.toRadians(10), 0, 0, 1F);
        progressRotationPrev(head, sitAmount, (float) Math.toRadians(5), 0, 0, 1F);
        progressRotationPrev(tail, sitAmount, (float) Math.toRadians(6), 0, 0, 1F);
    }
}
