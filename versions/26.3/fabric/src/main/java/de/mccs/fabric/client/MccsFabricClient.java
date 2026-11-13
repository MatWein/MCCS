package de.mccs.fabric.client;

import de.mccs.mc.client.render.StaffCrystalRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderers;
import de.mccs.fabric.config.FabricConfigs;
import de.mccs.fabric.network.FabricNetwork;
import de.mccs.mc.Mccs;
import de.mccs.mc.client.BeamClient;
import de.mccs.mc.client.ClientCompat;
import de.mccs.mc.client.KeyBindings;
import de.mccs.mc.client.StaffClient;
import de.mccs.mc.client.hud.StaffHud;
import de.mccs.mc.client.render.EnergyBallRenderer;
import de.mccs.mc.entity.EnergyBallEntity;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import de.mccs.mc.network.BeamStatePacket;
import de.mccs.mc.network.StaffActionPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;

/** MCCS's client on Fabric: key, HUD and ticks for the shared client code; the bubble is a vanilla mixin shared with NeoForge. */
public final class MccsFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // the crystal and its light, a special item model in items/<staff>.json (accessible through the class tweaker)
        SpecialModelRenderers.ID_MAPPER.put(StaffCrystalRenderer.ID, StaffCrystalRenderer.Unbaked.MAP_CODEC);
        StaffClient.actionSender = action -> ClientPlayNetworking.send(new StaffActionPacket(action));
        EntityRendererRegistry.register(EnergyBallEntity.TYPE, EnergyBallRenderer::new);
        KeyBindings.ALL.forEach(KeyMappingHelper::registerKeyMapping);
        ClientTickEvents.START_CLIENT_TICK.register(client -> StaffClient.onClientTickStart());
        ClientTickEvents.END_CLIENT_TICK.register(client -> StaffClient.onClientTickEnd());
        HudElementRegistry.addLast(Mccs.id("staff_hud"), (graphics, deltaTracker) ->
                StaffHud.render(ClientCompat.hudCanvas(graphics), graphics.guiWidth(), graphics.guiHeight()));
        // handlers run on the client thread
        ClientPlayNetworking.registerGlobalReceiver(BeamStatePacket.TYPE, (packet, context) -> BeamClient.update(packet));
        ClientPlayNetworking.registerGlobalReceiver(FabricNetwork.ServerConfigPacket.TYPE,
                (packet, context) -> FabricConfigs.SERVER.apply(packet.values()));
        // back to the own settings after leaving a server
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> client.execute(FabricConfigs.SERVER::load));
    }
}
