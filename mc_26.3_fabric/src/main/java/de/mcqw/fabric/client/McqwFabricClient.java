package de.mcqw.fabric.client;

import de.mcqw.fabric.config.FabricConfigs;
import de.mcqw.fabric.network.FabricNetwork;
import de.mcqw.mc.Mcqw;
import de.mcqw.mc.client.BeamClient;
import de.mcqw.mc.client.ClientCompat;
import de.mcqw.mc.client.KeyBindings;
import de.mcqw.mc.client.StaffClient;
import de.mcqw.mc.client.hud.StaffHud;
import de.mcqw.mc.client.render.EnergyBallRenderer;
import de.mcqw.mc.entity.EnergyBallEntity;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import de.mcqw.mc.network.BeamStatePacket;
import de.mcqw.mc.network.StaffActionPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;

/** MCQW's client on Fabric: key, HUD and ticks for the shared client code; the bubble is a vanilla mixin shared with NeoForge. */
public final class McqwFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        StaffClient.actionSender = action -> ClientPlayNetworking.send(new StaffActionPacket(action));
        EntityRendererRegistry.register(EnergyBallEntity.TYPE, EnergyBallRenderer::new);
        KeyMappingHelper.registerKeyMapping(KeyBindings.SHIELD);
        KeyMappingHelper.registerKeyMapping(KeyBindings.CLOAK);
        ClientTickEvents.END_CLIENT_TICK.register(client -> StaffClient.onClientTickEnd());
        HudElementRegistry.addLast(Mcqw.id("staff_hud"), (graphics, deltaTracker) ->
                StaffHud.render(ClientCompat.hudCanvas(graphics), graphics.guiWidth(), graphics.guiHeight()));
        // handlers run on the client thread
        ClientPlayNetworking.registerGlobalReceiver(BeamStatePacket.TYPE,
                (packet, context) -> BeamClient.update(packet.shooter(), packet.firing(), packet.targets()));
        ClientPlayNetworking.registerGlobalReceiver(FabricNetwork.ServerConfigPacket.TYPE,
                (packet, context) -> FabricConfigs.SERVER.apply(packet.values()));
        // back to the own settings after leaving a server
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> client.execute(FabricConfigs.SERVER::load));
    }
}
