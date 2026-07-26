package com.primordialmobs.server.event;

import com.primordialmobs.server.entity.util.SnifferSkinHolder;
import com.primordialmobs.server.entity.util.SnifferTaming;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Everything the mod does to the vanilla Sniffer: alternate skins, the 15% recolored-variant chance and
 * the Rammer/Logger-style taming (tree star food, sit/follow/wander commands, never rideable). All item
 * and sound references resolve by id so the same code runs standalone and alongside the full Alex's Caves
 * mod.
 */
public class SnifferEvents {

    @SubscribeEvent
    public void snifferInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof Sniffer sniffer) || !(sniffer instanceof SnifferSkinHolder skinHolder) || !sniffer.isAlive()) {
            return;
        }
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();
        // 1) Amber curiosity / tectonic shard: swap the alternate skin (existing feature).
        int skinFromItem = SnifferTaming.isAmberCuriosity(stack) ? 1 : SnifferTaming.isTectonicShard(stack) ? 2 : 0;
        if (skinFromItem > 0) {
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            SoundEvent sound = SnifferTaming.getSkinSwapSound(skinFromItem == 2);
            if (sound != null) {
                sniffer.playSound(sound);
            }
            if (!sniffer.level().isClientSide) {
                skinHolder.ac_setSkinType(skinFromItem == skinHolder.ac_getSkinType() ? 0 : skinFromItem);
            }
            player.swing(event.getHand());
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            return;
        }
        // 2) Taming with a tree star, mirroring the Rammer/Logger (1-in-3 chance, hearts/smoke feedback).
        if (!skinHolder.ac_isTame() && !sniffer.isBaby() && SnifferTaming.isTamingFood(stack)) {
            if (sniffer.level() instanceof ServerLevel serverLevel) {
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                sniffer.playSound(sniffer.getEatingSound(stack));
                if (sniffer.getRandom().nextInt(3) == 0) {
                    skinHolder.ac_setOwnerUUID(player.getUUID());
                    skinHolder.ac_setCommand(1);
                    spawnTamingParticles(serverLevel, sniffer, ParticleTypes.HEART);
                } else {
                    spawnTamingParticles(serverLevel, sniffer, ParticleTypes.SMOKE);
                }
            }
            player.swing(event.getHand());
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.sidedSuccess(sniffer.level().isClientSide));
            return;
        }
        // 3) The prehistoric mixtures (serene salad / primordial soup / seething stew) work on a Sniffer too.
        // Handled here rather than in PrehistoricMixtureItem so it also applies with the full Alex's Caves
        // installed, where the item classes are its own; the items are matched by id and the healing, the
        // food effects, the particles and the returned bowl mirror what that item does for any other mob.
        SnifferTaming.Mixture mixture = SnifferTaming.mixtureOf(stack);
        if (mixture != SnifferTaming.Mixture.NONE && !player.isShiftKeyDown()) {
            if (sniffer.level() instanceof ServerLevel serverLevel) {
                FoodProperties food = stack.getFoodProperties(sniffer);
                if (food != null) {
                    sniffer.heal(food.getNutrition());
                    for (Pair<MobEffectInstance, Float> effect : food.getEffects()) {
                        sniffer.addEffect(new MobEffectInstance(effect.getFirst()));
                    }
                }
                SnifferTaming.applyMixture(sniffer, mixture);
                for (int i = 0; i < 4 + sniffer.getRandom().nextInt(3); i++) {
                    serverLevel.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, stack),
                            sniffer.getRandomX(0.8F), sniffer.getRandomY(), sniffer.getRandomZ(0.8F),
                            0, 0.0D, 0.0D, 0.0D, 0.0D);
                }
                sniffer.playSound(sniffer.getEatingSound(stack));
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                    if (!player.addItem(new ItemStack(Items.BOWL))) {
                        player.drop(new ItemStack(Items.BOWL), true);
                    }
                }
            }
            player.swing(event.getHand());
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.sidedSuccess(sniffer.level().isClientSide));
            return;
        }
        // 4) Shift-click by the owner cycles wander -> sit -> follow, like the dinosaurs.
        if (skinHolder.ac_isTame() && player.getUUID().equals(skinHolder.ac_getOwnerUUID()) && player.isShiftKeyDown() && !sniffer.isFood(stack)) {
            if (!sniffer.level().isClientSide) {
                int command = skinHolder.ac_getCommand() + 1;
                if (command >= 3) {
                    command = 0;
                }
                skinHolder.ac_setCommand(command);
                player.displayClientMessage(Component.translatable("entity.alexscaves.all.command_" + command, sniffer.getName()), true);
            }
            player.swing(event.getHand());
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }

    private static void spawnTamingParticles(ServerLevel level, Sniffer sniffer, ParticleOptions particle) {
        for (int i = 0; i < 7; i++) {
            level.sendParticles(particle,
                    sniffer.getRandomX(1.0D), sniffer.getRandomY() + 0.5D, sniffer.getRandomZ(1.0D),
                    1, 0.0D, 0.0D, 0.0D, 0.02D);
        }
    }

    @SubscribeEvent
    public void snifferSkinOnSpawn(MobSpawnEvent.FinalizeSpawn event) {
        if (event.getEntity() instanceof Sniffer sniffer && sniffer instanceof SnifferSkinHolder skinHolder) {
            if (!skinHolder.ac_isRecolored() && sniffer.getRandom().nextFloat() < 0.15F) {
                skinHolder.ac_setRecolored(true);
            }
        }
    }

    @SubscribeEvent
    public void snifferRestore(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide && event.getEntity() instanceof Sniffer sniffer && sniffer instanceof SnifferSkinHolder skinHolder) {
            if (sniffer.getPersistentData().contains("ACSkinType")) {
                skinHolder.ac_setSkinType(sniffer.getPersistentData().getInt("ACSkinType"));
            }
            if (sniffer.getPersistentData().contains("ACRecolored")) {
                skinHolder.ac_setRecolored(sniffer.getPersistentData().getBoolean("ACRecolored"));
            }
            if (sniffer.getPersistentData().hasUUID("ACSnifferOwner")) {
                skinHolder.ac_setOwnerUUID(sniffer.getPersistentData().getUUID("ACSnifferOwner"));
            }
            if (sniffer.getPersistentData().contains("ACSnifferCommand")) {
                skinHolder.ac_setCommand(sniffer.getPersistentData().getInt("ACSnifferCommand"));
            }
        }
    }
}
