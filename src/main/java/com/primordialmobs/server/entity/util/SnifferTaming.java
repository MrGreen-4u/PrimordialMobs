package com.primordialmobs.server.entity.util;

import com.primordialmobs.PrimordialMobs;
import com.primordialmobs.server.misc.PMTagRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Id-based helpers for the tamable Sniffer. Everything here resolves "alexscaves:" content through the
 * registries at use time, so it works both standalone (our registrations) and with the full Alex's Caves
 * mod installed (its registrations).
 */
public class SnifferTaming {

    public static final ResourceLocation PRIMORDIAL_CAVES_BIOME = new ResourceLocation(PrimordialMobs.NAMESPACE, "primordial_caves");
    /**
     * The sit pose reuses the vanilla SNIFFER_DIG animation (8s), but only its first ~2 seconds are the
     * lie-down (body drops to y=-7, all six legs tuck). At 2.0s the body rotation is back to 0 and the
     * floor-sniffing bob has not started yet (first bob keyframe is at 2.5s), so the client render freezes
     * the animation here to hold a clean settled pose instead of playing the 6-second ground-sniffing loop.
     */
    public static final long SIT_ANIM_FREEZE_MS = 2000L;
    private static final ResourceLocation TREE_STAR_ID = new ResourceLocation(PrimordialMobs.NAMESPACE, "tree_star");
    private static final ResourceLocation AMBER_CURIOSITY_ID = new ResourceLocation(PrimordialMobs.NAMESPACE, "amber_curiosity");
    private static final ResourceLocation TECTONIC_SHARD_ID = new ResourceLocation(PrimordialMobs.NAMESPACE, "tectonic_shard");
    private static final ResourceLocation AMBER_SOUND_ID = new ResourceLocation(PrimordialMobs.NAMESPACE, "amber_monolith_summon");
    private static final ResourceLocation TECTONIC_SOUND_ID = new ResourceLocation(PrimordialMobs.NAMESPACE, "tectonic_shard_transform");

    public static boolean isTamingFood(ItemStack stack) {
        Item treeStar = ForgeRegistries.ITEMS.getValue(TREE_STAR_ID);
        return treeStar != null && stack.is(treeStar);
    }

    public static boolean isAmberCuriosity(ItemStack stack) {
        Item item = ForgeRegistries.ITEMS.getValue(AMBER_CURIOSITY_ID);
        return item != null && stack.is(item);
    }

    public static boolean isTectonicShard(ItemStack stack) {
        Item item = ForgeRegistries.ITEMS.getValue(TECTONIC_SHARD_ID);
        return item != null && stack.is(item);
    }

    public static SoundEvent getSkinSwapSound(boolean tectonic) {
        return ForgeRegistries.SOUND_EVENTS.getValue(tectonic ? TECTONIC_SOUND_ID : AMBER_SOUND_ID);
    }

    private static final ResourceLocation SERENE_SALAD_ID = new ResourceLocation(PrimordialMobs.NAMESPACE, "serene_salad");
    private static final ResourceLocation PRIMORDIAL_SOUP_ID = new ResourceLocation(PrimordialMobs.NAMESPACE, "primordial_soup");
    private static final ResourceLocation SEETHING_STEW_ID = new ResourceLocation(PrimordialMobs.NAMESPACE, "seething_stew");
    private static final ResourceLocation STUNNED_EFFECT_ID = new ResourceLocation(PrimordialMobs.NAMESPACE, "stunned");

    /**
     * The three prehistoric mixtures, and what each one does to a Sniffer. The dinosaurs each react to one
     * of them (a Primordial Soup sends a Logger to push trees over, a Serene Salad calms a stunned Roarer or
     * a relaxed Stealer); the Sniffer had no reaction at all, so each mixture now drives the one thing a
     * Sniffer is for — digging:
     *   SERENE_SALAD    calm: clears Stunned and sends a tame Sniffer to lie down and rest.
     *   PRIMORDIAL_SOUP back to work: erases the sniff cooldown (vanilla makes a Sniffer wait 10000-15000
     *                   ticks between digs), stands it up and lets it start scenting immediately.
     *   SEETHING_STEW   work the same ground again: the soup's effect plus erasing the explored-positions
     *                   memory, so it stops refusing to dig where it has already been.
     * All three still heal the Sniffer and apply their own food effects (see SnifferEvents).
     */
    public enum Mixture {
        NONE, SERENE_SALAD, PRIMORDIAL_SOUP, SEETHING_STEW
    }

