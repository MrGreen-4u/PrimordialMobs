package com.primordialmobs.server.misc;

import com.primordialmobs.PrimordialMobs;
import com.primordialmobs.server.item.PMItemRegistry;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.DecoratedPotPatterns;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * The four Alex's Caves pottery sherds (dinosaur, footprint, guardian, hero), standalone mode only.
 * Registration mirrors upstream ACPotPatternRegistry exactly; the ITEM_TO_POT_TEXTURE expansion needs the
 * accesstransformer line for {@code DecoratedPotPatterns.f_271367_} (already present in
 * accesstransformer.cfg, inherited from the original mod).
 */
public class PMPotPatternRegistry {

    public static final DeferredRegister<String> DEF_REG = DeferredRegister.create(Registries.DECORATED_POT_PATTERNS, PrimordialMobs.NAMESPACE);

    public static final RegistryObject<String> DINOSAUR = DEF_REG.register("dinosaur_pottery_pattern", () -> PrimordialMobs.NAMESPACE + ":dinosaur_pottery_pattern");
    public static final RegistryObject<String> FOOTPRINT = DEF_REG.register("footprint_pottery_pattern", () -> PrimordialMobs.NAMESPACE + ":footprint_pottery_pattern");
    public static final RegistryObject<String> GUARDIAN = DEF_REG.register("guardian_pottery_pattern", () -> PrimordialMobs.NAMESPACE + ":guardian_pottery_pattern");
    public static final RegistryObject<String> HERO = DEF_REG.register("hero_pottery_pattern", () -> PrimordialMobs.NAMESPACE + ":hero_pottery_pattern");

    public static void expandVanillaDefinitions() {
        ImmutableMap.Builder<Item, ResourceKey<String>> itemsToPot = new ImmutableMap.Builder<>();
        itemsToPot.putAll(DecoratedPotPatterns.ITEM_TO_POT_TEXTURE);
        itemsToPot.put(PMItemRegistry.DINOSAUR_POTTERY_SHERD.get(), DINOSAUR.getKey());
        itemsToPot.put(PMItemRegistry.FOOTPRINT_POTTERY_SHERD.get(), FOOTPRINT.getKey());
        itemsToPot.put(PMItemRegistry.GUARDIAN_POTTERY_SHERD.get(), GUARDIAN.getKey());
        itemsToPot.put(PMItemRegistry.HERO_POTTERY_SHERD.get(), HERO.getKey());
        DecoratedPotPatterns.ITEM_TO_POT_TEXTURE = itemsToPot.build();
    }
}
