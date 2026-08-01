package com.primordialmobs.server.level.feature;

import com.primordialmobs.PrimordialMobs;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class PMFeatureRegistry {
    public static final DeferredRegister<Feature<?>> DEF_REG = DeferredRegister.create(ForgeRegistries.FEATURES, PrimordialMobs.NAMESPACE);
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> PEWEN_TREE = DEF_REG.register("pewen_tree", () -> new PewenTreeFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> ANCIENT_TREE = DEF_REG.register("ancient_tree", () -> new AncientTreeFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> GIANT_ANCIENT_TREE = DEF_REG.register("giant_ancient_tree", () -> new GiantAncientTreeFeature(NoneFeatureConfiguration.CODEC));
}
