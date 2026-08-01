package com.primordialmobs.mixin.standalone;

import com.primordialmobs.server.item.PrimordialArmorItem;
import com.primordialmobs.server.misc.PMTagRegistry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Upstream parity: each worn Primordial armor piece adds +1 nutrition (+0.125 saturation) when eating raw
 * meat. Standalone only (the mixin.standalone.* package is gated off when the full Alex's Caves is
 * installed, whose identical mixin and armor then apply instead).
 */
@Mixin(FoodData.class)
public abstract class FoodDataMixin {

    @Shadow
    public abstract void eat(int nutrition, float saturation);

    @Inject(
            method = {"Lnet/minecraft/world/food/FoodData;eat(Lnet/minecraft/world/item/Item;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;)V"},
            cancellable = true,
            remap = false, //FORGE METHOD
            at = @At(value = "HEAD")
    )
    public void primordialmobs$eat(Item item, ItemStack stack, LivingEntity entity, CallbackInfo ci) {
        if (entity != null && stack.is(PMTagRegistry.RAW_MEATS)) {
            int extraShanksFromArmor = PrimordialArmorItem.getExtraSaturationFromArmor(entity);
            if (extraShanksFromArmor != 0) {
                ci.cancel();
                if (item.isEdible()) {
                    FoodProperties foodproperties = stack.getFoodProperties(entity);
                    this.eat(foodproperties.getNutrition() + extraShanksFromArmor, foodproperties.getSaturationModifier() + (extraShanksFromArmor * 0.125F));
                }
            }
        }
    }
}
