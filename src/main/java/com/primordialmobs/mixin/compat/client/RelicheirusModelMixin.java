package com.primordialmobs.mixin.compat.client;

import com.github.alexmodguy.alexscaves.client.model.RelicheirusModel;
import com.github.alexmodguy.alexscaves.server.entity.living.RelicheirusEntity;
import com.github.alexthe666.citadel.client.model.AdvancedEntityModel;
import com.github.alexthe666.citadel.client.model.AdvancedModelBox;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Compat only, client only: the Logger's sit pose and its walk cadence.
 *
 * Two things Primordial Mobs changes in this model and Alex's Caves does not have:
 *
 * 1. A rest pose. Alex's Caves' Relicheirus can never be told to sit, so a parked Logger just stood there.
 *    Ported verbatim from our standalone RelicheirusModel, forward-kinematics reasoning included.
 * 2. The walk cadence, which is the visible "chest does not move properly while I ride it" bug. Standalone
 *    multiplies the base 0.8 by 0.72 normally and by 0.38 while ridden; Alex's Caves has no ridden case at
 *    all. This matters beyond looks: RelicheirusEntity#positionRider bobs the rider with
 *    PMMath.walkValue(..., 0.304F, ...) = 0.8 * 0.38, so without this the rider was bobbing to a cadence
 *    the body never used and the two visibly drifted apart.
 *
 * The cadence is patched with {@code @ModifyConstant} on the FIRST 0.8F of the method ({@code walkSpeed =
 * 0.8F}); the other three are {@code walkDegree * 0.8F} factors, hence the ordinal. Constant-targeting is
 * mapping-agnostic, and with {@code defaultRequire = 1} an Alex's Caves update that reorders them fails
 * loudly at boot instead of silently doing nothing.
 */
@Mixin(RelicheirusModel.class)
public abstract class RelicheirusModelMixin extends AdvancedEntityModel<RelicheirusEntity> {

    @Shadow(remap = false) @Final private AdvancedModelBox body;
    @Shadow(remap = false) @Final private AdvancedModelBox chest;
    @Shadow(remap = false) @Final private AdvancedModelBox rleg;
    @Shadow(remap = false) @Final private AdvancedModelBox rleg2;
    @Shadow(remap = false) @Final private AdvancedModelBox rfoot;
    @Shadow(remap = false) @Final private AdvancedModelBox lleg;
    @Shadow(remap = false) @Final private AdvancedModelBox lleg2;
    @Shadow(remap = false) @Final private AdvancedModelBox lfoot;
    @Shadow(remap = false) @Final private AdvancedModelBox rarm;
    @Shadow(remap = false) @Final private AdvancedModelBox larm;
    @Shadow(remap = false) @Final private AdvancedModelBox rhand;
    @Shadow(remap = false) @Final private AdvancedModelBox lhand;
    @Shadow(remap = false) @Final private AdvancedModelBox tail;

    /** Set by {@link #primordialmobs$riddenCadence} so the constant hook knows which entity is being drawn. */
    @org.spongepowered.asm.mixin.Unique
    private boolean primordialmobs$ridden;

    @Inject(method = "setupAnim", at = @At("HEAD"), remap = false)
    private void primordialmobs$captureRidden(RelicheirusEntity entity, float limbSwing, float limbSwingAmount,
                                              float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        this.primordialmobs$ridden = entity.isVehicle();
    }

    @ModifyConstant(method = "setupAnim", constant = @Constant(floatValue = 0.8F, ordinal = 0), remap = false)
    private float primordialmobs$riddenCadence(float original) {
        // ~0.72x normally, ~0.38x while ridden: a calm gait instead of a frantic one, and in sync with the
        // rider's vertical bob (0.8 * 0.38 = 0.304, the factor RelicheirusEntity#positionRider uses).
        return original * (this.primordialmobs$ridden ? 0.38F : 0.72F);
    }

    @Inject(method = "setupAnim", at = @At("TAIL"), remap = false)
    private void primordialmobs$sitPose(RelicheirusEntity entity, float limbSwing, float limbSwingAmount,
                                        float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        float partialTick = ageInTicks - entity.tickCount;
        float sitAmount = entity.getSitProgress(partialTick);
        if (sitAmount <= 0.0F) {
            return;
        }
        // Resting theropod sit, forward-kinematics solved so the soles land FLAT (the three X rotations of
        // the leg sum to 0 => no foot tilt) and the body drop matches this mob's shorter legs, so the feet
        // are planted rather than buried.
        progressPositionPrev(body, sitAmount, 0, 12, -1, 1F);
        progressPositionPrev(rleg, sitAmount, 0, -6, 8, 1F);
        progressPositionPrev(lleg, sitAmount, 0, -6, 8, 1F);
        progressRotationPrev(rleg, sitAmount, (float) Math.toRadians(-20), (float) Math.toRadians(15), 0, 1F);
        progressRotationPrev(lleg, sitAmount, (float) Math.toRadians(-20), (float) Math.toRadians(-15), 0, 1F);
        progressRotationPrev(rleg2, sitAmount, (float) Math.toRadians(-50), 0, 0, 1F);
        progressRotationPrev(lleg2, sitAmount, (float) Math.toRadians(-50), 0, 0, 1F);
        progressRotationPrev(rfoot, sitAmount, (float) Math.toRadians(70), 0, 0, 1F);
        progressRotationPrev(lfoot, sitAmount, (float) Math.toRadians(70), 0, 0, 1F);
        // Arms rest forward with the hand HANGING straight down (wrist reads "|", not a flat "_"): FK-solved
        // (arm -105, hand +9) so the hand's finger axis maps to world-down and the whole arm's lowest point
        // sits at ~0 -- nothing buried. The arm is only two rigid bones and is longer than the shoulder
        // height, so the forearm lays forward as the ramp the hand hangs from.
        progressRotationPrev(rarm, sitAmount, (float) Math.toRadians(-105), 0, 0, 1F);
        progressRotationPrev(larm, sitAmount, (float) Math.toRadians(-105), 0, 0, 1F);
        progressRotationPrev(rhand, sitAmount, (float) Math.toRadians(9), 0, 0, 1F);
        progressRotationPrev(lhand, sitAmount, (float) Math.toRadians(9), 0, 0, 1F);
        // tail rests back, chest leans back a touch
        progressRotationPrev(tail, sitAmount, (float) Math.toRadians(-8), 0, 0, 1F);
        progressRotationPrev(chest, sitAmount, (float) Math.toRadians(6), 0, 0, 1F);
    }
}
