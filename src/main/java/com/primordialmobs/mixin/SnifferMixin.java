package com.primordialmobs.mixin;

import com.primordialmobs.server.entity.util.SnifferSkinHolder;
import com.primordialmobs.server.entity.util.SnifferTaming;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.UUID;

@Mixin(Sniffer.class)
public abstract class SnifferMixin extends Animal implements SnifferSkinHolder {

    @Unique
    private static final EntityDataAccessor<Integer> AC_SKIN_TYPE = SynchedEntityData.defineId(Sniffer.class, EntityDataSerializers.INT);
    @Unique
    private static final EntityDataAccessor<Boolean> AC_RECOLORED = SynchedEntityData.defineId(Sniffer.class, EntityDataSerializers.BOOLEAN);
    @Unique
    private static final EntityDataAccessor<Optional<UUID>> AC_OWNER = SynchedEntityData.defineId(Sniffer.class, EntityDataSerializers.OPTIONAL_UUID);
    @Unique
    private static final EntityDataAccessor<Integer> AC_COMMAND = SynchedEntityData.defineId(Sniffer.class, EntityDataSerializers.INT);

    /**
     * Ticks left of the stand-up (RISING) animation after being told to stop sitting; when it reaches 0
     * the sniffer is put back to IDLING (the brain never manages RISING for us because it did not start
     * the dig).
     */
    @Unique
    private int ac_risingTicks;

    /** Head-turn speed while resting, in degrees per tick: slow enough to read as a calm glance. */
    @Unique
    private static final float AC_LOOK_SPEED = 8.0F;
    /**
     * Body yaw held while lying down. A resting sniffer turns its head (see ac_restingLook) but its body
     * must not pivot on the ground: measured on a server, a sitting Grazer/Logger/Roarer never rotates its
     * body either (a stationary mob's BodyRotationControl clamps the head instead of turning the body), so
     * pinning the yaw here keeps the sniffer consistent with them even if it is shoved or knocked back.
     */
    @Unique
    private float ac_sitYaw;
    @Unique
    private boolean ac_sitYawSet;
    @Unique
    private int ac_lookScanTicks;
    @Unique
    private int ac_idleGlanceTicks;
    @Unique
    @Nullable
    private LivingEntity ac_lookTarget;

    /** Seething Stew rage: ticks left, the current victim, and the headbutt cooldown (server side). */
    @Unique
    private int ac_rageTicks;
    @Unique
    private int ac_rageAttackCooldown;
    @Unique
    @Nullable
    private LivingEntity ac_rageTarget;
    /** Synched on/off rage state, so the client can suppress sniffing/scenting animations. */
    @Unique
    private static final EntityDataAccessor<Boolean> AC_ENRAGED = SynchedEntityData.defineId(Sniffer.class, EntityDataSerializers.BOOLEAN);
    /**
     * Entity-event byte broadcast when a headbutt LANDS: the client answers by raising the head for
     * about a second (see ac_headbuttPoseTicks). Vanilla entity events stop at 63, so 78 is free.
     */
    @Unique
    private static final byte AC_HEADBUTT_EVENT = 78;
    /**
     * Client-side countdown that keeps the head raised after a landed headbutt. While it runs the
     * posture eases up (5 ticks), holds, and once it expires eases back down (5 more ticks) — a
     * ~1 second toss of the snout per hit instead of a permanently reared rage stance.
     */
    @Unique
    private int ac_headbuttPoseTicks;
    /** The head-toss posture, eased over 5 ticks and interpolated for smooth rendering. */
    @Unique
    private float ac_angryProgress;
    @Unique
    private float ac_angryProgressPrev;

