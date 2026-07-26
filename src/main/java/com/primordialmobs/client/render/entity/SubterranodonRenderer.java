package com.primordialmobs.client.render.entity;

import com.primordialmobs.PrimordialMobs;
import com.primordialmobs.client.model.SubterranodonModel;
import com.primordialmobs.client.render.entity.layer.SubterranodonRiderLayer;
import com.primordialmobs.server.entity.living.SubterranodonEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class SubterranodonRenderer extends MobRenderer<SubterranodonEntity, SubterranodonModel> {

    public SubterranodonRenderer(EntityRendererProvider.Context renderManagerIn) {
        super(renderManagerIn, new SubterranodonModel(), 0.5F);
        this.addLayer(new SubterranodonRiderLayer(this));

    }

    public ResourceLocation getTextureLocation(SubterranodonEntity entity) {
        String name = "subterranodon" + DinosaurSkinTextures.skinSuffix(entity.getAltSkin());
        if (entity.isRecolored()) {
            name += "_variant";
        }
        return DinosaurSkinTextures.get(name);
    }
}

