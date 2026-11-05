package de.mcqw.mc.client.render;

import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import de.mcqw.mc.Mcqw;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.TextureTransform;
import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** MCQW's own render types (lightning, crystal light and shield bubble). */
final class ModRenderTypes {
    // All of them are additive: with "improved transparency" they use vanilla's OIT pipelines for additive surfaces.
    /** Fades additive colors out in fog instead of adding the fog color (assets/mcqw/shaders/core/additive.fsh). */
    private static final Identifier ADDITIVE_FRAGMENT_SHADER = Mcqw.id("core/additive");

    /**
     * Like the charged creeper swirl (additive, scrolling texture) but without depth writes,
     * so the bubble does not hide translucent blocks rendered later (ice, water, glass).
     */
    private static final RenderPipeline BUBBLE = RenderPipeline.builder(RenderPipelines.ENERGY_SWIRL_SNIPPET)
            .withLocation(Mcqw.id("pipeline/energy_bubble"))
            .withFragmentShader(ADDITIVE_FRAGMENT_SHADER)
            .withColorTargetState(new ColorTargetState(BlendFunction.ADDITIVE))
            .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false))
            .build();

    /** The same seen from inside, where culling would remove it. */
    private static final RenderPipeline BUBBLE_INSIDE = RenderPipeline.builder(RenderPipelines.ENERGY_SWIRL_SNIPPET)
            .withLocation(Mcqw.id("pipeline/energy_bubble_inside"))
            .withFragmentShader(ADDITIVE_FRAGMENT_SHADER)
            .withColorTargetState(new ColorTargetState(BlendFunction.ADDITIVE))
            .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false))
            .withCull(false)
            .build();

    /** The staff's lightning and the crystal's light: full bright and additive like vanilla's "eyes" up to 1.21, visible from both sides. */
    private static final RenderPipeline BEAM = RenderPipeline.builder(RenderPipelines.ENTITY_NO_LIGHTMAP_SNIPPET, RenderPipelines.EYES_SNIPPET)
            .withLocation(Mcqw.id("pipeline/beam"))
            .withFragmentShader(ADDITIVE_FRAGMENT_SHADER)
            .withColorTargetState(new ColorTargetState(BlendFunction.ADDITIVE))
            .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false))
            .withCull(false)
            .build();
    private static final Map<Identifier, RenderType> BEAM_TYPES = new ConcurrentHashMap<>();

    private ModRenderTypes() {}

    static RenderType beam(Identifier texture) {
        return BEAM_TYPES.computeIfAbsent(texture, t -> RenderType.create("mcqw_beam", RenderSetup.builder(BEAM)
                .setOitPipelines(RenderPipelines.OIT_ENERGY_SWIRL)
                .withTexture("Sampler0", t)
                .sortOnUpload()
                .createRenderSetup()));
    }

    static RenderType energyBubble(Identifier texture, float uOffset, float vOffset) {
        return energyBubble(BUBBLE, texture, uOffset, vOffset);
    }

    static RenderType energyBubbleInside(Identifier texture, float uOffset, float vOffset) {
        return energyBubble(BUBBLE_INSIDE, texture, uOffset, vOffset);
    }

    private static RenderType energyBubble(RenderPipeline pipeline, Identifier texture, float uOffset, float vOffset) {
        return RenderType.create("mcqw_bubble", RenderSetup.builder(pipeline)
                .setOitPipelines(RenderPipelines.OIT_ENERGY_SWIRL)
                .withTexture("Sampler0", texture)
                .setTextureTransform(new TextureTransform.OffsetTextureTransform(uOffset, vOffset))
                .useLightmap()
                .sortOnUpload()
                .createRenderSetup());
    }
}