    protected SnifferMixin(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void ac_defineSkinData(EntityType entityType, Level level, CallbackInfo ci) {
        this.entityData.define(AC_SKIN_TYPE, 0);
        this.entityData.define(AC_RECOLORED, false);
        this.entityData.define(AC_OWNER, Optional.empty());
        this.entityData.define(AC_COMMAND, 0);
        this.entityData.define(AC_ENRAGED, false);
    }

    @Override
    public int ac_getSkinType() {
        return this.entityData.get(AC_SKIN_TYPE);
    }

    @Override
    public void ac_setSkinType(int type) {
        this.entityData.set(AC_SKIN_TYPE, type);
        this.getPersistentData().putInt("ACSkinType", type);
    }

    @Override
    public boolean ac_isRecolored() {
        return this.entityData.get(AC_RECOLORED);
    }

    @Override
    public void ac_setRecolored(boolean recolored) {
        this.entityData.set(AC_RECOLORED, recolored);
        this.getPersistentData().putBoolean("ACRecolored", recolored);
    }

    @Override
    public UUID ac_getOwnerUUID() {
        return this.entityData.get(AC_OWNER).orElse(null);
    }

    @Override
    public void ac_setOwnerUUID(UUID owner) {
        this.entityData.set(AC_OWNER, Optional.ofNullable(owner));
        if (owner == null) {
            this.getPersistentData().remove("ACSnifferOwner");
        } else {
            this.getPersistentData().putUUID("ACSnifferOwner", owner);
        }
    }

    @Override
    public int ac_getCommand() {
        return this.entityData.get(AC_COMMAND);
    }

    @Override
    public void ac_setCommand(int command) {
        int previous = this.entityData.get(AC_COMMAND);
        this.entityData.set(AC_COMMAND, command);
        this.getPersistentData().putInt("ACSnifferCommand", command);
        if (!this.level().isClientSide) {
            Sniffer self = (Sniffer) (Object) this;
            if (previous == 1 && command != 1 && ((SnifferAccessor) this).ac_invokeGetState() == Sniffer.State.DIGGING) {
                self.transitionTo(Sniffer.State.RISING);
                this.ac_risingTicks = 25;
            }
            if (command == 1) {
                this.getNavigation().stop();
            } else {
                this.ac_sitYawSet = false;                 // release the pinned body yaw when it stands up
            }
        }
    }

    @Override
    public void ac_enrage(int ticks) {
        this.ac_rageTicks = ticks;
        this.getPersistentData().putInt("ACSnifferRage", ticks);
        this.ac_rageTarget = null;
        this.ac_rageAttackCooldown = 0;
        if (!this.level().isClientSide) {
            this.entityData.set(AC_ENRAGED, ticks > 0);
        }
    }

    @Override
    public boolean ac_isEnraged() {
        return this.entityData.get(AC_ENRAGED);
    }

    @Override
    public float ac_getAngryHeadAmount(float partialTick) {
        return (this.ac_angryProgressPrev + (this.ac_angryProgress - this.ac_angryProgressPrev) * partialTick) / 5.0F;
    }

    /**
     * Eases the head-toss posture in while a landed headbutt's pulse runs and back out once it
     * expires. The pulse is only ever armed client side (broadcastEntityEvent), which is fine:
     * the posture is pure rendering state.
     */
    @Inject(method = "tick", at = @At("TAIL"))
    private void ac_easeAngryPosture(CallbackInfo ci) {
        this.ac_angryProgressPrev = this.ac_angryProgress;
        if (this.ac_headbuttPoseTicks > 0) {
            this.ac_headbuttPoseTicks--;
            if (this.ac_angryProgress < 5.0F) {
                this.ac_angryProgress++;
            }
        } else if (this.ac_angryProgress > 0.0F) {
            this.ac_angryProgress--;
        }
    }

    /** Client side: a landed headbutt raises the head for ~1 second (15 pulse ticks + 5 easing out). */
    @Override
    public void handleEntityEvent(byte id) {
        if (id == AC_HEADBUTT_EVENT) {
            this.ac_headbuttPoseTicks = 15;
        } else {
            super.handleEntityEvent(id);
        }
    }

    /**
     * Sitting uses the digging lie-down pose, in the Primordial Caves the sniffing/digging feature is
     * disabled entirely, and a seething sniffer has other things on its mind — in all three situations
     * the brain must never start scenting/sniffing/searching.
     */
    @Unique
    private boolean ac_blocksSniffing() {
        return this.ac_isOrderedToSit() || this.ac_isEnraged() || SnifferTaming.isInPrimordialCaves((Sniffer) (Object) this);
    }

    @Inject(method = "canSniff", at = @At("HEAD"), cancellable = true)
    private void ac_blockSniffing(CallbackInfoReturnable<Boolean> cir) {
        if (this.ac_blocksSniffing()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "canDig()Z", at = @At("HEAD"), cancellable = true)
    private void ac_blockDigging(CallbackInfoReturnable<Boolean> cir) {
        if (this.ac_blocksSniffing()) {
            cir.setReturnValue(false);
        }
    }

    /**
     * The sit pose reuses the DIGGING state (for its lie-down animation), but a resting pet must not spray
     * the block-breaking "digging" particles or emit the ground-shake vibration. Suppress them while sitting;
     * a wild sniffer digging naturally is unaffected.
     */
    @Inject(method = "emitDiggingParticles", at = @At("HEAD"), cancellable = true)
    private void ac_noSitDiggingParticles(net.minecraft.world.entity.AnimationState animationState, CallbackInfoReturnable<Sniffer> cir) {
        if (this.ac_isOrderedToSit()) {
            cir.setReturnValue((Sniffer) (Object) this);
        }
    }

    /**
     * Belt-and-suspenders: the forced sit-DIGGING never arms the seed-drop timer (we use the raw state
     * setter, not transitionTo), but if a sniffer is told to sit mid-natural-dig its timer could still be
     * armed. Block the actual extraction while sitting so a sitting pet never digs plants out of the ground.
     */
    @Inject(method = "dropSeed", at = @At("HEAD"), cancellable = true)
    private void ac_noSitSeedDrop(CallbackInfo ci) {
        if (this.ac_isOrderedToSit()) {
            ci.cancel();
        }
    }

    /**
     * While sitting or following, the brain (wander/sniff behaviors) is suspended and we drive the sniffer
     * ourselves, mirroring how a commanded dinosaur ignores its idle AI.
     */
    @Inject(method = "customServerAiStep", at = @At("HEAD"), cancellable = true)
    private void ac_commandedAiStep(CallbackInfo ci) {
        Sniffer self = (Sniffer) (Object) this;
        SnifferAccessor accessor = (SnifferAccessor) this;
        if (this.ac_risingTicks > 0 && --this.ac_risingTicks == 0 && accessor.ac_invokeGetState() == Sniffer.State.RISING) {
            self.transitionTo(Sniffer.State.IDLING);
        }
        if (this.ac_rageTicks > 0) {
            // The Seething Stew rage runs the sniffer itself; the vanilla brain (wander, scenting)
            // stays suspended exactly like it does for a commanded sit/follow. Navigation and the
            // look control still tick in Mob#serverAiStep after this returns.
            this.ac_rageStep();
            ci.cancel();
            return;
        }
        int command = this.ac_getCommand();
        if (command == 1) {
            // The raw state setter keeps the seed-drop timer unarmed: pose only, no ground sniffing and no
            // seed extraction (see SnifferAccessor#ac_invokeSetState).
            if (this.ac_risingTicks == 0 && accessor.ac_invokeGetState() != Sniffer.State.DIGGING) {
                accessor.ac_invokeSetState(Sniffer.State.DIGGING);
            }
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.0D, 1.0D, 0.0D));
            if (!this.getNavigation().isDone()) {
                this.getNavigation().stop();
            }
            this.ac_pinBodyYaw();
            this.ac_restingLook();
            ci.cancel();
        } else if (command == 2 && this.ac_isTame()) {
            this.ac_followOwner();
            ci.cancel();
        }
    }

