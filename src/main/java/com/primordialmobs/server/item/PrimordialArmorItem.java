package com.primordialmobs.server.item;

import com.primordialmobs.PrimordialMobs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import javax.annotation.Nullable;

/**
 * The three-piece Primordial armor set (helmet, tunic, pants — no boots), faithful to upstream: leather-tier
 * sounds, tough-hide repair, and each worn piece adds +1 nutrition (+0.125 saturation) when eating raw meat
 * or Dinosaur Chops (see FoodDataMixin and DinosaurChopBlock).
 */
public class PrimordialArmorItem extends ArmorItem {

    public PrimordialArmorItem(ArmorMaterial armorMaterial, Type slot) {
        super(armorMaterial, slot, new Properties());
    }

    @Override
    public void initializeClient(java.util.function.Consumer<IClientItemExtensions> consumer) {
        consumer.accept((IClientItemExtensions) PrimordialMobs.PROXY.getArmorProperties());
    }

    @Nullable
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        if (slot == EquipmentSlot.LEGS) {
            return PrimordialMobs.NAMESPACE + ":textures/armor/primordial_armor_1.png";
        } else {
            return PrimordialMobs.NAMESPACE + ":textures/armor/primordial_armor_0.png";
        }
    }

    public static int getExtraSaturationFromArmor(LivingEntity entity) {
        int i = 0;
        if (entity.getItemBySlot(EquipmentSlot.HEAD).is(PMItemRegistry.PRIMORDIAL_HELMET.get())) {
            i++;
        }
        if (entity.getItemBySlot(EquipmentSlot.CHEST).is(PMItemRegistry.PRIMORDIAL_TUNIC.get())) {
            i++;
        }
        if (entity.getItemBySlot(EquipmentSlot.LEGS).is(PMItemRegistry.PRIMORDIAL_PANTS.get())) {
            i++;
        }
        return i;
    }
}
