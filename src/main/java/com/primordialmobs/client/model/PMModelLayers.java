package com.primordialmobs.client.model;

import com.primordialmobs.PrimordialMobs;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.EntityRenderersEvent;

public class PMModelLayers {

    public static final ModelLayerLocation PRIMORDIAL_ARMOR = new ModelLayerLocation(new ResourceLocation(PrimordialMobs.NAMESPACE, "primordial_armor"), "main");

    public static void register(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(PRIMORDIAL_ARMOR, () -> PrimordialArmorModel.createArmorLayer(new CubeDeformation(0.5F)));
    }
}
