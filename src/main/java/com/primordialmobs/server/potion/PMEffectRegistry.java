package com.primordialmobs.server.potion;


import com.primordialmobs.PrimordialMobs;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class PMEffectRegistry {

    public static final DeferredRegister<MobEffect> DEF_REG = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, PrimordialMobs.NAMESPACE);
    public static final RegistryObject<MobEffect> STUNNED = DEF_REG.register("stunned", () -> new StunnedEffect());
}
