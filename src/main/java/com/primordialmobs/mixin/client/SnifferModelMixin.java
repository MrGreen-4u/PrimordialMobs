package com.primordialmobs.mixin.client;

import com.primordialmobs.server.entity.util.SnifferSkinHolder;
import com.primordialmobs.server.entity.util.SnifferTaming;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.SnifferModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Two client-side poses for the sniffer:
 *  - freezes the sitting sniffer's digging animation at {@link SnifferTaming#SIT_ANIM_FREEZE_MS} (2 s)
 *    so a seated pet holds the lie-down pose instead of playing the full 8-second SNIFFER_DIG, whose
 *    last ~6 seconds are the head-down ground-sniffing loop (a wild sniffer digging naturally still
 *    plays the whole animation);
 *  - the Seething Stew's angry posture: an enraged sniffer throws its snout in the air (~32 degrees),
 *    eased in and out over 5 ticks by SnifferMixin. Vanilla maps positive head xRot to looking DOWN
 *    ({@code this.head.xRot = headPitch * PI/180} with headPitch > 0 below the horizon), so raising is
 *    a subtraction.
 */
@Mixin(SnifferModel.class)
public abstract class SnifferModelMixin {

    @Shadow
    @Final
    private ModelPart head;

    @Unique
    private final Vector3f ac_animCache = new Vector3f();

    /** How far the enraged snout rises: ~32 degrees at full anger. */
    @Unique
    private static final float AC_ANGRY_HEAD_RAISE = 0.55F;

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/animal/sniffer/Sniffer;FFFFF)V", at = @At("TAIL"))
    private void ac_angryHeadRaise(Sniffer entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                                   float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (entity instanceof SnifferSkinHolder holder) {
            float partialTick = ageInTicks - entity.tickCount;
            float angry = holder.ac_getAngryHeadAmount(partialTick);
            if (angry > 0.0F) {
                this.head.xRot -= angry * AC_ANGRY_HEAD_RAISE;
            }
        }
    }

    @Redirect(
            method = "setupAnim(Lnet/minecraft/world/entity/animal/sniffer/Sniffer;FFFFF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/SnifferModel;animate(Lnet/minecraft/world/entity/AnimationState;Lnet/minecraft/client/animation/AnimationDefinition;F)V",
                    ordinal = 0
            )
    )
    private void ac_freezeSitDiggingAnimation(SnifferModel<?> model, AnimationState state, AnimationDefinition def, float ageInTicks,
                                              Sniffer entity, float limbSwing, float limbSwingAmount, float age2, float netHeadYaw, float headPitch) {
        // Replicates HierarchicalModel#animate, but clamps the accumulated time for a commanded-sit sniffer.
        final boolean sit = entity instanceof SnifferSkinHolder holder && holder.ac_isOrderedToSit();
        state.updateTime(ageInTicks, 1.0F);
        state.ifStarted(s -> {
            long time = s.getAccumulatedTime();
            if (sit) {
                time = Math.min(time, SnifferTaming.SIT_ANIM_FREEZE_MS);
            }
            KeyframeAnimations.animate(model, def, time, 1.0F, this.ac_animCache);
        });
    }
}
