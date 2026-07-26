package com.primordialmobs.server.misc;

import com.primordialmobs.PrimordialMobs;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class PMLootTableRegistry {

    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> GLOBAL_LOOT_MODIFIER_DEF_REG = DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, PrimordialMobs.NAMESPACE);
    public static final DeferredRegister<LootItemFunctionType> LOOT_FUNCTION_DEF_REG = DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, PrimordialMobs.NAMESPACE);

    public static final RegistryObject<Codec<PrimordialBrushingLootModifier>> PRIMORDIAL_BRUSHING_LOOT_MODIFIER = GLOBAL_LOOT_MODIFIER_DEF_REG.register("primordial_brushing", PrimordialBrushingLootModifier.CODEC);

}
