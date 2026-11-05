package de.mcqw.fabric.client;

import de.mcqw.fabric.config.FabricConfigs;
import de.mcqw.fabric.network.FabricNetwork;
import de.mcqw.mc.client.ClientCompat;
import de.mcqw.mc.client.KeyBindings;
import de.mcqw.mc.client.StaffClient;
import de.mcqw.mc.client.hud.StaffHud;
import de.mcqw.mc.client.render.ShieldRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;

import java.util.Map;

/** MCQW's client on Fabric: key, HUD and Fabric's client events for the shared client code. */
public final class McqwFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        StaffClient.toggleSender = () -> ClientPlayNetworking.send(FabricNetwork.TOGGLE_SHIELD, PacketByteBufs.empty());
        KeyBindingHelper.registerKeyBinding(KeyBindings.SHIELD);
        ClientTickEvents.END_CLIENT_TICK.register(client -> StaffClient.onClientTickEnd());
        HudRenderCallback.EVENT.register((graphics, partialTick) ->
                StaffHud.render(ClientCompat.hudCanvas(graphics), graphics.guiWidth(), graphics.guiHeight()));
        // LAST comes after clouds and weather, when the camera rotation is already off the model-view stack
        WorldRenderEvents.LAST.register(context ->
                ShieldRenderer.render(context.matrixStack().last(), false, context.camera(), context.tickDelta()));
        ClientPlayNetworking.registerGlobalReceiver(FabricNetwork.SERVER_CONFIG, (client, handler, buffer, sender) -> {
            Map<String, String> values = FabricNetwork.decodeConfig(buffer);
            client.execute(() -> FabricConfigs.SERVER.apply(values));
        });
        // back to the own settings after leaving a server
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> client.execute(FabricConfigs.SERVER::load));
    }
}
