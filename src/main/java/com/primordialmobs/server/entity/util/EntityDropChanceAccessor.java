package com.primordialmobs.server.entity.util;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;

/** Implemented on Mob by {@link com.primordialmobs.mixin.standalone.MobMixin} so the Bonking enchantment
 * can fake a charged-creeper kill (drop the mob's head) without also dropping its equipment. */
public interface EntityDropChanceAccessor {

    float pm_getEquipmentDropChance(EquipmentSlot equipmentSlot);

    void pm_setDropChance(EquipmentSlot equipmentSlot, float chance);

    void pm_dropCustomDeathLoot(DamageSource damageSource, int i1, boolean idk);
}
