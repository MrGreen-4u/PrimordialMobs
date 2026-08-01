package com.primordialmobs.server.block.blockentity;

import com.primordialmobs.PrimordialMobs;
import com.primordialmobs.server.block.PMBlockRegistry;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class PMBlockEntityRegistry {

    public static final DeferredRegister<BlockEntityType<?>> DEF_REG = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, PrimordialMobs.NAMESPACE);

    public static final RegistryObject<BlockEntityType<AmberMonolithBlockEntity>> AMBER_MONOLITH = DEF_REG.register("amber_monolith", () -> BlockEntityType.Builder.of(AmberMonolithBlockEntity::new, PMBlockRegistry.AMBER_MONOLITH.get()).build(null));
}
