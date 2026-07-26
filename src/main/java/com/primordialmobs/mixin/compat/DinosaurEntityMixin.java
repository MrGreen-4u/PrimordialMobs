package com.primordialmobs.mixin.compat;

import com.github.alexmodguy.alexscaves.server.entity.living.DinosaurEntity;
import com.primordialmobs.compat.PMRecolorable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Compat only: what Primordial Mobs changes in the shared dinosaur base class.
 *
 * 1. The recoloured-variant flag, as an independent synched value (see {@link PMRecolorable}). Kept out of
 *    Alex's Caves' AltSkin so the Amber Curiosity and Tectonic Shard can no longer overwrite or erase it.
 * 2. A seated dinosaur must never drift. {@code travel()} only cancels AI movement INPUT, so a parked mob
 *    still slides on residual momentum or when shoved by another mob.
 * 3. The collision box has to follow the lowered body, otherwise the hitbox floats at full standing height
 *    above a crouched dinosaur. The per-species scale lives in the species mixins' {@code getDimensions}.
 */
@Mixin(DinosaurEntity.class)
public abstract class DinosaurEntityMixin implements PMRecolorable {

    @Unique
    private static final EntityDataAccessor<Boolean> PRIMORDIALMOBS$RECOLORED =
            SynchedEntityData.defineId(DinosaurEntity.class, EntityDataSerializers.BOOLEAN);

    /** NBT key. Distinct from standalone's "Recolored" so a world can move between the two modes safely. */
    @Unique
    private static final String PRIMORDIALMOBS$RECOLORED_KEY = "PMRecolored";

    @Unique
    private boolean primordialmobs$sittingDimensions;

    @Override
    public boolean pm_isRecolored() {
        return ((DinosaurEntity) (Object) this).getEntityData().get(PRIMORDIALMOBS$RECOLORED);
    }

    @Override
    public void pm_setRecolored(boolean recolored) {
        ((DinosaurEntity) (Object) this).getEntityData().set(PRIMORDIALMOBS$RECOLORED, recolored);
    }

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void primordialmobs$defineRecolored(CallbackInfo ci) {
        ((DinosaurEntity) (Object) this).getEntityData().define(PRIMORDIALMOBS$RECOLORED, false);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void primordialmobs$saveRecolored(CompoundTag compound, CallbackInfo ci) {
        compound.putBoolean(PRIMORDIALMOBS$RECOLORED_KEY, this.pm_isRecolored());
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void primordialmobs$loadRecolored(CompoundTag compound, CallbackInfo ci) {
        DinosaurEntity self = (DinosaurEntity) (Object) this;
        this.pm_setRecolored(compound.getBoolean(PRIMORDIALMOBS$RECOLORED_KEY));
        // Migrate worlds written by the first compat build, which carried the variant as AltSkin 3. Alex's
        // Caves never assigns 3 itself, so this is unambiguous. Freeing the slot is what lets the Amber
        // Curiosity and Tectonic Shard work on a recoloured dinosaur without destroying its variant.
        if (self.getAltSkin() == 3) {
            self.setAltSkin(0);
            this.pm_setRecolored(true);
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void primordialmobs$pinWhileSitting(CallbackInfo ci) {
        DinosaurEntity self = (DinosaurEntity) (Object) this;
        if (self.isInSittingPose()) {
            self.setDeltaMovement(self.getDeltaMovement().multiply(0.0D, 1.0D, 0.0D));
            if (!self.level().isClientSide && !self.getNavigation().isDone()) {
                self.getNavigation().stop();
            }
        }
        if (this.primordialmobs$sittingDimensions != self.isInSittingPose()) {
            this.primordialmobs$sittingDimensions = self.isInSittingPose();
            self.refreshDimensions();
        }
    }
}
