package de.mccs.mc.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.mccs.core.render.CrystalRender;
import de.mccs.core.render.VertexSink;
import de.mccs.mc.Mccs;
import de.mccs.mc.client.ClientCompat;
import de.mccs.mc.client.StaffEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

/**
 * The staff's crystal as a special item model ("mccs:crystal" in items/<staff>.json): the crystal and, with
 * {@code effects} (the hands), its light ({@link StaffEffects}, {@link CrystalRender}). Minecraft calls
 * it while it draws the staff, with the item's pose, so all of it sits exactly on the staff in every view.
 */
public final class StaffCrystalRenderer implements SpecialModelRenderer<StaffEffects.Look> {
    public static final Identifier ID = Mccs.id("crystal");
    private static final RenderType CRYSTAL = ModRenderTypes.crystal(Mccs.id("textures/entity/crystal.png"));
    private static final RenderType GLOW = ModRenderTypes.beam(Mccs.id("textures/entity/crystal_glow.png"));
    private static final RenderType GLINT = ModRenderTypes.beam(Mccs.id("textures/entity/glint.png"));
    private static final RenderType BEAM = ModRenderTypes.beam(Mccs.id("textures/entity/beam.png"));
    // the crystal, for the item's bounds
    private static final float CRYSTAL_RADIUS = 3.2f / 16;
    private static final float CRYSTAL_BOTTOM = 16.9f / 16;
    private static final float CRYSTAL_TOP = 23.2f / 16;

    private final boolean effects;

    private StaffCrystalRenderer(boolean effects) {
        this.effects = effects;
    }

    @Override
    public void submit(StaffEffects.@Nullable Look look, PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, int overlayCoords,
                       boolean hasFoil, int outlineColor) {
        if (look == null) return;
        Minecraft minecraft = Minecraft.getInstance();
        float time = minecraft.level == null ? 0f : minecraft.level.getGameTime() + minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        Drawing drawing = new Drawing(poseStack, collector);
        if (!effects) {
            StaffEffects.drawCrystal(look, time, drawing);
            return;
        }
        Matrix4f itemPose = new Matrix4f(poseStack.last().pose());
        // Minecraft 26.x keeps the camera rotation out of the pose stack, in the world as in the first person hand: render
        // space has the world's axes, and the camera's right and up come from the camera
        float[][] basis = ClientCompat.cameraBasis(minecraft.gameRenderer.mainCamera());
        float[] right = {-basis[2][0], -basis[2][1], -basis[2][2]};
        StaffEffects.draw(look, itemPose, right, basis[1], time, drawing);
    }

    /**
     * Submits what {@link StaffEffects} hands over: the crystal's body with the item's pose, its additive light in render
     * space (without the item's pose), after water and clouds ({@link AfterWater}).
     */
    private record Drawing(PoseStack itemPose, SubmitNodeCollector collector, PoseStack renderSpace) implements StaffEffects.Output {
        Drawing(PoseStack itemPose, SubmitNodeCollector collector) {
            this(itemPose, collector, new PoseStack());
        }

        @Override
        public void body(Consumer<VertexSink> emitter) {
            collector.submitCustomGeometry(itemPose, CRYSTAL, (pose, buffer) -> emitter.accept(sink(pose, buffer)));
        }

        @Override
        public void light(CrystalRender.Light light, Consumer<VertexSink> emitter) {
            RenderType type = switch (light) {
                case GLOW -> GLOW;
                case GLINT -> GLINT;
                case BEAM -> BEAM;
            };
            AfterWater.submit(renderSpace, collector, type, (pose, buffer) -> emitter.accept(sink(pose, buffer)));
        }

        @Override
        public void orb(float[] center, float size, float charge, float orbTime) {
            PoseStack orbPose = new PoseStack();
            orbPose.translate(center[0], center[1], center[2]);
            orbPose.scale(size, size, size);
            EnergyBallRenderer.drawOrb(orbPose, collector, orbTime, charge);
        }
    }

    private static VertexSink sink(PoseStack.Pose pose, VertexConsumer buffer) {
        return RenderSinks.sink(pose, buffer, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        for (float y : new float[]{CRYSTAL_BOTTOM, CRYSTAL_TOP}) {
            output.accept(new Vector3f(0.5f - CRYSTAL_RADIUS, y, 0.5f - CRYSTAL_RADIUS));
            output.accept(new Vector3f(0.5f + CRYSTAL_RADIUS, y, 0.5f + CRYSTAL_RADIUS));
        }
    }

    @Override
    public StaffEffects.Look extractArgument(ItemStack stack) {
        return StaffEffects.look(stack);
    }

    /** {@code effects}: whether the crystal's light is shown as well (in the hands). */
    public record Unbaked(boolean effects) implements SpecialModelRenderer.Unbaked<StaffEffects.Look> {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Codec.BOOL.optionalFieldOf("effects", false).forGetter(Unbaked::effects)
        ).apply(i, Unbaked::new));

        @Override
        public SpecialModelRenderer<StaffEffects.Look> bake(SpecialModelRenderer.BakingContext context) {
            return new StaffCrystalRenderer(effects);
        }

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
