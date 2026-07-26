package com.primordialmobs.mixin.compat;

import com.github.alexmodguy.alexscaves.server.entity.ai.RelicheirusMeleeGoal;
import com.github.alexmodguy.alexscaves.server.entity.living.RelicheirusEntity;
import com.primordialmobs.compat.CompatDinosaurs;
import com.github.alexthe666.citadel.animation.IAnimatedEntity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Compat only: lets the Logger use its swallow-whole animation on any fish, not just the Trilocaris.
 *
 * Alex's Caves picks the animation with a literal {@code target instanceof TrilocarisEntity} and falls back
 * to the slash attack for everything else. Rather than rewrite that branch (an {@code instanceof} is not
 * redirectable with plain Mixin, and MixinExtras would tie us to a Forge version), this runs FIRST and, for
 * an extra fish already in reach, sets the peck height and the eating animation itself. Alex's Caves' own
 * branch is then a no-op because it is guarded by {@code getAnimation() == NO_ANIMATION}, which we have just
 * made false. Purely additive: for a Trilocaris, or for anything not in the tag, this does nothing at all.
 */
@Mixin(RelicheirusMeleeGoal.class)
public abstract class RelicheirusMeleeGoalMixin {

    @Shadow(remap = false)
    private RelicheirusEntity relicheirus;

    @Inject(method = "tick", at = @At("HEAD"))
    private void primordialmobs$swallowAnyFish(CallbackInfo ci) {
        LivingEntity target = this.relicheirus.getTarget();
        if (target == null || !CompatDinosaurs.isExtraFish(target)) {
            return;
        }
        if (this.relicheirus.getAnimation() != IAnimatedEntity.NO_ANIMATION) {
            return;
        }
        // Same reach test Alex's Caves applies before choosing an attack animation.
        double dist = this.relicheirus.distanceTo(target);
        if (dist < this.relicheirus.getBbWidth() + target.getBbWidth() + 1.0D) {
            this.relicheirus.setPeckY(target.getBlockY());
            this.relicheirus.setAnimation(RelicheirusEntity.ANIMATION_EAT_TRILOCARIS);
        }
    }
}
