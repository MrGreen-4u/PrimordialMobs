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
    /** How long a Seething Stew keeps a Sniffer enraged: one minute. */
    public static final int RAGE_TICKS = 1200;
    /**
     * The sit pose reuses the vanilla SNIFFER_DIG animation (8s), but only its first ~2 seconds are the
     * lie-down (body drops to y=-7, all six legs tuck). At 2.0s the body rotation is back to 0 and the
     * floor-sniffing bob has not started yet (first bob keyframe is at 2.5s), so the client render freezes
     * the animation here to hold a clean settled pose instead of playing the 6-second ground-sniffing loop.
     */
    public static final long SIT_ANIM_FREEZE_MS = 2000L;
    private static final ResourceLocation AMBER_CURIOSITY_ID = new ResourceLocation(PrimordialMobs.NAMESPACE, "amber_curiosity");
    private static final ResourceLocation TECTONIC_SHARD_ID = new ResourceLocation(PrimordialMobs.NAMESPACE, "tectonic_shard");
    private static final ResourceLocation AMBER_SOUND_ID = new ResourceLocation(PrimordialMobs.NAMESPACE, "amber_monolith_summon");
    private static final ResourceLocation TECTONIC_SOUND_ID = new ResourceLocation(PrimordialMobs.NAMESPACE, "tectonic_shard_transform");

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
     * The three prehistoric mixtures. Each does ONE distinct thing to a Sniffer, mirroring how the
     * dinosaurs each react to a different mixture (a Serene Salad calms a stunned Roarer or tames a
     * relaxed Stealer, a Seething Stew sends a Logger into a tree-felling frenzy):
     *   SERENE_SALAD    the calming mixture is also how a wild Sniffer is TAMED (1 chance in 3, like
     *                   the Stealer). On one that is already tame it clears Stunned and sends it to
     *                   lie down and rest.
     *   SEETHING_STEW   rage: for a minute the Sniffer drops everything and headbutts the hostile
     *                   mobs around it — the seething mixture makes it seethe.
     *   PRIMORDIAL_SOUP patience food: halves the remaining wait until the next sniff (vanilla makes
     *                   a Sniffer wait 9600 ticks between digs), and stands a resting one up so it
     *                   can get on with it.
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
     * Applies a mixture's Sniffer-specific effect (server side). {@code player} is whoever fed it —
     * the Serene Salad's taming roll needs them. Returns true if the sniffer visibly reacted.
     */
    public static boolean applyMixture(Sniffer sniffer, Mixture mixture, net.minecraft.world.entity.player.Player player) {
        SnifferSkinHolder holder = sniffer instanceof SnifferSkinHolder h ? h : null;
        switch (mixture) {
            case SERENE_SALAD -> {
                // the salad is the calming mixture everywhere in the mod: it clears Stunned...
                MobEffect stunned = ForgeRegistries.MOB_EFFECTS.getValue(STUNNED_EFFECT_ID);
                if (stunned != null) {
                    sniffer.removeEffect(stunned);
                }
                if (holder == null) {
                    return true;
                }
                // ...and, exactly like the Stealer's, it is the TAMING food: 1 chance in 3 on a wild
                // adult. The caller shows the hearts/smoke (see SnifferEvents).
                if (!holder.ac_isTame() && !sniffer.isBaby()) {
                    if (sniffer.getRandom().nextInt(3) == 0) {
                        holder.ac_setOwnerUUID(player.getUUID());
                        holder.ac_setCommand(1);
                        return true;
                    }
                    return false;
                }
                // On one already tamed it stays the calming mixture: lie down and rest.
                holder.ac_setCommand(1);
                return true;
            }
            case SEETHING_STEW -> {
                // The seething mixture makes it seethe: a minute of headbutting rage.
                if (holder == null || sniffer.isBaby()) {
                    return false;
                }
                if (holder.ac_isOrderedToSit()) {
                    holder.ac_setCommand(0);
                }
                holder.ac_enrage(RAGE_TICKS);
                return true;
            }
            case PRIMORDIAL_SOUP -> {
                // Patience food: the wait until the next sniff is halved, not erased.
                if (holder != null && holder.ac_isTame() && holder.ac_getCommand() == 1) {
                    holder.ac_setCommand(0);         // stand up: a sitting sniffer never sniffs
                }
                var brain = sniffer.getBrain();
                if (brain.getMemory(MemoryModuleType.SNIFF_COOLDOWN).isPresent()) {
                    long remaining = brain.getTimeUntilExpiry(MemoryModuleType.SNIFF_COOLDOWN);
                    brain.eraseMemory(MemoryModuleType.SNIFF_COOLDOWN);
                    if (remaining > 1) {
                        brain.setMemoryWithExpiry(MemoryModuleType.SNIFF_COOLDOWN, net.minecraft.util.Unit.INSTANCE, remaining / 2);
                    }
                }
                return true;
            }
            default -> {
                return false;
            }
        }
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
