package com.primordialmobs.mixin.compat;

import com.github.alexmodguy.alexscaves.server.entity.living.DinosaurEntity;
import com.github.alexmodguy.alexscaves.server.entity.living.GrottoceratopsEntity;
import com.primordialmobs.compat.CompatDinosaurs;
import com.primordialmobs.server.misc.PMMath;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Compat only: turns Alex's Caves' Grottoceratops into the Grazer.
 *
 * Everything Alex's Caves already gives it (attacks, grazing, eggs, loot, sounds, animations) is our code
 * verbatim — the two classes are identical upstream. What Primordial Mobs adds on top is this file:
 * a mount you can command and ride, a lowered hitbox while it is parked, and its Serene Salad reaction.
 *
 * All of these are declared as plain methods rather than injections: Alex's Caves' Grottoceratops does not
 * declare them at all, so adding them here overrides what it inherits from {@code DinosaurEntity} /
 * {@code Mob}. The vanilla-named ones ({@code getRiddenInput}, {@code positionRider}, ...) are renamed to
 * their SRG ids by ForgeGradle's reobf pass, exactly like the compat renderers' {@code getTextureLocation}
 * bridge, so they really do override at runtime.
 *
 * The mixin extends {@code DinosaurEntity} — the target's own superclass — so protected members like
 * {@code clampRotation} resolve; the constructor is discarded by Mixin.
 */
@Mixin(GrottoceratopsEntity.class)
public abstract class GrottoceratopsEntityMixin extends DinosaurEntity {

    /** Four minutes of Haste I: what a Grazer gives back for a Serene Salad. */
    private static final int PRIMORDIALMOBS$SALAD_HASTE_TICKS = 4 * 60 * 20;

    protected GrottoceratopsEntityMixin(EntityType<?> type, Level level) {
        super(type, level);
    }

    /**
     * Alex's Caves returns false here, which is the single reason its Grottoceratops can never be ridden even
     * once tamed: its DinosaurEntity.mobInteract already contains the whole mounting branch behind this gate.
     */
    public boolean canOwnerMount(Player player) {
        return !this.isBaby();
    }

    public boolean canOwnerCommand(Player ownerPlayer) {
        return ownerPlayer.isShiftKeyDown();
    }

    /** Hand a Grazer a Serene Salad and it passes its appetite for stone on to you: Haste I, four minutes. */
    @Override
    public boolean onFeedMixture(ItemStack itemStack, Player player) {
        if (itemStack.is(CompatDinosaurs.sereneSalad())) {
            if (!this.level().isClientSide) {
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, PRIMORDIALMOBS$SALAD_HASTE_TICKS, 0));
                this.level().broadcastEntityEvent(this, (byte) 7);
            }
            return true;
        }
        return false;
    }

    /** Couched with the chest on the ground, so the standing-height box should drop noticeably. */
    @Override
    public EntityDimensions getDimensions(Pose pose) {
        EntityDimensions dimensions = super.getDimensions(pose);
        if (this.isInSittingPose()) {
            return EntityDimensions.scalable(dimensions.width, dimensions.height * 0.78F);
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

    @Override
    protected float getRiddenSpeed(Player rider) {
        return (float) this.getAttributeValue(Attributes.MOVEMENT_SPEED);
    }

    @Override
    public void positionRider(Entity passenger, MoveFunction moveFunction) {
        if (this.isPassengerOfSameVehicle(passenger) && passenger instanceof LivingEntity living && !this.touchingUnloadedChunk()) {
            // Seat rests on the broad back, just behind the frill. +z is FORWARD once rotated.
            Vec3 seatOffset = new Vec3(0.0F, 0.0F, 0.35F).yRot((float) Math.toRadians(-this.yBodyRot));
            living.setYBodyRot(this.yBodyRot);
            passenger.fallDistance = 0.0F;
            clampRotation(living, 105);
            // Track the body's vertical motion so the rider rises/falls WITH the gait. These terms mirror
            // exactly what GrottoceratopsModel applies to the body: a walk-cycle bob plus the leg-solver
            // ground adaptation (max over all four legs).
            GrottoceratopsEntity self = (GrottoceratopsEntity) (Object) this;
            float limbSwing = this.walkAnimation.position();
            float limbSwingAmount = this.walkAnimation.speed();
            float bodyBob = PMMath.walkValue(limbSwing, limbSwingAmount, 0.5F * 1.5F, 0.5F, 2.4F, true);
            float legMax = Math.max(
                    Math.max(self.legSolver.backLeft.getHeight(1.0F), self.legSolver.backRight.getHeight(1.0F)),
                    Math.max(self.legSolver.frontLeft.getHeight(1.0F), self.legSolver.frontRight.getHeight(1.0F))) * 0.8F;
            double bodyDrop = bodyBob / 16.0D + legMax;
            moveFunction.accept(passenger, this.getX() + seatOffset.x, this.getY() + seatOffset.y + this.getPassengersRidingOffset() - bodyDrop, this.getZ() + seatOffset.z);
        } else {
            super.positionRider(passenger, moveFunction);
        }
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity dismounter) {
        return new Vec3(this.getX(), this.getBoundingBox().minY, this.getZ());
    }
}
