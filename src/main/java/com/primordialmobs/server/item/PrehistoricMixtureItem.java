package com.primordialmobs.server.item;

import com.primordialmobs.server.entity.living.DinosaurEntity;
import com.primordialmobs.server.entity.living.RelicheirusEntity;
import com.primordialmobs.server.entity.living.TremorsaurusEntity;
import com.primordialmobs.server.potion.PMEffectRegistry;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BowlFoodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class PrehistoricMixtureItem extends BowlFoodItem {

    public PrehistoricMixtureItem(Properties properties) {
        super(properties);
    }

    public InteractionResult interactLivingEntity(ItemStack itemStack, Player player, LivingEntity livingEntity, InteractionHand hand) {
        FoodProperties foodProperties = itemStack.getFoodProperties(livingEntity);
        if (!livingEntity.level().isClientSide && livingEntity instanceof Mob && canFeedMob(player, (Mob) livingEntity) && foodProperties != null) {
            livingEntity.heal(foodProperties.getNutrition());
            if (!(livingEntity instanceof DinosaurEntity dinosaur && dinosaur.onFeedMixture(itemStack, player))) {
                if (!foodProperties.getEffects().isEmpty()) {
                    for (Pair<MobEffectInstance, Float> mobEffectInstance : foodProperties.getEffects()) {
                        livingEntity.addEffect(mobEffectInstance.getFirst());
                    }
                }
                if (this == PMItemRegistry.SERENE_SALAD.get()) {
                    livingEntity.removeEffect(PMEffectRegistry.STUNNED.get());
                }
            }
            for (int i = 0; i < 4 + livingEntity.getRandom().nextInt(3); i++) {
                ((ServerLevel) livingEntity.level()).sendParticles(new ItemParticleOption(ParticleTypes.ITEM, itemStack), livingEntity.getRandomX(0.8F), livingEntity.getRandomY(), livingEntity.getRandomZ(0.8F), 0, 0, 0, 0, 0);
            }
            if (!player.isCreative()) {
                itemStack.shrink(1);
                if (!player.addItem(new ItemStack(Items.BOWL))) {
                    player.drop(new ItemStack(Items.BOWL), true);
                }
            }
            return InteractionResult.SUCCESS;
        }
        return super.interactLivingEntity(itemStack, player, livingEntity, hand);
    }

    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (this == PMItemRegistry.SERENE_SALAD.get()) {
            livingEntity.removeEffect(PMEffectRegistry.STUNNED.get());
        }
        return super.finishUsingItem(stack, level, livingEntity);
    }

    private boolean canFeedMob(Player player, Mob mob) {
        if (mob instanceof TremorsaurusEntity && mob.hasEffect(PMEffectRegistry.STUNNED.get()) && this == PMItemRegistry.SERENE_SALAD.get()) {
            return true;
        }
        if (mob instanceof RelicheirusEntity relicheirus && relicheirus.getPushingTreesFor() > 0 && this == PMItemRegistry.PRIMORDIAL_SOUP.get()) {
            return false;
        }
        LivingEntity target = mob.getTarget();
        return target == null || !target.is(player);
    }
}
