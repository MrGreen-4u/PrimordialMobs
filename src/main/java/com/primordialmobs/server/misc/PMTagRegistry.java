package com.primordialmobs.server.misc;

import com.primordialmobs.PrimordialMobs;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;

/**
 * The two Alex's Caves-namespace tags this add-on reads.
 *
 * {@code DINOSAURS_SPAWNABLE_ON} is Alex's Caves' own data; {@code RELICHEIRUS_FISHES} is this
 * mod's addition (data/alexscaves/tags/entity_types/relicheirus_fishes.json) steering the
 * Logger's broader fishing goal.
 */
public class PMTagRegistry {

    public static final TagKey<Block> DINOSAURS_SPAWNABLE_ON = registerBlockTag("dinosaurs_spawnable_on");
    public static final TagKey<EntityType<?>> RELICHEIRUS_FISHES = registerEntityTag("relicheirus_fishes");

    private static TagKey<Block> registerBlockTag(String name) {
        return TagKey.create(Registries.BLOCK, new ResourceLocation(PrimordialMobs.NAMESPACE, name));
    }

    private static TagKey<EntityType<?>> registerEntityTag(String name) {
        return TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(PrimordialMobs.NAMESPACE, name));
    }
}
