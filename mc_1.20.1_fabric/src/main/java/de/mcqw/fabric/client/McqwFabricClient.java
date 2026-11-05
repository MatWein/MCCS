package de.mcqw.fabric.client;

import de.mcqw.fabric.config.FabricConfigs;
import de.mcqw.fabric.network.FabricNetwork;
import de.mcqw.mc.client.BeamClient;
import de.mcqw.mc.client.ClientCompat;
import de.mcqw.mc.client.KeyBindings;
import de.mcqw.mc.client.StaffClient;
import de.mcqw.mc.client.hud.StaffHud;
import de.mcqw.mc.client.render.BubbleRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import de.mcqw.mc.network.BeamStatePacket;
import de.mcqw.mc.network.StaffActionPacket;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Map;

/** MCQW's client on Fabric: key, HUD and Fabric's client events for the shared client code. */
public final class McqwFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        StaffClient.actionSender = action -> {
            FriendlyByteBuf buffer = PacketByteBufs.create();
            StaffActionPacket.encode(new StaffActionPacket(action), buffer);
            ClientPlayNetworking.send(FabricNetwork.STAFF_ACTION, buffer);
        };
        KeyBindingHelper.registerKeyBinding(KeyBindings.SHIELD);
        KeyBindingHelper.registerKeyBinding(KeyBindings.CLOAK);
        ClientTickEvents.END_CLIENT_TICK.register(client -> StaffClient.onClientTickEnd());
        HudRenderCallback.EVENT.register((graphics, partialTick) ->
                StaffHud.render(ClientCompat.hudCanvas(graphics), graphics.guiWidth(), graphics.guiHeight()));
        // LAST comes after clouds and weather, when the camera rotation is already off the model-view stack
        WorldRenderEvents.LAST.register(context ->
                BubbleRenderer.render(context.matrixStack().last(), false, context.camera(), context.tickDelta()));
        ClientPlayNetworking.registerGlobalReceiver(FabricNetwork.BEAM_STATE, (client, handler, buffer, sender) -> {
            BeamStatePacket packet = BeamStatePacket.decode(buffer);
            client.execute(() -> BeamClient.update(packet.shooter(), packet.firing(), packet.targets()));
        });
        ClientPlayNetworking.registerGlobalReceiver(FabricNetwork.SERVER_CONFIG, (client, handler, buffer, sender) -> {
            Map<String, String> values = FabricNetwork.decodeConfig(buffer);
            client.execute(() -> FabricConfigs.SERVER.apply(values));
        });
        // back to the own settings after leaving a server
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> client.execute(FabricConfigs.SERVER::load));
    }
}
