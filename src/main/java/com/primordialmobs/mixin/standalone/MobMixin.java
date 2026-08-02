package com.primordialmobs.mixin.standalone;

import com.primordialmobs.server.entity.util.EntityDropChanceAccessor;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Exposes the three protected Mob members the Bonking enchantment needs (equipment drop chances and the
 * custom-death-loot call). They must be reached with {@code @Shadow} rather than a cast: a cast to Mob
 * cannot see protected members from outside the class hierarchy.
 *
 * Standalone only: with the full Alex's Caves installed its identical mixin and its own Bonking
 * enchantment are in play instead.
 */
@Mixin(Mob.class)
public abstract class MobMixin extends LivingEntity implements EntityDropChanceAccessor {

    @Shadow
    protected abstract float getEquipmentDropChance(EquipmentSlot equipmentSlot);

    @Shadow
    public abstract void setDropChance(EquipmentSlot equipmentSlot, float chance);

    @Shadow
    protected abstract void dropCustomDeathLoot(DamageSource damageSource, int i1, boolean idk);

    protected MobMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    public float pm_getEquipmentDropChance(EquipmentSlot equipmentSlot) {
        return this.getEquipmentDropChance(equipmentSlot);
    }

    public void pm_setDropChance(EquipmentSlot equipmentSlot, float chance) {
        this.setDropChance(equipmentSlot, chance);
    }

    public void pm_dropCustomDeathLoot(DamageSource damageSource, int i1, boolean idk) {
        this.dropCustomDeathLoot(damageSource, i1, idk);
    }
}
