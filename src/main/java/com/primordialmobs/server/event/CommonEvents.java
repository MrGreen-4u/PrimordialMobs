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

    /**
     * Bonking: killing a mob with an enchanted Primitive Club has a 1-in-3 chance to drop its head, by
     * running the mob's custom death loot against a synthetic charged-creeper damage source (the only
     * thing vanilla accepts as a head-dropping kill). Equipment drop chances are zeroed for the duration
     * so the fake kill cannot also duplicate the mob's gear.
     */
    @SubscribeEvent
    public void livingDie(net.minecraftforge.event.entity.living.LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide || !(event.getEntity() instanceof Mob mob)) {
            return;
        }
        if (event.getSource() == null || !(event.getSource().getDirectEntity() instanceof LivingEntity directSource)) {
            return;
        }
        net.minecraft.world.item.ItemStack held = directSource.getItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND);
        if (!held.is(com.primordialmobs.server.item.PMItemRegistry.PRIMITIVE_CLUB.get())
                || held.getEnchantmentLevel(com.primordialmobs.server.enchantment.PMEnchantmentRegistry.BONKING.get()) <= 0
                || mob.level().random.nextFloat() >= 0.33F) {
            return;
        }
        net.minecraft.world.entity.monster.Creeper fakeCreeperForSkullDrop = net.minecraft.world.entity.EntityType.CREEPER.create(mob.level());
        if (fakeCreeperForSkullDrop == null) {
            return;
        }
        if (mob.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            net.minecraft.world.entity.LightningBolt fakeThunder = net.minecraft.world.entity.EntityType.LIGHTNING_BOLT.create(serverLevel);
            if (fakeThunder != null) {
                fakeThunder.setVisualOnly(true);
                fakeCreeperForSkullDrop.thunderHit(serverLevel, fakeThunder);
            }
        }
        net.minecraft.world.damagesource.DamageSource fakeCreeperDamage = mob.level().damageSources().mobAttack(fakeCreeperForSkullDrop);
        java.util.HashMap<net.minecraft.world.entity.EquipmentSlot, Float> prevLootDropChances = new java.util.HashMap<>();
        com.primordialmobs.server.entity.util.EntityDropChanceAccessor dropChanceAccessor = (com.primordialmobs.server.entity.util.EntityDropChanceAccessor) mob;
        for (net.minecraft.world.entity.EquipmentSlot slot : net.minecraft.world.entity.EquipmentSlot.values()) {
            prevLootDropChances.put(slot, dropChanceAccessor.pm_getEquipmentDropChance(slot));
            dropChanceAccessor.pm_setDropChance(slot, 0.0F);
        }
        dropChanceAccessor.pm_dropCustomDeathLoot(fakeCreeperDamage, 0, false);
        for (net.minecraft.world.entity.EquipmentSlot slot : net.minecraft.world.entity.EquipmentSlot.values()) {
            dropChanceAccessor.pm_setDropChance(slot, prevLootDropChances.get(slot));
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