    private static boolean is(ItemStack stack, ResourceLocation id) {
        Item item = ForgeRegistries.ITEMS.getValue(id);
        return item != null && stack.is(item);
    }

    public static Mixture mixtureOf(ItemStack stack) {
        if (is(stack, SERENE_SALAD_ID)) {
            return Mixture.SERENE_SALAD;
        }
        if (is(stack, PRIMORDIAL_SOUP_ID)) {
            return Mixture.PRIMORDIAL_SOUP;
        }
        if (is(stack, SEETHING_STEW_ID)) {
            return Mixture.SEETHING_STEW;
        }
        return Mixture.NONE;
    }

    /**
     * Applies a mixture's Sniffer-specific effect (server side). Returns the particle/feedback flag: true if
     * the sniffer visibly reacted.
     */
    public static boolean applyMixture(Sniffer sniffer, Mixture mixture) {
        SnifferSkinHolder holder = sniffer instanceof SnifferSkinHolder h ? h : null;
        switch (mixture) {
            case SERENE_SALAD -> {
                // the salad is the calming mixture everywhere in the mod: it clears Stunned
                MobEffect stunned = ForgeRegistries.MOB_EFFECTS.getValue(STUNNED_EFFECT_ID);
                if (stunned != null) {
                    sniffer.removeEffect(stunned);
                }
                if (holder != null && holder.ac_isTame()) {
                    holder.ac_setCommand(1);
                }
                return true;
            }
            case SEETHING_STEW -> {
                sniffer.getBrain().eraseMemory(MemoryModuleType.SNIFFER_EXPLORED_POSITIONS);
                wakeUpToDig(sniffer, holder);
                return true;
            }
            case PRIMORDIAL_SOUP -> {
                wakeUpToDig(sniffer, holder);
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    private static void wakeUpToDig(Sniffer sniffer, SnifferSkinHolder holder) {
        if (holder != null && holder.ac_isTame() && holder.ac_getCommand() == 1) {
            holder.ac_setCommand(0);                 // stand up: a sitting sniffer never sniffs
        }
        sniffer.getBrain().eraseMemory(MemoryModuleType.SNIFF_COOLDOWN);
        sniffer.getBrain().eraseMemory(MemoryModuleType.SNIFFER_DIGGING);
        sniffer.getBrain().eraseMemory(MemoryModuleType.SNIFFER_SNIFFING_TARGET);
    }

    /**
     * Same surface rule the dinosaurs use, so naturally-spawning Sniffers stand on the Primordial Caves
     * floor like the Grottoceratops and the Relicheirus do. Only the Primordial Caves biome modifier ever
     * adds Sniffer spawn entries, so this predicate does not affect vanilla worlds.
     */
    public static boolean checkPrimordialSnifferSpawnRules(EntityType<? extends Animal> type, LevelAccessor levelAccessor, MobSpawnType mobType, BlockPos pos, RandomSource randomSource) {
        return levelAccessor.getBlockState(pos.below()).is(PMTagRegistry.DINOSAURS_SPAWNABLE_ON) && levelAccessor.getFluidState(pos).isEmpty() && levelAccessor.getFluidState(pos.below()).isEmpty();
    }

    public static void registerSpawnPlacements(SpawnPlacementRegisterEvent event) {
        // Vanilla 1.20.1 registers no placement data for the Sniffer (it is egg-only), so REPLACE simply
        // installs ours.
        event.register(EntityType.SNIFFER, SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SnifferTaming::checkPrimordialSnifferSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
    }

    /**
     * In the Primordial Caves the Sniffer is a plain passive mob: no scenting/sniffing/searching and no
     * digging plants out of the ground.
     */
    public static boolean isInPrimordialCaves(Sniffer sniffer) {
        return sniffer.level().getBiome(sniffer.blockPosition()).is(PRIMORDIAL_CAVES_BIOME);
    }
}