    /**
     * A resting sniffer is not a statue: its brain is frozen while it sits, but Mob#serverAiStep still ticks
     * the look control afterwards, so all it needs is something to look at. It watches its owner, then any
     * nearby player, then whatever animal wanders past, and glances idly around when nothing is there.
     * The rotation speed is deliberately low (a calm head turn) and the sniffer's own +-50 degree head limit
     * keeps the body still unless something walks right around behind it.
     */
    /** Freezes the lying body's direction without freezing the animal: only the head keeps moving. */
    @Unique
    private void ac_pinBodyYaw() {
        if (!this.ac_sitYawSet) {
            this.ac_sitYaw = this.getYRot();
            this.ac_sitYawSet = true;
        }
        this.setYRot(this.ac_sitYaw);
        this.yRotO = this.ac_sitYaw;
        this.yBodyRot = this.ac_sitYaw;
        this.yBodyRotO = this.ac_sitYaw;
    }

    @Unique
    private void ac_restingLook() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (this.ac_lookScanTicks-- <= 0) {
            this.ac_lookScanTicks = 20;
            this.ac_lookTarget = this.ac_findSomethingToWatch(serverLevel);
        }
        LivingEntity watched = this.ac_lookTarget;
        if (watched != null && watched.isAlive() && watched.distanceToSqr(this) < 256.0D) {
            this.getLookControl().setLookAt(watched, AC_LOOK_SPEED, AC_LOOK_SPEED);
            this.ac_idleGlanceTicks = 0;
            return;
        }
        if (this.ac_idleGlanceTicks-- <= 0) {
            this.ac_idleGlanceTicks = 60 + this.random.nextInt(100);
            double yaw = Math.toRadians(this.getYRot() + (this.random.nextFloat() - 0.5F) * 80.0F);
            this.getLookControl().setLookAt(
                    this.getX() - Math.sin(yaw) * 6.0D,
                    this.getEyeY() + (this.random.nextFloat() - 0.35F) * 2.0D,
                    this.getZ() + Math.cos(yaw) * 6.0D,
                    AC_LOOK_SPEED, AC_LOOK_SPEED);
        }
    }

    @Unique
    @Nullable
    private LivingEntity ac_findSomethingToWatch(ServerLevel serverLevel) {
        UUID ownerId = this.ac_getOwnerUUID();
        Player owner = ownerId == null ? null : serverLevel.getPlayerByUUID(ownerId);
        if (owner != null && !owner.isSpectator() && owner.distanceToSqr(this) < 144.0D) {
            return owner;
        }
        Player player = serverLevel.getNearestPlayer(this, 12.0D);
        if (player != null && !player.isSpectator()) {
            return player;
        }
        LivingEntity closest = null;
        double closestDist = Double.MAX_VALUE;
        for (LivingEntity candidate : serverLevel.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8.0D))) {
            if (candidate == (Object) this || !candidate.isAlive()) {
                continue;
            }
            double dist = candidate.distanceToSqr(this);
            if (dist < closestDist) {
                closestDist = dist;
                closest = candidate;
            }
        }
        return closest;
    }

    @Unique
    private void ac_followOwner() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        UUID ownerId = this.ac_getOwnerUUID();
        Player owner = ownerId == null ? null : serverLevel.getPlayerByUUID(ownerId);
        if (owner == null || owner.isSpectator() || !owner.isAlive()) {
            return;
        }
        double distSq = this.distanceToSqr(owner);
        if (distSq > 144.0D) {
            this.ac_teleportNearOwner(owner);
        } else if (distSq > 36.0D) {
            this.getNavigation().moveTo(owner, 1.2D);
        }
        this.getLookControl().setLookAt(owner, 10.0F, (float) this.getMaxHeadXRot());
    }

    /**
     * The Seething Stew rage: for a minute the sniffer charges the hostile mobs around it and
     * headbutts them across the room. It prefers whatever last hurt it (never its owner), then the
     * nearest monster in sight. With nothing to fight it paws the ground where it stands until the
     * seething wears off. Damage is dealt directly ({@code hurt}) because the vanilla Sniffer has no
     * attack-damage attribute to route {@code doHurtTarget} through.
     */
    @Unique
    private void ac_rageStep() {
        Sniffer self = (Sniffer) (Object) this;
        this.ac_rageTicks--;
        if (this.ac_rageTicks <= 0) {
            this.entityData.set(AC_ENRAGED, false);
            this.getPersistentData().remove("ACSnifferRage");
            return;
        }
        if (this.ac_rageTicks % 20 == 0) {
            this.getPersistentData().putInt("ACSnifferRage", this.ac_rageTicks);
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.ANGRY_VILLAGER,
                        this.getRandomX(0.7D), this.getEyeY() + 0.6D, this.getRandomZ(0.7D),
                        1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
        }
        if (this.ac_rageAttackCooldown > 0) {
            this.ac_rageAttackCooldown--;
        }
        if (this.ac_rageTarget == null || !this.ac_rageTarget.isAlive() || this.ac_rageTicks % 10 == 0) {
            this.ac_rageTarget = this.ac_findRageTarget();
        }
        LivingEntity target = this.ac_rageTarget;
        if (target == null) {
            return;
        }
        this.getNavigation().moveTo(target, 1.4D);
        this.getLookControl().setLookAt(target, 30.0F, 30.0F);
        double reach = this.getBbWidth() * 0.5D + target.getBbWidth() * 0.5D + 1.2D;
        if (this.ac_rageAttackCooldown == 0 && this.distanceTo(target) < reach && this.hasLineOfSight(target)) {
            this.ac_rageAttackCooldown = 25;
            if (target.hurt(this.damageSources().mobAttack(self), 6.0F)) {
                // the headbutt: vanilla mob knockback along the sniffer's facing, half again as hard
                target.knockback(0.9D,
                        Math.sin(this.getYRot() * (Math.PI / 180.0D)),
                        -Math.cos(this.getYRot() * (Math.PI / 180.0D)));
                // tell watching clients the blow landed so they play the ~1s head-toss
                this.level().broadcastEntityEvent(self, AC_HEADBUTT_EVENT);
                this.playSound(net.minecraft.sounds.SoundEvents.SNIFFER_SNIFFING, 1.0F, 0.6F);
                if (this.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.CRIT,
                            target.getX(), target.getY(0.6D), target.getZ(), 5, 0.3D, 0.3D, 0.3D, 0.1D);
                }
            }
        }
    }

    @Unique
    @Nullable
    private LivingEntity ac_findRageTarget() {
        LivingEntity attacker = this.getLastHurtByMob();
        UUID ownerId = this.ac_getOwnerUUID();
        if (attacker != null && attacker.isAlive() && !(attacker instanceof Sniffer)
                && (ownerId == null || !ownerId.equals(attacker.getUUID()))
                && this.distanceToSqr(attacker) < 400.0D) {
            return attacker;
        }
        LivingEntity closest = null;
        double closestDist = Double.MAX_VALUE;
        for (LivingEntity candidate : this.level().getEntitiesOfClass(net.minecraft.world.entity.monster.Monster.class,
                this.getBoundingBox().inflate(12.0D), monster -> monster.isAlive() && this.hasLineOfSight(monster))) {
            double dist = candidate.distanceToSqr(this);
            if (dist < closestDist) {
                closestDist = dist;
                closest = candidate;
            }
        }
        return closest;
    }

    @Unique
    private void ac_teleportNearOwner(Player owner) {
        BlockPos base = owner.blockPosition();
        for (int attempt = 0; attempt < 10; attempt++) {
            int x = base.getX() + this.random.nextInt(7) - 3;
            int z = base.getZ() + this.random.nextInt(7) - 3;
            if (Math.abs(x - base.getX()) < 2 && Math.abs(z - base.getZ()) < 2) {
                continue;
            }
            BlockPos target = new BlockPos(x, base.getY(), z);
            if (this.level().getBlockState(target.below()).isSolidRender(this.level(), target.below())
                    && this.level().noCollision(this, this.getBoundingBox().move(target.getX() + 0.5D - this.getX(), target.getY() - this.getY(), target.getZ() + 0.5D - this.getZ()))) {
                this.moveTo(target.getX() + 0.5D, target.getY(), target.getZ() + 0.5D, this.getYRot(), this.getXRot());
                this.getNavigation().stop();
                return;
            }
        }
    }
}
