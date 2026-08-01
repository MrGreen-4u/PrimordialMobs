package com.primordialmobs.server.event;

import com.primordialmobs.server.entity.living.DinosaurEntity;
import com.primordialmobs.server.entity.living.VallumraptorEntity;
import com.primordialmobs.server.entity.util.FlyingMount;
import com.primordialmobs.server.potion.PMEffectRegistry;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CommonEvents {

    /**
     * The 15% recoloured-variant roll, standalone mode. Rolled on the dinosaur's FIRST join to the world
     * rather than in finalizeSpawn, because several natural paths never call finalizeSpawn (hatching from
     * egg blocks, feature-driven spawning); every path goes through EntityJoinLevelEvent. The one-shot
     * flag lives in ForgeData so dimension changes and reloads never re-roll.
     */
    @SubscribeEvent
    public void dinosaurVariantOnFirstJoin(net.minecraftforge.event.entity.EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide && !event.loadedFromDisk() && event.getEntity() instanceof DinosaurEntity dinosaur
                && !dinosaur.getPersistentData().getBoolean("PMVariantRolled")) {
            dinosaur.getPersistentData().putBoolean("PMVariantRolled", true);
            if (!dinosaur.isRecolored() && dinosaur.getRandom().nextFloat() < 0.15F) {
                dinosaur.setRecolored(true);
            }
        }
    }

    @SubscribeEvent
    public void livingFindTarget(LivingChangeTargetEvent event) {
        if (event.getEntity() instanceof Mob mob && event.getNewTarget() instanceof VallumraptorEntity vallumraptor && vallumraptor.getHideFor() > 0) {
            mob.setTarget(null);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void livingHurt(LivingDamageEvent event) {
        if (event.getEntity().isPassenger() && event.getEntity() instanceof FlyingMount && (event.getSource().is(DamageTypes.IN_WALL) || event.getSource().is(DamageTypes.FALL) || event.getSource().is(DamageTypes.FLY_INTO_WALL))) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void livingAttack(LivingAttackEvent event) {
        if (event.getSource() != null && event.getSource().getDirectEntity() instanceof LivingEntity directSource && directSource.hasEffect(PMEffectRegistry.STUNNED.get())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void playerAttack(AttackEntityEvent event) {
        if (event.getTarget() instanceof DinosaurEntity && event.getEntity().isPassengerOfSameVehicle(event.getTarget())) {
            event.setCanceled(true);
        }
    }




    @SubscribeEvent
    public void livingTick(LivingEvent.LivingTickEvent event) {
        if (!event.getEntity().level().isClientSide && event.getEntity() instanceof Mob mob && mob.getTarget() instanceof VallumraptorEntity vallumraptor && vallumraptor.getHideFor() > 0) {
            mob.setTarget(null);
        }
    }
}
