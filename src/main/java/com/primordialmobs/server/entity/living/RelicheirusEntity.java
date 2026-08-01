package com.primordialmobs.server.entity.living;

import com.primordialmobs.server.block.PMBlockRegistry;
import com.primordialmobs.server.block.PewenBranchBlock;
import com.primordialmobs.server.entity.PMEntityRegistry;
import com.primordialmobs.server.entity.ai.*;
import com.primordialmobs.server.item.PMItemRegistry;
import com.primordialmobs.server.misc.PMMath;
import com.primordialmobs.server.misc.PMSoundRegistry;
import com.primordialmobs.server.misc.PMTagRegistry;
import com.github.alexthe666.citadel.animation.Animation;
import com.github.alexthe666.citadel.animation.AnimationHandler;
import com.github.alexthe666.citadel.animation.IAnimatedEntity;
import com.github.alexthe666.citadel.animation.LegSolverQuadruped;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class RelicheirusEntity extends DinosaurEntity implements IAnimatedEntity {
    public LegSolverQuadruped legSolver = new LegSolverQuadruped(-0.15F, 0.6F, 0.5F, 0.75F, 1);
    private static final EntityDataAccessor<Integer> PECK_Y = SynchedEntityData.defineId(RelicheirusEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> HELD_MOB_ID = SynchedEntityData.defineId(RelicheirusEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> PUSHING_TREES_FOR = SynchedEntityData.defineId(RelicheirusEntity.class, EntityDataSerializers.INT);
    private Animation currentAnimation;
    private int animationTick;
    public static final Animation ANIMATION_SPEAK_1 = Animation.create(13);
    public static final Animation ANIMATION_SPEAK_2 = Animation.create(20);
    public static final Animation ANIMATION_EAT_TREE = Animation.create(40);
    public static final Animation ANIMATION_EAT_TRILOCARIS = Animation.create(50);
    /**
     * The relicheirus fishes any small water animal in #alexscaves:relicheirus_fishes (the Trilocaris plus
     * cod, salmon, tropical fish, pufferfish and tadpoles), not just the Trilocaris.
     */
    private static final Predicate<LivingEntity> RELICHEIRUS_FISHES = living -> living.getType().is(PMTagRegistry.RELICHEIRUS_FISHES);

    public static boolean isFishable(Entity entity) {
        return entity.getType().is(PMTagRegistry.RELICHEIRUS_FISHES);
    }

    public static final Animation ANIMATION_PUSH_TREE = Animation.create(60);
    public static final Animation ANIMATION_SCRATCH_1 = Animation.create(60);
    public static final Animation ANIMATION_SCRATCH_2 = Animation.create(40);
    public static final Animation ANIMATION_SHAKE = Animation.create(30);
    public static final Animation ANIMATION_MELEE_SLASH_1 = Animation.create(20);
    public static final Animation ANIMATION_MELEE_SLASH_2 = Animation.create(20);
    private float prevRaiseArmsAmount = 0;
    private float raiseArmsAmount = 0;

    public RelicheirusEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(PECK_Y, 0);
        this.entityData.define(HELD_MOB_ID, -1);
        this.entityData.define(PUSHING_TREES_FOR, 0);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.ATTACK_DAMAGE, 12.0D).add(Attributes.MOVEMENT_SPEED, 0.22D).add(Attributes.MAX_HEALTH, 120.0D);
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(2, new RelicheirusMeleeGoal(this));
        this.goalSelector.addGoal(3, new AnimalFollowOwnerGoal(this, 1.2D, 6.0F, 3.0F, false) {
            @Override
            public boolean shouldFollow() {
                return RelicheirusEntity.this.getCommand() == 2;
            }
        });
        this.goalSelector.addGoal(4, new AnimalBreedEggsGoal(this, 1));
        this.goalSelector.addGoal(5, new AnimalLayEggGoal(this, 100, 1));
        this.goalSelector.addGoal(6, new TemptGoal(this, 1.1D, Ingredient.of(PMBlockRegistry.TREE_STAR.get()), false));
        this.goalSelector.addGoal(7, new RelicheirusPushTreesGoal(this, 25));
        this.goalSelector.addGoal(8, new RelicheirusNibblePewensGoal(this, 20));
        this.goalSelector.addGoal(9, new RandomStrollGoal(this, 1.0D, 45));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(11, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, (new HurtByTargetGoal(this, RelicheirusEntity.class)));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 100, true, false, RELICHEIRUS_FISHES));
    }

    protected PathNavigation createNavigation(Level level) {
        return new AdvancedPathNavigateNoTeleport(this, level);
    }

    protected void playStepSound(BlockPos pos, BlockState state) {
        if (!this.isBaby()) {
            this.playSound(PMSoundRegistry.RELICHEIRUS_STEP.get(), 1.0F, 1.0F);
        }
    }

    protected float getStandingEyeHeight(Pose pose, EntityDimensions dimensions) {
        return 0.99F * dimensions.height;
    }

    @Override
    public boolean onFeedMixture(ItemStack itemStack, Player player) {
        if (itemStack.is(PMItemRegistry.PRIMORDIAL_SOUP.get())) {
            this.setPushingTreesFor(1200);
            return true;
        }
        return false;
    }

    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (!this.isTame() && !this.isBaby() && this.isTamingFood(itemstack)) {
            if (!this.level().isClientSide) {
                this.usePlayerItem(player, hand, itemstack);
                if (this.getRandom().nextInt(3) == 0) {
                    this.tame(player);
                    this.setCommand(1);
                    this.setOrderedToSit(true);
                    this.level().broadcastEntityEvent(this, (byte) 7);
                } else {
                    this.level().broadcastEntityEvent(this, (byte) 6);
                }
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        InteractionResult prev = super.mobInteract(player, hand);
        if (!prev.consumesAction() && itemstack.is(PMItemRegistry.PRIMORDIAL_SOUP.get())) {
            if (!itemstack.getCraftingRemainingItem().isEmpty()) {
                this.spawnAtLocation(itemstack.getCraftingRemainingItem().copy());
            }
            this.usePlayerItem(player, hand, itemstack);
            return InteractionResult.SUCCESS;
        }
        return prev;
    }


    /**
     * The Logger wades and fishes for a living, so it is less sluggish in water than a land dinosaur.
     *
     * This is vanilla's per-tick water drag: velocity *= factor, so terminal speed is a*f/(1-f); vanilla's
     * land default is 0.8F. The value is CALIBRATED, not derived: measured on a headless server with the
     * same chase goal on a land track vs a flooded channel (see anotaciones.md, 2026-08-01), 0.84 puts the
     * sustained swim speed at ~1.15x the sustained walk speed, which is the design brief's target. Tune
     * HERE if it feels wrong (0.87 measured ~1.5x land).
     */
    @Override
    protected float getWaterSlowDown() {
        return WATER_SLOWDOWN;
    }

    /** See {@link #getWaterSlowDown()}. */
    public static final float WATER_SLOWDOWN = 0.84F;

    public boolean isTamingFood(ItemStack stack) {
        return stack.is(PMBlockRegistry.TREE_STAR.get().asItem());
    }

    @Override
    public float getSittingDimensionScale() {
        // The body lowers onto the haunches but the long neck stays up, so trim the box only modestly.
        return 0.85F;
    }

    public boolean canOwnerMount(Player player) {
        // Never mountable while the Seething Stew has it in tree-felling frenzy.
        return !this.isBaby() && this.getPushingTreesFor() <= 0;
    }

    public boolean canOwnerCommand(Player ownerPlayer) {
        return ownerPlayer.isShiftKeyDown();
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
        // Rides notably more slowly and deliberately than it walks free.
        return (float) (this.getAttributeValue(Attributes.MOVEMENT_SPEED) * 0.7D);
    }

    // Saddle height on the BACK (lomo), over the shoulders -- never up on the neck/head. The
    // bounding box height (5.9) is dominated by the long upright neck, so getPassengersRidingOffset()
    // (~4.4) would seat the rider on the head; this fixed value sits on the upper back.
    private static final float RIDER_BACK_HEIGHT = 3.1F;

    @Override
    public void positionRider(Entity passenger, MoveFunction moveFunction) {
        if (this.isPassengerOfSameVehicle(passenger) && passenger instanceof LivingEntity living && !this.touchingUnloadedChunk()) {
            // +z is the mob's FORWARD direction once rotated, so a positive value seats the rider
            // further onto the back/shoulders (toward the neck) rather than hanging off the rump.
            Vec3 seatOffset = new Vec3(0.0F, 0.0F, 0.15F).yRot((float) Math.toRadians(-this.yBodyRot));
            living.setYBodyRot(this.yBodyRot);
            passenger.fallDistance = 0.0F;
            clampRotation(living, 105);
            // Track the body's vertical motion so the rider rises and falls WITH the gait instead of
            // staying static while the mob bobs. These two terms mirror exactly what RelicheirusModel
            // applies to the body: a walk-cycle bob plus the leg-solver ground adaptation.
            float limbSwing = this.walkAnimation.position();
            float limbSwingAmount = this.walkAnimation.speed();
            // 0.8 * 0.38 = 0.304: matches the ridden walk cadence used in RelicheirusModel so the
            // rider's bob tracks the body exactly.
            float bodyWalkBob = PMMath.walkValue(limbSwing, limbSwingAmount, 0.304F, -1.5F, 4F, false);
            float legMax = Math.max(
                    Math.max(this.legSolver.backLeft.getHeight(1.0F), this.legSolver.backRight.getHeight(1.0F)),
                    Math.max(this.legSolver.frontLeft.getHeight(1.0F), this.legSolver.frontRight.getHeight(1.0F))) * 0.8F;
            double bodyDrop = bodyWalkBob / 16.0D + legMax;
            moveFunction.accept(passenger, this.getX() + seatOffset.x, this.getY() + seatOffset.y + RIDER_BACK_HEIGHT - bodyDrop, this.getZ() + seatOffset.z);
        } else {
            super.positionRider(passenger, moveFunction);
        }
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity dismounter) {
        return new Vec3(this.getX(), this.getBoundingBox().minY, this.getZ());
    }

    public void push(double x, double y, double z) {
        if (this.getAnimation() != ANIMATION_EAT_TRILOCARIS) {
            super.push(x, y, z);
        }
    }

    public void tick() {
        super.tick();
        if (this.getAnimation() != ANIMATION_EAT_TREE) {
            this.yBodyRot = Mth.approachDegrees(this.yBodyRotO, yBodyRot, getHeadRotSpeed());
        }
        this.prevRaiseArmsAmount = raiseArmsAmount;
        this.legSolver.update(this, this.yBodyRot, this.getScale());
        AnimationHandler.INSTANCE.updateAnimations(this);
        if (shouldRaiseArms() && raiseArmsAmount < 5F) {
            raiseArmsAmount++;
        }
        if (!shouldRaiseArms() && raiseArmsAmount > 0F) {
            raiseArmsAmount--;
        }
        if (this.tickCount % 100 == 0 && this.getHealth() < this.getMaxHealth()) {
            this.heal(2);
        }
        if (!level().isClientSide) {
            if (isStillEnough() && random.nextInt(200) == 0 && this.getAnimation() == NO_ANIMATION && !this.isDancing() && !this.isInSittingPose() && !this.isVehicle()) {
                Animation idle;
                float rand = random.nextFloat();
                if (rand < 0.15F) {
                    idle = ANIMATION_SCRATCH_1;
                } else if (rand < 0.3F) {
                    idle = ANIMATION_SCRATCH_2;
                } else {
                    idle = ANIMATION_SHAKE;
                }
                this.setAnimation(idle);
            }
            boolean held = false;
            LivingEntity target = this.getTarget();
            if (target != null && target.distanceTo(this) < 10 && isFishable(target)) {
                if (this.getAnimation() == ANIMATION_EAT_TRILOCARIS) {
                    if (this.getAnimationTick() < 20) {
                        held = true;
                        this.setHeldMobId(target.getId());
                    } else if (this.getAnimationTick() <= 50) {
                        Vec3 heldPos = getHeldFishPos();
                        target.setPos(heldPos);
                        if (this.getAnimationTick() >= 45 && target.isAlive()) {
                            target.hurt(damageSources().mobAttack(this), 20);
                        }
                        held = true;
                        target.fallDistance = 0;
                    }
                }
            }
            if (!held && getHeldMobId() != -1) {
                this.setHeldMobId(-1);
            }
            if (this.getPushingTreesFor() > 0) {
                this.setPushingTreesFor(this.getPushingTreesFor() - 1);
                // The stew frenzy and carrying a rider are mutually exclusive (a fed Logger cannot be
                // mounted, and one fed while ridden throws its rider).
                if (this.isVehicle()) {
                    this.ejectPassengers();
                }
            }
        }
        // A ridden Logger keeps its arms down: no idle scratching/shaking and no tree-work swings.
        if (this.isVehicle() && (this.getAnimation() == ANIMATION_SCRATCH_1 || this.getAnimation() == ANIMATION_SCRATCH_2
                || this.getAnimation() == ANIMATION_SHAKE || this.getAnimation() == ANIMATION_PUSH_TREE
                || this.getAnimation() == ANIMATION_EAT_TREE)) {
            this.setAnimation(NO_ANIMATION);
        }
        if (this.getAnimation() == ANIMATION_SPEAK_1 && this.getAnimationTick() == 1 || this.getAnimation() == ANIMATION_SPEAK_2 && this.getAnimationTick() == 1) {
            actuallyPlayAmbientSound();
        }
    }

    public AABB getBoundingBoxForCulling() {
        return this.getBoundingBox().inflate(3, 3, 3);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob mob) {
        return PMEntityRegistry.RELICHEIRUS.get().create(level);
    }

    private Vec3 getHeldFishPos() {
        Vec3 fishUp = new Vec3(0, 0F, 1.5F);
        if (this.getAnimation() == ANIMATION_EAT_TRILOCARIS && getAnimationTick() >= 15F) {
            float anim1 = Math.min(getAnimationTick() - 15F, 15F) / 15F;
            float anim2 = Math.min(getAnimationTick(), 15F) / 15F;
            fishUp = fishUp.add(0, (this.getEyeHeight() + 1F) * anim1, anim2 * -1F + 1F);
        }
        Vec3 head = fishUp.xRot(-this.getXRot() * ((float) Math.PI / 180F)).yRot(-this.getYHeadRot() * ((float) Math.PI / 180F));
        return this.position().add(head);
    }

    private boolean isStillEnough() {
        return this.getDeltaMovement().horizontalDistance() < 0.05;
    }

    public boolean shouldRaiseArms() {
        return this.getAnimation() == ANIMATION_EAT_TREE || this.getAnimation() == ANIMATION_PUSH_TREE || this.getAnimation() == ANIMATION_SCRATCH_1 || this.getAnimation() == ANIMATION_SCRATCH_2 || this.getAnimation() == ANIMATION_MELEE_SLASH_1 || this.getAnimation() == ANIMATION_MELEE_SLASH_2;
    }

    public void setPeckY(int y) {
        this.entityData.set(PECK_Y, y);
    }


    public int getPeckY() {
        return this.entityData.get(PECK_Y);
    }

    public void setHeldMobId(int i) {
        this.entityData.set(HELD_MOB_ID, i);
    }

    public void travel(Vec3 vec3d) {
        if (this.getAnimation() == ANIMATION_EAT_TRILOCARIS || this.isDancing()) {
            vec3d = Vec3.ZERO;
        }
        super.travel(vec3d);
    }

    public int getHeldMobId() {
        return this.entityData.get(HELD_MOB_ID);
    }

    public Entity getHeldMob() {
        int id = getHeldMobId();
        return id == -1 ? null : level().getEntity(id);
    }

    /**
     * The Seething Stew frenzy timer survives saving/reloading (the original mod lost it on relog, which
     * also made it untestable headless: it is now plain entity NBT, so `/data merge entity <id>
     * {PushingTreesFor:1200}` triggers the exact tree-felling code path a stew feeding does).
     */
    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("PushingTreesFor", this.getPushingTreesFor());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setPushingTreesFor(compound.getInt("PushingTreesFor"));
    }

    public void setPushingTreesFor(int time) {
        this.entityData.set(PUSHING_TREES_FOR, time);
    }

    public int getPushingTreesFor() {
        return this.entityData.get(PUSHING_TREES_FOR);
    }

    public float getRaiseArmsAmount(float partialTick) {
        return (prevRaiseArmsAmount + (raiseArmsAmount - prevRaiseArmsAmount) * partialTick) * 0.2F;
    }

    public int getHeadRotSpeed() {
        return 5;
    }

    public void playAmbientSound() {
        if (this.getAnimation() == NO_ANIMATION && !level().isClientSide) {
            this.setAnimation(random.nextBoolean() ? ANIMATION_SPEAK_2 : ANIMATION_SPEAK_1);
        }
    }

    public void actuallyPlayAmbientSound() {
        SoundEvent soundevent = this.getAmbientSound();
        if (soundevent != null) {
            this.playSound(soundevent, this.getSoundVolume(), this.getVoicePitch());
        }
    }

    @Override
    public int getAnimationTick() {
        return animationTick;
    }

    @Override
    public void setAnimationTick(int tick) {
        animationTick = tick;
    }

    @Override
    public Animation getAnimation() {
        return currentAnimation;
    }

    @Override
    public void setAnimation(Animation animation) {
        currentAnimation = animation;
    }

    @Override
    public Animation[] getAnimations() {
        return new Animation[]{ANIMATION_SPEAK_1, ANIMATION_SPEAK_2, ANIMATION_EAT_TREE, ANIMATION_EAT_TRILOCARIS, ANIMATION_PUSH_TREE, ANIMATION_SCRATCH_1, ANIMATION_SCRATCH_2, ANIMATION_SHAKE, ANIMATION_MELEE_SLASH_1, ANIMATION_MELEE_SLASH_2};
    }

    public float getScale() {
        return this.isBaby() ? 0.25F : 1.0F;
    }

    public BlockPos getStandAtTreePos(BlockPos target) {
        Vec3 vec3 = Vec3.atCenterOf(target).subtract(this.position());
        float f = -((float) Mth.atan2(vec3.x, vec3.z)) * 180.0F / (float) Math.PI;
        BlockState state = level().getBlockState(target);
        Direction dir = Direction.fromYRot(f);
        if (state.is(PMBlockRegistry.PEWEN_BRANCH.get())) {
            dir = Direction.fromYRot(state.getValue(PewenBranchBlock.ROTATION) * 45);
        }
        if (level().getBlockState(target.below()).isAir()) {
            target = target.relative(dir);
        }
        return target.relative(dir.getOpposite(), 4).atY((int) this.getY());
    }

    public boolean lockTreePosition(BlockPos target) {
        Vec3 vec3 = Vec3.atCenterOf(target).subtract(this.position());
        float f = -((float) Mth.atan2(vec3.x, vec3.z)) * 180.0F / (float) Math.PI;
        BlockState state = level().getBlockState(target);
        Direction dir = Direction.fromYRot(f);
        if (state.is(PMBlockRegistry.PEWEN_BRANCH.get())) {
            dir = Direction.fromYRot(state.getValue(PewenBranchBlock.ROTATION) * 45);
        }
        float targetRot = Mth.approachDegrees(this.getYRot(), dir.toYRot(), 20);
        this.setYRot(targetRot);
        this.setYHeadRot(targetRot);
        this.yBodyRot = targetRot;
        if (level().getBlockState(target.below()).isAir()) {
            target = target.relative(dir);
        }
        Vec3 vec31 = Vec3.atCenterOf(target.relative(dir.getOpposite(), 2));
        Vec3 vec32 = vec31.subtract(this.position());
        if (vec32.length() > 1) {
            vec32 = vec32.normalize();
        }
        Vec3 delta = new Vec3(vec32.x * 0.1F, 0F, vec32.z * 0.1F);
        this.setDeltaMovement(this.getDeltaMovement().add(delta));
        return this.distanceToSqr(vec31.x, this.getY(), vec31.z) < 4.0D && Mth.degreesDifferenceAbs(this.getYRot(), dir.toYRot()) < 7;
    }

    public boolean isFood(ItemStack stack) {
        return stack.is(PMBlockRegistry.TREE_STAR.get().asItem());
    }

    @Override
    public BlockState createEggBlockState() {
        return PMBlockRegistry.RELICHEIRUS_EGG.get().defaultBlockState();
    }

    public float getStepHeight() {
        return 1.1F;
    }

    protected SoundEvent getAmbientSound() {
        return PMSoundRegistry.RELICHEIRUS_IDLE.get();
    }

    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return PMSoundRegistry.RELICHEIRUS_HURT.get();
    }

    protected SoundEvent getDeathSound() {
        return PMSoundRegistry.RELICHEIRUS_DEATH.get();
    }
}
