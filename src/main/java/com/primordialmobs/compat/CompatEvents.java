package com.primordialmobs.compat;

import com.github.alexmodguy.alexscaves.server.entity.living.DinosaurEntity;
import com.github.alexmodguy.alexscaves.server.entity.living.RelicheirusEntity;
import com.primordialmobs.PrimordialMobs;
import com.primordialmobs.compat.PMRecolorable;
import com.primordialmobs.server.entity.ai.AnimalFollowOwnerGoal;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Set;

/**
 * Registered ONLY when the full Alex's Caves mod is installed (this class references its
 * DinosaurEntity, so it must never be class-loaded standalone).
 *
 * Two jobs:
 *  - give Alex's Caves' five Primordial Caves dinosaurs the same 15% recolored-variant chance they have
 *    standalone;
 *  - graft on the AI goals that Primordial Mobs adds to the Grazer and the Logger (sit, follow, retaliate
 *    for the owner, and the Logger's broader fishing target).
 *
 * Goals are added on {@link EntityJoinLevelEvent} rather than by injecting into {@code registerGoals}: the
 * result is identical (the selector is per-instance and this fires once per instance) and it keeps one more
 * hook off Alex's Caves' bytecode.
 */
public class CompatEvents {

    /** The Logger's own walking speed; Alex's Caves' Relicheirus uses 0.20. */
    private static final double LOGGER_MOVEMENT_SPEED = 0.22D;

    private static final Set<ResourceLocation> PRIMORDIAL_DINOSAURS = Set.of(
            new ResourceLocation(PrimordialMobs.NAMESPACE, "grottoceratops"),
            new ResourceLocation(PrimordialMobs.NAMESPACE, "relicheirus"),
            new ResourceLocation(PrimordialMobs.NAMESPACE, "tremorsaurus"),
            new ResourceLocation(PrimordialMobs.NAMESPACE, "subterranodon"),
            new ResourceLocation(PrimordialMobs.NAMESPACE, "vallumraptor"));

    /**
     * Same 15% recoloured-variant chance the five dinosaurs have standalone.
     *
     * The flag is our OWN synched value (see {@link com.primordialmobs.compat.PMRecolorable}), not a slot in
     * Alex's Caves' AltSkin. The first compat build wrote AltSkin 3 here, which the Amber Curiosity and the
     * Tectonic Shard then overwrote — applying either to a recoloured dinosaur showed Alex's Caves' plain
     * retro/tectonic texture, and a second click reset AltSkin to 0 and destroyed the variant for good.
     * Keeping them independent makes the textures compose exactly as they do standalone.
     */
    @SubscribeEvent
    public void dinosaurVariantOnSpawn(MobSpawnEvent.FinalizeSpawn event) {
        if (event.getEntity() instanceof DinosaurEntity dinosaur && dinosaur instanceof PMRecolorable recolorable) {
            ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(dinosaur.getType());
            if (id != null && PRIMORDIAL_DINOSAURS.contains(id)
                    && !recolorable.pm_isRecolored() && dinosaur.getRandom().nextFloat() < 0.15F) {
                recolorable.pm_setRecolored(true);
            }
        }
    }

    @SubscribeEvent
    public void addPrimordialGoals(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide || !(event.getEntity() instanceof DinosaurEntity dinosaur)) {
            return;
        }
        if (dinosaur instanceof RelicheirusEntity relicheirus) {
            // The Logger hunts every small water animal in #alexscaves:relicheirus_fishes, not just the
            // Trilocaris. Alex's Caves' own Trilocaris-only goal stays at priority 2 and simply targets a
            // subset of this one, so both can coexist.
            relicheirus.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(
                    relicheirus, LivingEntity.class, 100, true, false, CompatDinosaurs::isFishable));
            // The Logger walks a touch faster than Alex's Caves' Relicheirus (0.22 vs 0.20). createAttributes
            // is static and was consumed when the entity type was registered, so the base value is set on the
            // instance instead; it persists in the entity's saved attribute map.
            AttributeInstance speed = relicheirus.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speed != null && speed.getBaseValue() != LOGGER_MOVEMENT_SPEED) {
                speed.setBaseValue(LOGGER_MOVEMENT_SPEED);
            }
            // Water speed parity with standalone comes from the getWaterSlowDown mixin (RelicheirusEntityMixin),
            // which reads the same calibrated WATER_SLOWDOWN constant.
        }
        if (CompatDinosaurs.isTameableByUs(dinosaur)) {
            addTameableGoals(dinosaur);
        }
    }

    /**
     * Taming the Grazer and the Logger with a Tree Star, 1 chance in 3, hearts on success and smoke on
     * failure — the same deal as standalone, and the same one the Roarer, Drifter and Stealer already have in
     * Alex's Caves with their own foods.
     *
     * Done from the interact event rather than by injecting into {@code mobInteract} so it takes precedence
     * over Alex's Caves' whole interaction chain: the Tree Star is also this mob's breeding food, so letting
     * the vanilla branch run first would put an untamed adult into love mode instead of taming it.
     */
    @SubscribeEvent
    public void tamePrimordialDinosaur(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof DinosaurEntity dinosaur) || !dinosaur.isAlive()) {
            return;
        }
        if (!CompatDinosaurs.isTameableByUs(dinosaur) || dinosaur.isTame() || dinosaur.isBaby()) {
            return;
        }
        Item treeStar = CompatDinosaurs.treeStar();
        ItemStack stack = event.getItemStack();
        if (treeStar == null || !stack.is(treeStar)) {
            return;
        }
        Player player = event.getEntity();
        if (dinosaur.level() instanceof ServerLevel) {
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            if (dinosaur.getRandom().nextInt(3) == 0) {
                dinosaur.tame(player);
                dinosaur.setCommand(1);
                dinosaur.setOrderedToSit(true);
                dinosaur.level().broadcastEntityEvent(dinosaur, (byte) 7);
            } else {
                dinosaur.level().broadcastEntityEvent(dinosaur, (byte) 6);
            }
        }
        player.swing(event.getHand());
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(dinosaur.level().isClientSide));
    }

    /**
     * The Grazer and the Logger are wild in Alex's Caves; Primordial Mobs tames them. Alex's Caves already
     * ships the whole tamed-dinosaur pipeline in DinosaurEntity (command cycle, mounting, the
     * entity.alexscaves.all.command_N messages) — it is simply never reachable because its canOwnerMount and
     * canOwnerCommand return false for these two. Our mixin opens that gate; these are the goals that make a
     * tamed one actually sit, follow and defend its owner.
     */
    private static void addTameableGoals(DinosaurEntity dinosaur) {
        Mob mob = dinosaur;
        mob.goalSelector.addGoal(1, new SitWhenOrderedToGoal(dinosaur));
        mob.goalSelector.addGoal(3, new AnimalFollowOwnerGoal(dinosaur, 1.2D, 6.0F, 3.0F, false) {
            @Override
            public boolean shouldFollow() {
                return dinosaur.getCommand() == 2;
            }
        });
        mob.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(dinosaur));
        mob.targetSelector.addGoal(2, new OwnerHurtTargetGoal(dinosaur));
    }
}
