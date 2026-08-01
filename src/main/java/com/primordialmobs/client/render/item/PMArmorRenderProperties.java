package com.primordialmobs.client.render.item;

import com.primordialmobs.client.model.PMModelLayers;
import com.primordialmobs.client.model.PrimordialArmorModel;
import com.primordialmobs.server.item.PrimordialArmorItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

/** Serves the custom Primordial armor model (bone crest, hide flaps) instead of the flat humanoid layer. */
public class PMArmorRenderProperties implements IClientItemExtensions {

    private static PrimordialArmorModel primordialArmorModel;

    @Override
    public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entityLiving, ItemStack itemStack, EquipmentSlot armorSlot, HumanoidModel<?> _default) {
        if (itemStack.getItem() instanceof PrimordialArmorItem) {
            if (primordialArmorModel == null) {
                primordialArmorModel = new PrimordialArmorModel(Minecraft.getInstance().getEntityModels().bakeLayer(PMModelLayers.PRIMORDIAL_ARMOR));
            }
            return entityLiving == null ? primordialArmorModel : primordialArmorModel.withAnimations(entityLiving);
        }
        return _default;
    }
}
