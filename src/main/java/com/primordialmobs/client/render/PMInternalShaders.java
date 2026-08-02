package com.primordialmobs.client.render;

import net.minecraft.client.renderer.ShaderInstance;

import javax.annotation.Nullable;

/**
 * Holder for the mod's own core shaders, filled in from {@code RegisterShadersEvent}. Same pattern as
 * upstream Alex's Caves' ACInternalShaders; this mod only needs the one shader that draws the Extinction
 * Spear's dinosaur spirits.
 */
public class PMInternalShaders {

    private static ShaderInstance renderTypeRedGhostShader;

    @Nullable
    public static ShaderInstance getRenderTypeRedGhostShader() {
        return renderTypeRedGhostShader;
    }

    public static void setRenderTypeRedGhostShader(ShaderInstance instance) {
        renderTypeRedGhostShader = instance;
    }
}
