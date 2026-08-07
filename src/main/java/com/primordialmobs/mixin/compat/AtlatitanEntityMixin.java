package com.primordialmobs.mixin.compat;

import com.github.alexmodguy.alexscaves.server.entity.living.AtlatitanEntity;
import com.github.alexmodguy.alexscaves.server.entity.living.SauropodBaseEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fixes the ridden Rammer's broken-looking walk.
 *
 * Alex's Caves' {@code tickRidden} snaps the mount's movement yaw to the rider's view instantly,
 * but the BODY — and everything visual hangs off {@code yBodyRot}: legs, neck, tail — catches up
 * through {@code SauropodBaseEntity#tick}'s approach at {@code turningFast ? 10 : 2} degrees per
 * tick. {@code turningFast} is only ever engaged by the melee goals, never while ridden, so a
 * steered Rammer turns its body at 2°/tick (4+ seconds for a half turn) and visibly crabs
 * sideways with its legs animating against the real travel direction.
 *
 * The fix reuses Alex's Caves' own switch: while the rider steers, or while the body still lags
 * more than 20° behind the movement yaw, the body turns at the same fast rate the melee goals
 * use. {@code tickRidden} runs on both the server and the controlling client, and the body-yaw
 * approach in {@code tick} runs on both sides too, so the plain (unsynced) field stays
 * consistent. Nothing changes for an unridden Rammer: with no rider {@code tickRidden} never
 * runs, and the melee goals keep managing the field exactly as before.
 */
@Mixin(AtlatitanEntity.class)
public abstract class AtlatitanEntityMixin {

    @Inject(method = "tickRidden", at = @At("TAIL"))
    private void primordialmobs$riddenBodyTurn(Player player, Vec3 travelVector, CallbackInfo ci) {
        SauropodBaseEntity self = (SauropodBaseEntity) (Object) this;
        boolean steering = player.zza != 0.0F || player.xxa != 0.0F;
        self.turningFast = steering || Mth.degreesDifferenceAbs(self.yBodyRot, self.getYRot()) > 20.0F;
    }
}
