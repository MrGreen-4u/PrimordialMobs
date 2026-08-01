package com.primordialmobs.server.entity;

import com.primordialmobs.PrimordialMobs;
import com.primordialmobs.server.entity.item.FallingTreeBlockEntity;
import com.primordialmobs.server.entity.living.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class PMEntityRegistry {
    // NOTE: no @Mod.EventBusSubscriber here on purpose. That annotation class-loads this registry even in
    // Alex's-Caves-compat mode, which would duplicate the extensible cave_creature MobCategory. The two
    // event handlers below are added manually from the mod constructor, standalone mode only.


    public static final DeferredRegister<EntityType<?>> DEF_REG = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, PrimordialMobs.NAMESPACE);
    public static final MobCategory CAVE_CREATURE = MobCategory.create("cave_creature", "alexscaves:cave_creature", 10, true, true, 128);
    public static final RegistryObject<EntityType<FallingTreeBlockEntity>> FALLING_TREE_BLOCK = DEF_REG.register("falling_tree_block", () -> (EntityType) EntityType.Builder.of(FallingTreeBlockEntity::new, MobCategory.MISC).sized(0.99F, 0.99F).setCustomClientFactory(FallingTreeBlockEntity::new).setUpdateInterval(1).setShouldReceiveVelocityUpdates(true).updateInterval(10).clientTrackingRange(20).build("falling_tree_block"));
    public static final RegistryObject<EntityType<TrilocarisEntity>> TRILOCARIS = DEF_REG.register("trilocaris", () -> (EntityType) EntityType.Builder.of(TrilocarisEntity::new, MobCategory.AXOLOTLS).sized(0.9F, 0.4F).build("trilocaris"));
    public static final RegistryObject<EntityType<SubterranodonEntity>> SUBTERRANODON = DEF_REG.register("subterranodon", () -> (EntityType) EntityType.Builder.of(SubterranodonEntity::new, CAVE_CREATURE).sized(1.75F, 1.2F).setTrackingRange(12).setShouldReceiveVelocityUpdates(true).setUpdateInterval(1).build("subterranodon"));
    public static final RegistryObject<EntityType<VallumraptorEntity>> VALLUMRAPTOR = DEF_REG.register("vallumraptor", () -> (EntityType) EntityType.Builder.of(VallumraptorEntity::new, CAVE_CREATURE).sized(0.8F, 1.5F).setTrackingRange(8).build("vallumraptor"));
    public static final RegistryObject<EntityType<GrottoceratopsEntity>> GROTTOCERATOPS = DEF_REG.register("grottoceratops", () -> (EntityType) EntityType.Builder.of(GrottoceratopsEntity::new, CAVE_CREATURE).sized(2.3F, 2.5F).setTrackingRange(8).build("grottoceratops"));
    public static final RegistryObject<EntityType<TremorsaurusEntity>> TREMORSAURUS = DEF_REG.register("tremorsaurus", () -> (EntityType) EntityType.Builder.of(TremorsaurusEntity::new, CAVE_CREATURE).sized(2.5F, 3.85F).setTrackingRange(8).build("tremorsaurus"));
    public static final RegistryObject<EntityType<com.primordialmobs.server.entity.item.ExtinctionSpearEntity>> EXTINCTION_SPEAR = DEF_REG.register("extinction_spear", () -> (EntityType) EntityType.Builder.of(com.primordialmobs.server.entity.item.ExtinctionSpearEntity::new, MobCategory.MISC).sized(0.5F, 0.5F).setCustomClientFactory(com.primordialmobs.server.entity.item.ExtinctionSpearEntity::new).setUpdateInterval(1).setShouldReceiveVelocityUpdates(true).fireImmune().build("extinction_spear"));
    public static final RegistryObject<EntityType<com.primordialmobs.server.entity.item.DinosaurSpiritEntity>> DINOSAUR_SPIRIT = DEF_REG.register("dinosaur_spirit", () -> (EntityType) EntityType.Builder.of(com.primordialmobs.server.entity.item.DinosaurSpiritEntity::new, MobCategory.MISC).sized(1.0F, 1.0F).setCustomClientFactory(com.primordialmobs.server.entity.item.DinosaurSpiritEntity::new).setUpdateInterval(1).setShouldReceiveVelocityUpdates(true).fireImmune().build("dinosaur_spirit"));
    public static final RegistryObject<EntityType<RelicheirusEntity>> RELICHEIRUS = DEF_REG.register("relicheirus", () -> (EntityType) EntityType.Builder.of(RelicheirusEntity::new, CAVE_CREATURE).sized(2.65F, 5.9F).setTrackingRange(9).build("relicheirus"));

    public static void initializeAttributes(EntityAttributeCreationEvent event) {
        event.put(TRILOCARIS.get(), TrilocarisEntity.createAttributes().build());
        event.put(SUBTERRANODON.get(), SubterranodonEntity.createAttributes().build());
        event.put(VALLUMRAPTOR.get(), VallumraptorEntity.createAttributes().build());
        event.put(GROTTOCERATOPS.get(), GrottoceratopsEntity.createAttributes().build());
        event.put(TREMORSAURUS.get(), TremorsaurusEntity.createAttributes().build());
        event.put(RELICHEIRUS.get(), RelicheirusEntity.createAttributes().build());
    }

    public static void spawnPlacements(SpawnPlacementRegisterEvent event) {
        event.register(TRILOCARIS.get(), SpawnPlacements.Type.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, TrilocarisEntity::checkTrilocarisSpawnRules, SpawnPlacementRegisterEvent.Operation.AND);
        event.register(SUBTERRANODON.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SubterranodonEntity::checkSubterranodonSpawnRules, SpawnPlacementRegisterEvent.Operation.AND);
        event.register(VALLUMRAPTOR.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, VallumraptorEntity::checkPrehistoricSpawnRules, SpawnPlacementRegisterEvent.Operation.AND);
        event.register(GROTTOCERATOPS.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, GrottoceratopsEntity::checkPrehistoricSpawnRules, SpawnPlacementRegisterEvent.Operation.AND);
        event.register(TREMORSAURUS.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, TremorsaurusEntity::checkPrehistoricSpawnRules, SpawnPlacementRegisterEvent.Operation.AND);
        event.register(RELICHEIRUS.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, RelicheirusEntity::checkPrehistoricSpawnRules, SpawnPlacementRegisterEvent.Operation.AND);
    }
}
