package com.primordialmobs.mixin.compat;

import com.github.alexmodguy.alexscaves.server.entity.living.DinosaurEntity;
import com.github.alexmodguy.alexscaves.server.entity.living.RelicheirusEntity;
import com.primordialmobs.compat.CompatDinosaurs;
import com.primordialmobs.server.misc.PMMath;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Compat only: turns Alex's Caves' Relicheirus into the Logger.
 *
 * Two things Primordial Mobs adds on top of Alex's Caves' (identical) code: it fishes anything in
 * {@code #alexscaves:relicheirus_fishes} rather than only the Trilocaris, and it can be tamed, commanded and
 * ridden. Tree-felling, pewen-nibbling, the Primordial Soup fervor and the loot are Alex's Caves' own and
 * already correct.
 *
 * See {@link GrottoceratopsEntityMixin} for why the riding methods are declared rather than injected.
 */
@Mixin(RelicheirusEntity.class)
public abstract class RelicheirusEntityMixin extends DinosaurEntity {

    /**
     * Saddle height on the BACK (lomo), over the shoulders — never up on the neck/head. The bounding box
     * height (5.9) is dominated by the long upright neck, so getPassengersRidingOffset() (~4.4) would seat
     * the rider on the head; this fixed value sits on the upper back. Measured by forward kinematics against
     * RelicheirusModel for the standalone entity; unchanged here.
     */
    private static final float PRIMORDIALMOBS$RIDER_BACK_HEIGHT = 3.1F;

    protected RelicheirusEntityMixin(EntityType<?> type, Level level) {
        super(type, level);
    }

    /**
     * Alex's Caves' tick() only carries a mob in its beak while {@code target instanceof TrilocarisEntity};
     * anything else it grabs plays the eating animation and is never held, moved or bitten. This appends the
     * SAME block for every other entity in the tag.
     *
     * Injected at TAIL on purpose: Alex's Caves clears the held-mob id earlier in the same tick when its own
     * condition does not hold ({@code if (!held && getHeldMobId() != -1) setHeldMobId(-1)}), so setting it
     * before that line would be undone. Running last leaves the synched value correct for the render layer.
     */
    @Inject(method = "tick", at = @At("TAIL"))
    private void primordialmobs$holdAnyFish(CallbackInfo ci) {
        RelicheirusEntity self = (RelicheirusEntity) (Object) this;
        if (self.level().isClientSide) {
            return;
        }
        LivingEntity target = self.getTarget();
        if (target == null || self.getAnimation() != RelicheirusEntity.ANIMATION_EAT_TRILOCARIS) {
            return;
        }
        // Alex's Caves already handles the Trilocaris itself; only the extra fish need us.
        if (!CompatDinosaurs.isExtraFish(target) || target.distanceTo(self) >= 10.0F) {
            return;
        }
        int tick = self.getAnimationTick();
        if (tick < 20) {
            self.setHeldMobId(target.getId());
        } else if (tick <= 50) {
            target.setPos(primordialmobs$heldFishPos(self));
            if (tick >= 45 && target.isAlive()) {
                target.hurt(self.damageSources().mobAttack(self), 20);
            }
            self.setHeldMobId(target.getId());
            target.fallDistance = 0.0F;
        }
    }

    /**
     * Copy of Alex's Caves' private {@code getTrilocarisPos()}: the point in front of and above the beak
     * where a swallowed animal rides during the eating animation. Replicated rather than shadowed because
     * the method is private and a {@code @Shadow} of a private member of a production jar is brittle.
     */
    private static Vec3 primordialmobs$heldFishPos(RelicheirusEntity self) {
        Vec3 fishUp = new Vec3(0, 0F, 1.5F);
        if (self.getAnimationTick() >= 15F) {
            float anim1 = Math.min(self.getAnimationTick() - 15F, 15F) / 15F;
            float anim2 = Math.min(self.getAnimationTick(), 15F) / 15F;
            fishUp = fishUp.add(0, (self.getEyeHeight() + 1F) * anim1, anim2 * -1F + 1F);
        }
        Vec3 head = fishUp.xRot(-self.getXRot() * ((float) Math.PI / 180F)).yRot(-self.getYHeadRot() * ((float) Math.PI / 180F));
        return self.position().add(head);
    }

    /**
     * Same water drag as standalone (see RelicheirusEntity#getWaterSlowDown): the Logger wades and
     * fishes, so it should not be as sluggish in water as Alex's Caves' land-bound default 0.8F.
     */
    @Override
    protected float getWaterSlowDown() {
        return com.primordialmobs.server.entity.living.RelicheirusEntity.WATER_SLOWDOWN;
    }

    public boolean canOwnerMount(Player player) {
        return !this.isBaby();
    }

    public boolean canOwnerCommand(Player ownerPlayer) {
        return ownerPlayer.isShiftKeyDown();
    }

    /** The body lowers onto the haunches but the long neck stays up, so trim the box only modestly. */
    @Override
    public EntityDimensions getDimensions(Pose pose) {
        EntityDimensions dimensions = super.getDimensions(pose);
        if (this.isInSittingPose()) {
            return EntityDimensions.scalable(dimensions.width, dimensions.height * 0.85F);
        }
        return dimensions;
    }

    @Override
    public LivingEntity getControllingPassenger() {
        Entity entity = this.getFirstPassenger();
        if (entity instanceof Player player && this.isTame() && this.isOwnedBy(player)) {
            return player;
        }
        return null;
    }

    @Override
    protected Vec3 getRiddenInput(Player player, Vec3 deltaIn) {
        float strafe = player.xxa * 0.5F;
        float forward = player.zza < 0.0F ? player.zza * 0.35F : player.zza;
        return new Vec3(strafe, 0.0D, forward);
    }

    @Override
    protected void tickRidden(Player player, Vec3 vec3) {
        super.tickRidden(player, vec3);
        if (player.zza != 0 || player.xxa != 0) {
            this.setRot(player.getYRot(), player.getXRot() * 0.25F);
            this.setYHeadRot(player.getYHeadRot());
            this.setTarget(null);
        }
    }

    /** Rides notably more slowly and deliberately than it walks free. */
    @Override
    protected float getRiddenSpeed(Player rider) {
        return (float) (this.getAttributeValue(Attributes.MOVEMENT_SPEED) * 0.7D);
    }

    @Override
    public void positionRider(Entity passenger, MoveFunction moveFunction) {
        if (this.isPassengerOfSameVehicle(passenger) && passenger instanceof LivingEntity living && !this.touchingUnloadedChunk()) {
            // +z is the mob's FORWARD direction once rotated, so a positive value seats the rider further
            // onto the back/shoulders (toward the neck) rather than hanging off the rump.
            Vec3 seatOffset = new Vec3(0.0F, 0.0F, 0.15F).yRot((float) Math.toRadians(-this.yBodyRot));
            living.setYBodyRot(this.yBodyRot);
            passenger.fallDistance = 0.0F;
            clampRotation(living, 105);
            // These two terms mirror exactly what RelicheirusModel applies to the body: a walk-cycle bob plus
            // the leg-solver ground adaptation. 0.8 * 0.38 = 0.304 matches the ridden walk cadence.
            RelicheirusEntity self = (RelicheirusEntity) (Object) this;
            float limbSwing = this.walkAnimation.position();
            float limbSwingAmount = this.walkAnimation.speed();
            float bodyWalkBob = PMMath.walkValue(limbSwing, limbSwingAmount, 0.304F, -1.5F, 4F, false);
            float legMax = Math.max(
                    Math.max(self.legSolver.backLeft.getHeight(1.0F), self.legSolver.backRight.getHeight(1.0F)),
                    Math.max(self.legSolver.frontLeft.getHeight(1.0F), self.legSolver.frontRight.getHeight(1.0F))) * 0.8F;
            double bodyDrop = bodyWalkBob / 16.0D + legMax;
            moveFunction.accept(passenger, this.getX() + seatOffset.x, this.getY() + seatOffset.y + PRIMORDIALMOBS$RIDER_BACK_HEIGHT - bodyDrop, this.getZ() + seatOffset.z);
        } else {
            super.positionRider(passenger, moveFunction);
        }
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity dismounter) {
        return new Vec3(this.getX(), this.getBoundingBox().minY, this.getZ());
    }
}
