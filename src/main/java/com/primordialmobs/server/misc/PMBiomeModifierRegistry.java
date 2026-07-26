package com.primordialmobs.server.misc;

import com.primordialmobs.PrimordialMobs;
import com.primordialmobs.server.entity.util.SnifferTaming;
import com.primordialmobs.server.entity.util.TrilocarisSpawns;
import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registered in BOTH modes (serializers are cheap and the data file ships unconditionally). The modifier
 * only ever matches the Alex's Caves Primordial Caves biome, so standalone (where that biome does not
 * exist) it is a no-op.
 */
public class PMBiomeModifierRegistry {

    public static final DeferredRegister<Codec<? extends BiomeModifier>> DEF_REG = DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, PrimordialMobs.MODID);

    public static final RegistryObject<Codec<AddPrimordialSnifferModifier>> ADD_PRIMORDIAL_SNIFFER = DEF_REG.register("add_primordial_sniffer", () -> Codec.unit(AddPrimordialSnifferModifier.INSTANCE));

    public static final RegistryObject<Codec<AddLushCavesTrilocarisModifier>> ADD_LUSH_TRILOCARIS = DEF_REG.register("add_lush_trilocaris", () -> Codec.unit(AddLushCavesTrilocarisModifier.INSTANCE));

    /**
     * Puts the Trilocaris in the Lush Caves alongside the axolotls, in the SAME spawner list they use.
     *
     * Done by code rather than with a data-driven forge:add_spawns because that helper files a mob under its
     * OWN MobCategory, and with the full Alex's Caves installed the Trilocaris is a WATER_AMBIENT entity
     * (ours is AXOLOTLS). It would land in a different list, with a different mob cap and a different spawn
     * pass -- i.e. NOT "the same spawns as an axolotl". Naming the category explicitly makes both modes
     * behave identically. Alex's Caves does the same thing with minecraft:frog in its cave_creature list.
     *
     * The water-over-clay condition is the placement rule, in {@link TrilocarisSpawns}.
     */
    public static class AddLushCavesTrilocarisModifier implements BiomeModifier {
        public static final AddLushCavesTrilocarisModifier INSTANCE = new AddLushCavesTrilocarisModifier();

        @Override
        public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
            if (phase != Phase.ADD || !biome.is(TrilocarisSpawns.LUSH_CAVES)) {
                return;
            }
            EntityType<?> trilocaris = net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getValue(TrilocarisSpawns.TRILOCARIS);
            if (trilocaris != null) {
                // Rarer than the axolotls themselves (weight 10, 4-6), so they read as the odd one out.
                builder.getMobSpawnSettings().addSpawn(MobCategory.AXOLOTLS, new MobSpawnSettings.SpawnerData(trilocaris, 2, 1, 4));
            } else {
                PrimordialMobs.LOGGER.warn("Lush Caves found but alexscaves:trilocaris did not resolve; no Trilocaris spawns added");
            }
        }

        @Override
        public Codec<? extends BiomeModifier> codec() {
            return ADD_LUSH_TRILOCARIS.get();
        }
    }

    /**
     * Adds natural Sniffer spawns to alexscaves:primordial_caves, in the same "alexscaves:cave_creature"
     * spawner list the Grottoceratops (weight 27, 2-4) and Relicheirus (weight 13, 1-1) use. Weight 13 with
     * packs of 1-2 puts the Sniffer at Relicheirus-like rarity. Alex's Caves itself adds minecraft:frog
     * (a CREATURE-category mob) to this list the same way.
     */
    public static class AddPrimordialSnifferModifier implements BiomeModifier {
        public static final AddPrimordialSnifferModifier INSTANCE = new AddPrimordialSnifferModifier();

        @Override
        public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
            if (phase == Phase.ADD && biome.is(SnifferTaming.PRIMORDIAL_CAVES_BIOME)) {
                MobCategory caveCreature = findCaveCreature();
                if (caveCreature != null) {
                    builder.getMobSpawnSettings().addSpawn(caveCreature, new MobSpawnSettings.SpawnerData(EntityType.SNIFFER, 13, 1, 2));
                } else {
                    PrimordialMobs.LOGGER.warn("Primordial Caves found but no cave_creature MobCategory; no natural Sniffer spawns will be added");
                }
            }
        }

        /**
         * Finds Alex's Caves' extensible spawn category.
         *
         * GOTCHA, measured on a real server: Forge's extensible-enum MobCategory keeps the FULL id as its
         * name, so this is called "alexscaves:cave_creature", NOT "cave_creature". Matching the bare name
         * silently found nothing and the Sniffer was never added to the biome -- which is exactly how this
         * shipped unnoticed, because a missing spawn entry produces no error anywhere. Accept both spellings
         * so a future Alex's Caves that drops the namespace still works.
         */
        private static MobCategory findCaveCreature() {
            for (MobCategory category : MobCategory.values()) {
                String name = category.getName();
                if ("cave_creature".equals(name) || name.endsWith(":cave_creature")) {
                    return category;
                }
            }
            return null;
        }

        @Override
        public Codec<? extends BiomeModifier> codec() {
            return ADD_PRIMORDIAL_SNIFFER.get();
        }
    }
}
