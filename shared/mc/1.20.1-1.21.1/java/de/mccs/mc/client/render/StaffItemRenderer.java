package de.mccs.mc.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.mccs.core.render.CrystalRender;
import de.mccs.core.render.VertexSink;
import de.mccs.core.staff.StaffKind;
import de.mccs.mc.Mccs;
import de.mccs.mc.client.ClientCompat;
import de.mccs.mc.client.StaffEffects;
import de.mccs.mc.staff.StaffSystems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Draws the staff item (Minecraft up to 1.21.1). Its item model is "builtin/entity" with the staff's display transforms, so
 * Minecraft hands the drawing over to this class with the item's pose already applied: the staff's body model, the crystal
 * and, in the hands, its light ({@link StaffEffects}, {@link CrystalRender}), which therefore sits exactly on it in every
 * view. The loaders call this from their item renderer hook and supply the body models.
 */
public final class StaffItemRenderer {
    private static final RenderType CRYSTAL = ModRenderTypes.crystal(Mccs.id("textures/entity/crystal.png"));
    private static final RenderType GLOW = ModRenderTypes.glow(Mccs.id("textures/entity/crystal_glow.png"));
    private static final RenderType GLINT = ModRenderTypes.glow(Mccs.id("textures/entity/glint.png"));
    private static final RenderType BEAM = ModRenderTypes.beam(Mccs.id("textures/entity/beam.png"));
    private static final Direction[] QUAD_SIDES = {Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, null};
    private static final RandomSource RANDOM = RandomSource.create();

    /** The baked body models by id; set by the loader, which also registers {@link #bodyModels()} for baking. */
    public static Function<ResourceLocation, BakedModel> models = id -> Minecraft.getInstance().getModelManager().getMissingModel();

    private StaffItemRenderer() {}

    /** The body models of all staffs (models/item/{@code <kind>_body}), to be loaded besides the item models. */
    public static List<ResourceLocation> bodyModels() {
        List<ResourceLocation> ids = new ArrayList<>();
        for (StaffKind kind : StaffKind.values()) ids.add(bodyModel(kind));
        return ids;
    }

    private static ResourceLocation bodyModel(StaffKind kind) {
        return Mccs.id("item/" + kind.id() + "_body");
    }

    /** Draws a staff stack; the pose stack holds the item's display transform already. */
    public static void render(ItemStack stack, ItemDisplayContext context, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
        BakedModel model = models.apply(bodyModel(StaffSystems.kind(stack)));
        VertexConsumer consumer = buffers.getBuffer(ItemBlockRenderTypes.getRenderType(stack, true));
        PoseStack.Pose pose = poseStack.last();
        for (Direction side : QUAD_SIDES) {
            RANDOM.setSeed(42L);
            for (BakedQuad quad : model.getQuads(null, side, RANDOM)) {
                RenderSinks.quad(consumer, pose, quad, light, overlay);
            }
        }
        StaffEffects.Look look = StaffEffects.look(stack);
        Drawing drawing = new Drawing(pose, buffers);
        if (inHand(context)) {
            float[][] axes = RenderSinks.cameraAxes();
            StaffEffects.draw(look, pose.pose(), axes[0], axes[1], time(), context.firstPerson(), drawing);
        } else {
            StaffEffects.drawCrystal(look, time(), drawing);
        }
    }

    private static float time() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.level == null ? 0f : minecraft.level.getGameTime() + ClientCompat.partialTick(minecraft);
    }

    private static boolean inHand(ItemDisplayContext context) {
        return context.firstPerson() || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
    }

    /**
     * Draws what {@link StaffEffects} hands over: the crystal's body with the item's pose, its light in render space
     * (without the item's pose). Each render type is drawn as soon as the next one is asked for, so the order is kept.
     */
    private record Drawing(PoseStack.Pose itemPose, MultiBufferSource buffers, PoseStack.Pose renderSpace) implements StaffEffects.Output {
        Drawing(PoseStack.Pose itemPose, MultiBufferSource buffers) {
            this(itemPose, buffers, new PoseStack().last());
        }

        private VertexSink sink(PoseStack.Pose pose, RenderType type) {
            return RenderSinks.sink(pose, buffers.getBuffer(type), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        }

        @Override
        public void body(Consumer<VertexSink> emitter) {
            emitter.accept(sink(itemPose, CRYSTAL));
        }

        @Override
        public void light(CrystalRender.Light light, Consumer<VertexSink> emitter) {
            emitter.accept(sink(renderSpace, switch (light) {
                case GLOW -> GLOW;
                case GLINT -> GLINT;
                case BEAM -> BEAM;
            }));
        }

        @Override
        public void orb(float[] center, float size, float charge, float orbTime) {
            PoseStack orbPose = new PoseStack();
            orbPose.translate(center[0], center[1], center[2]);
            orbPose.scale(size, size, size);
            EnergyBallRenderer.drawOrb(orbPose.last(), buffers, orbTime, charge);
        }
    }
}
