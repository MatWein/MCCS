package de.mcqw.neoforge.client;

import de.mcqw.mc.Mcqw;
import de.mcqw.mc.client.BeamClient;
import de.mcqw.mc.client.ClientCompat;
import de.mcqw.mc.client.KeyBindings;
import de.mcqw.mc.client.StaffClient;
import de.mcqw.mc.client.hud.StaffHud;
import de.mcqw.mc.client.render.EnergyBallRenderer;
import de.mcqw.mc.entity.EnergyBallEntity;
import de.mcqw.mc.network.BeamStatePacket;
import de.mcqw.mc.network.StaffActionPacket;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.common.NeoForge;

/** MCQW's client on NeoForge: key, HUD and ticks for the shared client code; the bubble is a vanilla mixin shared with Fabric. */
@Mod(value = Mcqw.MOD_ID, dist = Dist.CLIENT)
public final class McqwNeoForgeClient {
    public McqwNeoForgeClient(IEventBus modBus) {
        BeamStatePacket.handler = packet -> BeamClient.update(packet.shooter(), packet.firing(), packet.targets());
        StaffClient.actionSender = action -> ClientPacketDistributor.sendToServer(new StaffActionPacket(action));
        modBus.addListener(EntityRenderersEvent.RegisterRenderers.class, event -> event.registerEntityRenderer(EnergyBallEntity.TYPE, EnergyBallRenderer::new));
        modBus.addListener(RegisterKeyMappingsEvent.class, event -> {
            event.registerCategory(KeyBindings.CATEGORY);
            event.register(KeyBindings.SHIELD);
            event.register(KeyBindings.CLOAK);
        });
        modBus.addListener(RegisterGuiLayersEvent.class, event -> event.registerAbove(VanillaGuiLayers.HOTBAR, Mcqw.id("staff_hud"),
                (graphics, deltaTracker) -> StaffHud.render(ClientCompat.hudCanvas(graphics), graphics.guiWidth(), graphics.guiHeight())));
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, event -> StaffClient.onClientTickEnd());
    }
}
