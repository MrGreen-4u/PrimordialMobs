package com.primordialmobs.client.render.entity;

import com.primordialmobs.PrimordialMobs;
import com.primordialmobs.server.entity.util.SnifferSkinHolder;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SnifferRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.sniffer.Sniffer;

import java.util.HashMap;
import java.util.Map;

public class PrimordialSnifferRenderer extends SnifferRenderer {

    private static final Map<String, ResourceLocation> CACHE = new HashMap<>();

    public PrimordialSnifferRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(Sniffer sniffer) {
        if (sniffer instanceof SnifferSkinHolder skinHolder) {
            int type = skinHolder.ac_getSkinType();
            boolean recolored = skinHolder.ac_isRecolored();
            if (type != 0 || recolored) {
                String name = "sniffer/sniffer" + (type == 1 ? "_retro" : type == 2 ? "_tectonic" : "");
                if (recolored) {
                    name += "_variant";
                }
                return CACHE.computeIfAbsent(name, n -> new ResourceLocation(PrimordialMobs.NAMESPACE, "textures/entity/" + n + ".png"));
            }
        }
        return super.getTextureLocation(sniffer);
    }
}
