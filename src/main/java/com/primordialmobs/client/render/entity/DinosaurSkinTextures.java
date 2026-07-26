package com.primordialmobs.client.render.entity;

import com.primordialmobs.PrimordialMobs;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class DinosaurSkinTextures {

    private static final Map<String, ResourceLocation> CACHE = new HashMap<>();

    public static ResourceLocation get(String textureName) {
        return CACHE.computeIfAbsent(textureName, name -> new ResourceLocation(PrimordialMobs.NAMESPACE, "textures/entity/" + name + ".png"));
    }

    public static String skinSuffix(int altSkin) {
        return altSkin == 1 ? "_retro" : altSkin == 2 ? "_tectonic" : "";
    }
}
