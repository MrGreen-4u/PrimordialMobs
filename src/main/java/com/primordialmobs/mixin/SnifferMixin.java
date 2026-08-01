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

    protected SnifferMixin(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void ac_defineSkinData(EntityType entityType, Level level, CallbackInfo ci) {
        this.entityData.define(AC_SKIN_TYPE, 0);
        this.entityData.define(AC_RECOLORED, false);
        this.entityData.define(AC_OWNER, Optional.empty());
        this.entityData.define(AC_COMMAND, 0);
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

    /**
     * Sitting uses the digging lie-down pose, and in the Primordial Caves the sniffing/digging feature is
     * disabled entirely, so in both situations the brain must never start scenting/sniffing/searching.
     */
    @Unique
    private boolean ac_blocksSniffing() {
        return this.ac_isOrderedToSit() || SnifferTaming.isInPrimordialCaves((Sniffer) (Object) this);
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
