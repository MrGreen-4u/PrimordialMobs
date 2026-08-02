package com.primordialmobs.server.item;

import com.primordialmobs.PrimordialMobs;
import com.primordialmobs.server.enchantment.PMEnchantmentRegistry;
import com.primordialmobs.server.message.UpdateEffectVisualityEntityMessage;
import com.primordialmobs.server.misc.PMSoundRegistry;
import com.primordialmobs.server.potion.PMEffectRegistry;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public class PrimitiveClubItem extends Item {
    private final Multimap<Attribute, AttributeModifier>[] defaultModifiers = new ImmutableMultimap[4];

    public PrimitiveClubItem(Item.Properties properties) {
        super(properties);
        for (int i = 0; i <= 3; i++) {
            this.defaultModifiers[i] = getStatsForEnchantmentLevel(i);
        }
    }

    private ImmutableMultimap getStatsForEnchantmentLevel(int swiftwoodLevel) {
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier", 8.0D, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", (double) Math.min(0, -3.75F + 0.15F * swiftwoodLevel), AttributeModifier.Operation.ADDITION));
        return builder.build();
    }

    @Override
    public int getEnchantmentValue() {
        return 1;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return stack.getCount() == 1;
    }

    public boolean hurtEnemy(ItemStack stack, LivingEntity hurtEntity, LivingEntity player) {
        stack.hurtAndBreak(1, player, (entity) -> {
            entity.broadcastBreakEvent(EquipmentSlot.MAINHAND);
        });
        if (!hurtEntity.level().isClientSide) {
            SoundEvent soundEvent = PMSoundRegistry.PRIMITIVE_CLUB_MISS.get();
            if (hurtEntity.getRandom().nextFloat() < 0.8F) {
                MobEffectInstance instance = new MobEffectInstance(PMEffectRegistry.STUNNED.get(), 150 + hurtEntity.getRandom().nextInt(150), 0, false, false);
                if (hurtEntity.addEffect(instance)) {
                    PrimordialMobs.sendMSGToAll(new UpdateEffectVisualityEntityMessage(hurtEntity.getId(), player.getId(), 3, instance.getDuration()));
                    soundEvent = PMSoundRegistry.PRIMITIVE_CLUB_HIT.get();
                }
                // Dazing Sweep: the stun splashes onto everything around the victim.
                int dazingEdgeLevel = stack.getEnchantmentLevel(PMEnchantmentRegistry.DAZING_SWEEP.get());
                if (dazingEdgeLevel > 0) {
                    float f = dazingEdgeLevel + 1.2F;
                    AABB aabb = AABB.ofSize(hurtEntity.position(), f, f, f);
                    for (Entity entity : hurtEntity.level().getEntities(player, aabb, Entity::canBeHitByProjectile)) {
                        if (!entity.is(hurtEntity) && !entity.isAlliedTo(player) && entity.distanceTo(hurtEntity) <= f && entity instanceof LivingEntity inflict) {
                            MobEffectInstance instance2 = new MobEffectInstance(PMEffectRegistry.STUNNED.get(), 80 + hurtEntity.getRandom().nextInt(80), 0, false, false);
                            inflict.hurt(inflict.level().damageSources().mobAttack(player), 1.0F);
                            if (inflict.addEffect(instance2)) {
                                PrimordialMobs.sendMSGToAll(new UpdateEffectVisualityEntityMessage(inflict.getId(), player.getId(), 3, instance2.getDuration()));
                            }
                        }
                    }
                }
            }
            player.level().playSound((Player) null, player.getX(), player.getY(), player.getZ(), soundEvent, player.getSoundSource(), 1.0F, 1.0F);

        }
        return true;
    }

    public boolean mineBlock(ItemStack itemStack, Level level, BlockState state, BlockPos blockPos, LivingEntity
            livingEntity) {
        if ((double) state.getDestroySpeed(level, blockPos) != 0.0D) {
            itemStack.hurtAndBreak(2, livingEntity, (entity) -> {
                entity.broadcastBreakEvent(EquipmentSlot.MAINHAND);
            });
        }

        return true;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot equipmentSlot) {
        return equipmentSlot == EquipmentSlot.MAINHAND ? this.defaultModifiers[0] : super.getDefaultAttributeModifiers(equipmentSlot);
    }

    public boolean isValidRepairItem(ItemStack item, ItemStack repairItem) {
        return repairItem.is(PMItemRegistry.HEAVY_BONE.get()) || super.isValidRepairItem(item, repairItem);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        // Swiftwood raises the attack speed; the four modifier sets were built for levels 0-3 in the ctor.
        int swift = stack.getEnchantmentLevel(PMEnchantmentRegistry.SWIFTWOOD.get());
        return slot == EquipmentSlot.MAINHAND ? defaultModifiers[Mth.clamp(swift, 0, 3)] : super.getAttributeModifiers(slot, stack);
    }

    @Override
    public void initializeClient(java.util.function.Consumer<IClientItemExtensions> consumer) {
        consumer.accept((IClientItemExtensions) PrimordialMobs.PROXY.getISTERProperties());
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        return player.getAttackStrengthScale(0) < 0.95 || player.attackAnim != 0;
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        if (entity instanceof Player player) {
            if (player.getAttackStrengthScale(0) < 1 && player.attackAnim > 0) {
                return true;
            } else {
                player.swingTime = -1;
            }
        }
        return false;
    }

    public void inventoryTick(ItemStack stack, Level level, Entity entity, int i, boolean held) {
        if (entity instanceof Player player && held) {
            if (player.getAttackStrengthScale(0) < 0.95 && player.attackAnim > 0) {
                player.swingTime--;
            }
        }
    }
}
