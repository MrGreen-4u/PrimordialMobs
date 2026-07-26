package com.primordialmobs.compat;

import com.github.alexmodguy.alexscaves.server.entity.living.DinosaurEntity;
import com.github.alexmodguy.alexscaves.server.entity.living.TrilocarisEntity;
import com.primordialmobs.PrimordialMobs;
import com.primordialmobs.server.misc.PMTagRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Shared helpers for the compat-mode overlay over Alex's Caves' Primordial Caves dinosaurs.
 *
 * References Alex's Caves classes directly, so it must only ever be class-loaded when Alex's Caves is
 * installed — every caller is itself compat-gated (the {@code compat} mixins via
 * {@code PrimordialMixinPlugin}, {@link CompatEvents} via the branch in {@code PrimordialMobs}).
 */
public class CompatDinosaurs {

    private static final ResourceLocation GROTTOCERATOPS = new ResourceLocation(PrimordialMobs.NAMESPACE, "grottoceratops");
    private static final ResourceLocation RELICHEIRUS = new ResourceLocation(PrimordialMobs.NAMESPACE, "relicheirus");

    /**
     * The two Primordial Caves dinosaurs that Alex's Caves leaves wild but Primordial Mobs makes tameable,
     * commandable and rideable. The other three (Roarer, Glider, Stealer) already do all of that in Alex's
     * Caves with exactly our code, so they need no overlay at all.
     */
    public static boolean isTameableByUs(Entity entity) {
        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        return GROTTOCERATOPS.equals(id) || RELICHEIRUS.equals(id);
    }

    public static boolean isRammer(Entity entity) {
        return GROTTOCERATOPS.equals(ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()));
    }

    public static boolean isLogger(Entity entity) {
        return RELICHEIRUS.equals(ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()));
    }

    /** Everything the Logger fishes: the tag, which Alex's Caves' own data merges into. */
    public static boolean isFishable(Entity entity) {
        return entity.getType().is(PMTagRegistry.RELICHEIRUS_FISHES);
    }

    /**
     * Fishable prey that is NOT a Trilocaris. Alex's Caves already carries and bites Trilocaris itself, so
     * the compat overlay must only handle the rest or the prey would be moved and damaged twice.
     */
    public static boolean isExtraFish(Entity entity) {
        return !(entity instanceof TrilocarisEntity) && isFishable(entity);
    }

    /** True for a tamed dinosaur owned by nobody in particular — used to gate command/mount overrides. */
    public static boolean isOurTamedMount(DinosaurEntity dinosaur) {
        return dinosaur.isTame() && isTameableByUs(dinosaur);
    }

    /** The Tree Star, resolved by id so it hits Alex's Caves' block item in compat mode. */
    public static net.minecraft.world.item.Item treeStar() {
        return ForgeRegistries.ITEMS.getValue(new ResourceLocation(PrimordialMobs.NAMESPACE, "tree_star"));
    }

    /** The Serene Salad, resolved by id. */
    public static net.minecraft.world.item.Item sereneSalad() {
        return ForgeRegistries.ITEMS.getValue(new ResourceLocation(PrimordialMobs.NAMESPACE, "serene_salad"));
    }

    public static EntityType<?> entityType(String path) {
        return ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(PrimordialMobs.NAMESPACE, path));
    }
}
