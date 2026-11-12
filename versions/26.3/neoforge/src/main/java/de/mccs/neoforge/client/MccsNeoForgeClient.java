package de.mccs.neoforge.client;

import de.mccs.mc.Mccs;
import de.mccs.mc.client.BeamClient;
import de.mccs.mc.client.ClientCompat;
import de.mccs.mc.client.KeyBindings;
import de.mccs.mc.client.StaffClient;
import de.mccs.mc.client.hud.StaffHud;
import de.mccs.mc.client.render.EnergyBallRenderer;
import de.mccs.mc.entity.EnergyBallEntity;
import de.mccs.mc.network.BeamStatePacket;
import de.mccs.mc.network.StaffActionPacket;
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

/** MCCS's client on NeoForge: key, HUD and ticks for the shared client code; the bubble is a vanilla mixin shared with Fabric. */
@Mod(value = Mccs.MOD_ID, dist = Dist.CLIENT)
public final class MccsNeoForgeClient {
    public MccsNeoForgeClient(IEventBus modBus) {
        BeamStatePacket.handler = packet -> BeamClient.update(packet.shooter(), packet.firing(), packet.tether(), packet.targets());
        StaffClient.actionSender = action -> ClientPacketDistributor.sendToServer(new StaffActionPacket(action));
        modBus.addListener(EntityRenderersEvent.RegisterRenderers.class, event -> event.registerEntityRenderer(EnergyBallEntity.TYPE, EnergyBallRenderer::new));
        modBus.addListener(RegisterKeyMappingsEvent.class, event -> {
            event.registerCategory(KeyBindings.CATEGORY);
            event.register(KeyBindings.SHIELD);
            event.register(KeyBindings.CLOAK);
            event.register(KeyBindings.TELEPORT);
            event.register(KeyBindings.LIGHTS);
            event.register(KeyBindings.BLOCK_DAMAGE);
            event.register(KeyBindings.TELEKINESIS);
        });
        modBus.addListener(RegisterGuiLayersEvent.class, event -> event.registerAbove(VanillaGuiLayers.HOTBAR, Mccs.id("staff_hud"),
                (graphics, deltaTracker) -> StaffHud.render(ClientCompat.hudCanvas(graphics), graphics.guiWidth(), graphics.guiHeight())));
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Pre.class, event -> StaffClient.onClientTickStart());
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, event -> StaffClient.onClientTickEnd());
    }
}
