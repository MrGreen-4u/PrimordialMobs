package com.primordialmobs.server.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;

public class PMFoods {
    public static final FoodProperties TRILOCARIS_TAIL = (new FoodProperties.Builder()).nutrition(2).saturationMod(0.3F).meat().build();
    public static final FoodProperties TRILOCARIS_TAIL_COOKED = (new FoodProperties.Builder()).nutrition(5).saturationMod(0.5F).meat().build();
    public static final FoodProperties PINE_NUTS = (new FoodProperties.Builder()).nutrition(2).saturationMod(0.175F).build();
    public static final FoodProperties DINOSAUR_NUGGETS = (new FoodProperties.Builder()).nutrition(3).saturationMod(0.3F).meat().fast().build();
    public static final FoodProperties SERENE_SALAD = (new FoodProperties.Builder()).nutrition(5).saturationMod(0.35F).build();
    public static final FoodProperties PRIMORDIAL_SOUP = (new FoodProperties.Builder()).nutrition(6).saturationMod(0.6F).effect(() -> new MobEffectInstance(MobEffects.DIG_SPEED, 800), 1.0F).build();
    /**
     * Alex's Caves grants its own "rage" effect here, which belongs to caves this mod does not ship;
     * Strength for the same 2200 ticks is the closest vanilla stand-in. Same nutrition/saturation.
     */
    public static final FoodProperties SEETHING_STEW = (new FoodProperties.Builder()).nutrition(6).saturationMod(0.6F).effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, 2200), 1.0F).build();
}
