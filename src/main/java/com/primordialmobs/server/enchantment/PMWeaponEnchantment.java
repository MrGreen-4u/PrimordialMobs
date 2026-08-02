package com.primordialmobs.server.enchantment;

import com.primordialmobs.PrimordialMobs;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

/**
 * Base class for the mod's weapon enchantments, identical to upstream ACWeaponEnchantment: a linear
 * min-cost curve, a fixed max level, and tradeability/book-availability gated on the
 * {@code enchantments_in_loot} config option (which defaults to false, i.e. these are only obtainable
 * by enchanting the weapon itself).
 */
public class PMWeaponEnchantment extends Enchantment {

    private final int levels;
    private final int minXP;
    private final String registryName;

    protected PMWeaponEnchantment(String name, Rarity rarity, EnchantmentCategory category, int levels, int minXP, EquipmentSlot... equipmentSlot) {
        super(rarity, category, equipmentSlot);
        this.levels = levels;
        this.minXP = minXP;
        this.registryName = name;
    }

    public int getMinCost(int i) {
        return 1 + (i - 1) * minXP;
    }

    public int getMaxCost(int i) {
        return super.getMinCost(i) + 30;
    }

    public int getMaxLevel() {
        return levels;
    }

    protected boolean checkCompatibility(Enchantment enchantment) {
        return this != enchantment && PMEnchantmentRegistry.areCompatible(this, enchantment);
    }

    public boolean isTradeable() {
        return PrimordialMobs.COMMON_CONFIG.enchantmentsInLoot.get();
    }

    public boolean isDiscoverable() {
        return true;
    }

    public boolean isAllowedOnBooks() {
        return PrimordialMobs.COMMON_CONFIG.enchantmentsInLoot.get();
    }

    public String getName() {
        return registryName;
    }
}
