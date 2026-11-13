package de.mccs.neoforge.client;

import de.mccs.mc.Mccs;
import de.mccs.mc.client.BeamClient;
import de.mccs.mc.client.ClientCompat;
import de.mccs.mc.client.KeyBindings;
import de.mccs.mc.client.StaffClient;
import de.mccs.mc.client.hud.StaffHud;
import de.mccs.mc.client.render.BubbleRenderer;
import de.mccs.mc.client.render.EnergyBallRenderer;
import de.mccs.mc.client.render.StaffItemRenderer;
import de.mccs.mc.entity.EnergyBallEntity;
import de.mccs.mc.network.BeamStatePacket;
import de.mccs.mc.network.StaffActionPacket;
import de.mccs.neoforge.MccsNeoForge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.ModelResourceLocation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;

/** MCCS's client on NeoForge: key, HUD, item renderer and events for the shared client code; the vanilla mixins are shared with Fabric. */
@Mod(value = Mccs.MOD_ID, dist = Dist.CLIENT)
public final class MccsNeoForgeClient {
    public MccsNeoForgeClient(IEventBus modBus) {
        BeamStatePacket.handler = BeamClient::update;
        StaffClient.actionSender = action -> PacketDistributor.sendToServer(new StaffActionPacket(action));
        StaffItemRenderer.models = id -> Minecraft.getInstance().getModelManager().getModel(ModelResourceLocation.standalone(id));
        modBus.addListener(ModelEvent.RegisterAdditional.class,
                event -> StaffItemRenderer.bodyModels().forEach(id -> event.register(ModelResourceLocation.standalone(id))));
        // the staffs' item models are "builtin/entity": Minecraft hands their drawing to this renderer
        modBus.addListener(RegisterClientExtensionsEvent.class, event -> event.registerItem(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return Renderer.INSTANCE;
            }
        }, MccsNeoForge.staffs()));
        modBus.addListener(EntityRenderersEvent.RegisterRenderers.class, event -> event.registerEntityRenderer(EnergyBallEntity.TYPE, EnergyBallRenderer::new));
        modBus.addListener(RegisterKeyMappingsEvent.class, event -> KeyBindings.ALL.forEach(event::register));
        modBus.addListener(RegisterGuiLayersEvent.class, event -> event.registerAbove(VanillaGuiLayers.HOTBAR, Mccs.id("staff_hud"),
                (graphics, deltaTracker) -> StaffHud.render(ClientCompat.hudCanvas(graphics), graphics.guiWidth(), graphics.guiHeight())));
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Pre.class, event -> StaffClient.onClientTickStart());
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, event -> StaffClient.onClientTickEnd());
        NeoForge.EVENT_BUS.addListener(RenderLevelStageEvent.class, event -> {
            if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_WEATHER) {
                BubbleRenderer.render(event.getPoseStack().last(), true, event.getCamera(), event.getPartialTick().getGameTimeDeltaPartialTick(false));
            }
        });
    }

    /** NeoForge's item renderer hook, handing over to the shared {@link StaffItemRenderer}; created on first use, when the game is ready. */
    private static final class Renderer extends BlockEntityWithoutLevelRenderer {
        static final Renderer INSTANCE = new Renderer();

        private Renderer() {
            super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
        }

        @Override
        public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
            StaffItemRenderer.render(stack, context, poseStack, buffers, light, overlay);
        }
    }
}
