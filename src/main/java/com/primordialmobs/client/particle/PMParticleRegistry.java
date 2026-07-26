package com.primordialmobs.client.particle;

import com.primordialmobs.PrimordialMobs;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class PMParticleRegistry {

    public static final DeferredRegister<ParticleType<?>> DEF_REG = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, PrimordialMobs.NAMESPACE);
    public static final RegistryObject<SimpleParticleType> WATER_TREMOR = DEF_REG.register("water_tremor", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> STUN_STAR = DEF_REG.register("stun_star", () -> new SimpleParticleType(false));
}
