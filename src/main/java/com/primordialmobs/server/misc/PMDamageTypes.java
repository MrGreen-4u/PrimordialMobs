package com.primordialmobs.server.misc;

import com.primordialmobs.PrimordialMobs;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;

/**
 * The one damage type the absorbed Extinction Spear needs. Upstream wraps this in a
 * DamageSourceRandomMessages with several death lines; a plain DamageSource with the single
 * "death.attack.spirit_dinosaur" lang key keeps the surface minimal.
 */
public class PMDamageTypes {

    public static final ResourceKey<DamageType> SPIRIT_DINOSAUR = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(PrimordialMobs.NAMESPACE, "spirit_dinosaur"));

    public static DamageSource causeSpiritDinosaurDamage(RegistryAccess registryAccess, Entity source) {
        return new DamageSource(registryAccess.registry(Registries.DAMAGE_TYPE).get().getHolderOrThrow(SPIRIT_DINOSAUR), source);
    }
}
