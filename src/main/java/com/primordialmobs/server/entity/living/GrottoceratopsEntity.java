package com.primordialmobs.server.entity.living;

import com.primordialmobs.server.block.PMBlockRegistry;
import com.primordialmobs.server.entity.PMEntityRegistry;
import com.primordialmobs.server.entity.ai.*;
import com.primordialmobs.server.item.PMItemRegistry;
import com.primordialmobs.server.misc.PMMath;
import com.primordialmobs.server.misc.PMSoundRegistry;
import com.github.alexthe666.citadel.animation.Animation;
import com.github.alexthe666.citadel.animation.AnimationHandler;
import com.github.alexthe666.citadel.animation.IAnimatedEntity;
import com.github.alexthe666.citadel.animation.LegSolverQuadruped;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class GrottoceratopsEntity extends DinosaurEntity implements IAnimatedEntity {

    private static final EntityDataAccessor<Float> TAIL_SWING_ROT = SynchedEntityData.defineId(GrottoceratopsEntity.class, EntityDataSerializers.FLOAT);
    public LegSolverQuadruped legSolver = new LegSolverQuadruped(0.0F, 1.1F, 1.15F, 1.15F, 1);
    private Animation currentAnimation;
    private int animationTick;
    public static final Animation ANIMATION_SPEAK_1 = Animation.create(15);
    public static final Animation ANIMATION_SPEAK_2 = Animation.create(20);
    public static final Animation ANIMATION_CHEW_FROM_GROUND = Animation.create(60);
    public static final Animation ANIMATION_CHEW = Animation.create(40);
    public static final Animation ANIMATION_MELEE_RAM = Animation.create(20);
    public static final Animation ANIMATION_MELEE_TAIL_1 = Animation.create(20);
    public static final Animation ANIMATION_MELEE_TAIL_2 = Animation.create(20);
    private float prevTailSwingRot;
    private int resetAttackerCooldown = 0;

    public GrottoceratopsEntity(EntityType<? extends Animal> type, Level level) {
        super(type, level);
    }

    /** Four minutes of Haste I: what a Grazer gives back for a Serene Salad. */
    private static final int SALAD_HASTE_TICKS = 4 * 60 * 20;

    /**
     * The Grazer's own reaction to a prehistoric mixture, in the same slot where a Logger reacts to the
     * Primordial Soup and a Roarer or a Stealer react to the Serene Salad: hand a **Serene Salad** to a
     * Grazer and the digger of the family passes its appetite for stone on to you — Haste I for four
     * minutes. Returning true means the item's generic branch (mob effects, curing Stunned) is skipped;
     * the healing and the returned bowl still happen in PrehistoricMixtureItem.
     */
    @Override
    public boolean onFeedMixture(ItemStack itemStack, Player player) {
        if (itemStack.is(PMItemRegistry.SERENE_SALAD.get())) {
            if (!this.level().isClientSide) {
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, SALAD_HASTE_TICKS, 0));
                this.level().broadcastEntityEvent(this, (byte) 7);
            }
            return true;
        }
        return false;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.ATTACK_DAMAGE, 10.0D).add(Attributes.MOVEMENT_SPEED, 0.2D).add(Attributes.KNOCKBACK_RESISTANCE, 0.9D).add(Attributes.FOLLOW_RANGE, 32.0D).add(Attributes.MAX_HEALTH, 50.0D).add(Attributes.ARMOR, 8.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(TAIL_SWING_ROT, 0F);
    }

    protected PathNavigation createNavigation(Level level) {
        return new AdvancedPathNavigateNoTeleport(this, level);
    }

    protected void playStepSound(BlockPos pos, BlockState state) {
        if (!this.isBaby()) {
            this.playSound(PMSoundRegistry.GROTTOCERATOPS_STEP.get(), 0.7F, 0.85F);
        }
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(2, new GrottoceratopsMeleeGoal(this));
        this.goalSelector.addGoal(3, new AnimalFollowOwnerGoal(this, 1.2D, 6.0F, 3.0F, false) {
            @Override
            public boolean shouldFollow() {
                return GrottoceratopsEntity.this.getCommand() == 2;
            }
        });
        this.goalSelector.addGoal(4, new AnimalBreedEggsGoal(this, 1));
        this.goalSelector.addGoal(5, new AnimalLayEggGoal(this, 100, 1));
        this.goalSelector.addGoal(6, new TemptGoal(this, 1.1D, Ingredient.of(PMBlockRegistry.TREE_STAR.get()), false));
        this.goalSelector.addGoal(7, new GrottoceratopsEatPlantsGoal(this, 16));
        this.goalSelector.addGoal(8, new RandomStrollGoal(this, 1.0D, 45));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, (new HurtByTargetGoal(this, GrottoceratopsEntity.class)).setAlertOthers());
    }

    public boolean hurt(DamageSource damageSource, float f) {
        if (damageSource.getDirectEntity() instanceof VallumraptorEntity) {
            f *= 0.75F;
        }
        return super.hurt(damageSource, f);
    }

    public void tick() {
        super.tick();
        float tailSwing = getTailSwingRot();
        this.prevTailSwingRot = tailSwing;
        if (this.getAnimation() == ANIMATION_MELEE_TAIL_1 || this.getAnimation() == ANIMATION_MELEE_TAIL_2) {
            float start = this.getAnimation() == ANIMATION_MELEE_TAIL_1 ? 30 : -30;
            float end = this.getAnimation() == ANIMATION_MELEE_TAIL_1 ? -180 : 180;
            if (this.getAnimationTick() <= 7) {
                this.setTailSwingRot(Mth.approachDegrees(tailSwing, start, 5));
            } else {
                this.setTailSwingRot(Mth.approachDegrees(tailSwing, end, 25));
            }
            this.walkAnimation.setSpeed(1);
        } else {
            if (Math.abs(tailSwing) > 0.0F) {
                this.setTailSwingRot(Mth.approachDegrees(tailSwing, 0, 20));
            }
            this.yBodyRot = Mth.approachDegrees(this.yBodyRotO, yBodyRot, getHeadRotSpeed());
        }
        if (this.getAnimation() == ANIMATION_CHEW || this.getAnimation() == ANIMATION_CHEW_FROM_GROUND) {
            if (this.getAnimationTick() > this.getAnimation().getDuration() - 1) {
                this.heal(5);
            }
        }
        if (this.getAnimation() == ANIMATION_CHEW && this.getAnimationTick() == 2 || this.getAnimation() == ANIMATION_CHEW_FROM_GROUND && this.getAnimationTick() == 10) {
            this.playSound(PMSoundRegistry.GROTTOCERATOPS_GRAZE.get());
        }
        if (this.tickCount % 100 == 0 && this.getHealth() < this.getMaxHealth()) {
            this.heal(2);
        }
        if (resetAttackerCooldown > 0) {
            resetAttackerCooldown--;
        } else if (!level().isClientSide && !this.isBaby() && (this.getLastHurtByMob() == null || !this.getLastHurtByMob().isAlive())) {
            this.setTarget(this.getLastHurtByMob());
            resetAttackerCooldown = 600;
        }
        if (this.getAnimation() == ANIMATION_SPEAK_1 && this.getAnimationTick() == 5 || this.getAnimation() == ANIMATION_SPEAK_2 && this.getAnimationTick() == 2) {
            actuallyPlayAmbientSound();
        }
        this.legSolver.update(this, this.yBodyRot + getTailSwingRot(), this.getScale());
        AnimationHandler.INSTANCE.updateAnimations(this);
    }

    private float getTailSwingRot() {
        return entityData.get(TAIL_SWING_ROT);
    }

    public float getTailSwingRot(float f) {
        return prevTailSwingRot + (getTailSwingRot() - prevTailSwingRot) * f;
    }

    public void setTailSwingRot(float rot) {
        entityData.set(TAIL_SWING_ROT, rot);
    }

    @Override
    public BlockState createEggBlockState() {
        return PMBlockRegistry.GROTTOCERATOPS_EGG.get().defaultBlockState();
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob mob) {
        return PMEntityRegistry.GROTTOCERATOPS.get().create(level);
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
        return new Animation[]{ANIMATION_SPEAK_1, ANIMATION_SPEAK_2, ANIMATION_CHEW_FROM_GROUND, ANIMATION_CHEW, ANIMATION_MELEE_RAM, ANIMATION_MELEE_TAIL_1, ANIMATION_MELEE_TAIL_2};
    }

    public void playAmbientSound() {
        if (this.getAnimation() == NO_ANIMATION && !level().isClientSide) {
            this.setAnimation(random.nextBoolean() ? ANIMATION_SPEAK_2 : ANIMATION_SPEAK_1);
        }
    }

    public void actuallyPlayAmbientSound() {
        SoundEvent soundevent = this.getAmbientSound();
        float volume = this.getSoundVolume();
        if (this.getAnimation() == ANIMATION_SPEAK_2) {
            soundevent = PMSoundRegistry.GROTTOCERATOPS_CALL.get();
            volume += 1.0F;
        }
        if (soundevent != null) {
            this.playSound(soundevent, volume, this.getVoicePitch());
        }
    }

    public boolean isFood(ItemStack stack) {
        return stack.is(PMBlockRegistry.TREE_STAR.get().asItem());
    }

    public boolean isTamingFood(ItemStack stack) {
        return stack.is(PMBlockRegistry.TREE_STAR.get().asItem());
    }

    @Override
    public float getSittingDimensionScale() {
        // Couched with the chest on the ground, so the standing-height box should drop noticeably.
        return 0.78F;
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
        return super.mobInteract(player, hand);
    }

    public boolean canOwnerMount(Player player) {
        return !this.isBaby();
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
        return (float) this.getAttributeValue(Attributes.MOVEMENT_SPEED);
    }

    @Override
    public void positionRider(Entity passenger, MoveFunction moveFunction) {
        if (this.isPassengerOfSameVehicle(passenger) && passenger instanceof LivingEntity living && !this.touchingUnloadedChunk()) {
            // Seat rests on the broad back, just behind the frill.
            Vec3 seatOffset = new Vec3(0.0F, 0.0F, 0.35F).yRot((float) Math.toRadians(-this.yBodyRot));
            living.setYBodyRot(this.yBodyRot);
            passenger.fallDistance = 0.0F;
            clampRotation(living, 105);
            // Track the body's vertical motion so the rider rises/falls WITH the gait. These terms
            // mirror exactly what GrottoceratopsModel applies to the body: a walk-cycle bob plus the
            // leg-solver ground adaptation (max over all four legs).
            float limbSwing = this.walkAnimation.position();
            float limbSwingAmount = this.walkAnimation.speed();
            float bodyBob = PMMath.walkValue(limbSwing, limbSwingAmount, 0.5F * 1.5F, 0.5F, 2.4F, true);
            float legMax = Math.max(
                    Math.max(this.legSolver.backLeft.getHeight(1.0F), this.legSolver.backRight.getHeight(1.0F)),
                    Math.max(this.legSolver.frontLeft.getHeight(1.0F), this.legSolver.frontRight.getHeight(1.0F))) * 0.8F;
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

    public void calculateEntityAnimation(boolean flying) {
        float f1 = (float) Mth.length(this.getX() - this.xo, flying ? this.getY() - this.yo : 0, this.getZ() - this.zo);
        float f2 = Math.min(f1 * 8.0F, 1.0F);
        this.walkAnimation.update(f2, 0.4F);
    }


    @Override
    public void setInLove(@javax.annotation.Nullable Player player) {
        super.setInLove(player);
        if (this.getAnimation() == null || this.getAnimation() == NO_ANIMATION) {
            this.setAnimation(ANIMATION_CHEW);
        }
    }

    protected SoundEvent getAmbientSound() {
        return PMSoundRegistry.GROTTOCERATOPS_IDLE.get();
    }

    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return PMSoundRegistry.GROTTOCERATOPS_HURT.get();
    }

    protected SoundEvent getDeathSound() {
        return PMSoundRegistry.GROTTOCERATOPS_DEATH.get();
    }

    public float getStepHeight() {
        return 1.1F;
    }
}
