package com.primordialmobs.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/**
 * The mod's custom render types. Verbatim from upstream Alex's Caves' ACRenderTypes, pruned to the one
 * type this mod needs: the red ghost the Extinction Spear's dinosaur spirits are drawn with.
 *
 * <p>Extends {@link RenderType} only to reach its protected state shards and {@code create}; it is never
 * instantiated.
 */
public class PMRenderTypes extends RenderType {

    protected static final RenderStateShard.ShaderStateShard RENDERTYPE_RED_GHOST_SHADER =
            new RenderStateShard.ShaderStateShard(PMInternalShaders::getRenderTypeRedGhostShader);

    /**
     * Additive-on-alpha blending: the silhouette adds its light to whatever is behind it instead of
     * replacing it, which is what makes the spirits read as translucent glowing ghosts rather than solid
     * red models.
     */
    protected static final RenderStateShard.TransparencyStateShard EYES_ALPHA_TRANSPARENCY =
            new RenderStateShard.TransparencyStateShard("eyes_alpha_transparency", () -> {
                RenderSystem.enableBlend();
                RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            }, () -> {
                RenderSystem.disableBlend();
                RenderSystem.defaultBlendFunc();
            });

    private PMRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
        throw new IllegalStateException("This class must not be instantiated");
    }

    public static RenderType getRedGhost(ResourceLocation locationIn) {
        return create("red_ghost", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, true, RenderType.CompositeState.builder()
                .setShaderState(RENDERTYPE_RED_GHOST_SHADER)
                .setCullState(NO_CULL)
                .setTextureState(new RenderStateShard.TextureStateShard(locationIn, false, false))
                .setTransparencyState(EYES_ALPHA_TRANSPARENCY)
                .setWriteMaskState(COLOR_DEPTH_WRITE)
                .setDepthTestState(LEQUAL_DEPTH_TEST)
                .setOverlayState(OVERLAY)
                .createCompositeState(true));
    }
}
