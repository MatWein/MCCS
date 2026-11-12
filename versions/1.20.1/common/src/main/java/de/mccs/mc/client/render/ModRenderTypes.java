package de.mccs.mc.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/** Subclass only to reach the protected render state shards. */
final class ModRenderTypes extends RenderType {
    private static final int BUFFER_SIZE = 256;

    private ModRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean affectsCrumbling,
                           boolean sortOnUpload, Runnable setup, Runnable clear) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setup, clear);
    }

    /**
     * Like the charged creeper swirl (additive, scrolling texture) but without depth writes,
     * so the bubble does not hide translucent blocks rendered later (ice, water, glass).
     */
    static RenderType energyBubble(ResourceLocation texture, float uOffset, float vOffset) {
        return energyBubble(texture, uOffset, vOffset, CULL);
    }

    /** The staff's lightning and the crystal's arcs: full bright, additive and visible from both sides. */
    static RenderType beam(ResourceLocation texture) {
        return create("mccs_beam", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, BUFFER_SIZE, false, true,
                RenderType.CompositeState.builder()
                        .setShaderState(RENDERTYPE_EYES_SHADER)
                        .setTextureState(new TextureStateShard(texture, false, false))
                        .setTransparencyState(ADDITIVE_TRANSPARENCY)
                        .setWriteMaskState(COLOR_WRITE)
                        .setCullState(NO_CULL)
                        .createCompositeState(false));
    }

    /**
     * The staff's crystal: shines on its own like vanilla's emissive entity parts (no light map) but is opaque and writes
     * depth, so neither the staff behind it nor water drawn later shows through.
     */
    static RenderType crystal(ResourceLocation texture) {
        return create("mccs_crystal", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, BUFFER_SIZE, true, false,
                RenderType.CompositeState.builder()
                        .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER)
                        .setTextureState(new TextureStateShard(texture, false, false))
                        .setTransparencyState(NO_TRANSPARENCY)
                        .setCullState(NO_CULL)
                        .setWriteMaskState(COLOR_DEPTH_WRITE)
                        .setOverlayState(OVERLAY)
                        .createCompositeState(true));
    }

    /** The crystal's light: like the lightning, but the soft glow texture is filtered smoothly instead of showing its pixels. */
    static RenderType glow(ResourceLocation texture) {
        return create("mccs_glow", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, BUFFER_SIZE, false, true,
                RenderType.CompositeState.builder()
                        .setShaderState(RENDERTYPE_EYES_SHADER)
                        .setTextureState(new TextureStateShard(texture, true, false))
                        .setTransparencyState(ADDITIVE_TRANSPARENCY)
                        .setWriteMaskState(COLOR_WRITE)
                        .setCullState(NO_CULL)
                        .createCompositeState(false));
    }

    /** The same seen from inside, where culling would remove it. */
    static RenderType energyBubbleInside(ResourceLocation texture, float uOffset, float vOffset) {
        return energyBubble(texture, uOffset, vOffset, NO_CULL);
    }

    private static RenderType energyBubble(ResourceLocation texture, float uOffset, float vOffset, CullStateShard cull) {
        return create("mccs_bubble", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, BUFFER_SIZE, false, true,
                RenderType.CompositeState.builder()
                        .setShaderState(RENDERTYPE_ENERGY_SWIRL_SHADER)
                        .setTextureState(new TextureStateShard(texture, false, false))
                        .setTexturingState(new OffsetTexturingStateShard(uOffset, vOffset))
                        .setTransparencyState(ADDITIVE_TRANSPARENCY)
                        .setWriteMaskState(COLOR_WRITE)
                        .setLightmapState(LIGHTMAP)
                        .setOverlayState(OVERLAY)
                        .setCullState(cull)
                        .createCompositeState(false));
    }
}
