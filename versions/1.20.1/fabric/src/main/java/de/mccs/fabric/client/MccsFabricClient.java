package de.mccs.fabric.client;

import de.mccs.core.staff.StaffKind;
import de.mccs.mc.Mccs;
import de.mccs.mc.client.render.StaffItemRenderer;
import net.fabricmc.fabric.api.client.model.BakedModelManagerHelper;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import de.mccs.fabric.config.FabricConfigs;
import de.mccs.fabric.network.FabricNetwork;
import de.mccs.mc.client.BeamClient;
import de.mccs.mc.client.ClientCompat;
import de.mccs.mc.client.KeyBindings;
import de.mccs.mc.client.StaffClient;
import de.mccs.mc.client.hud.StaffHud;
import de.mccs.mc.client.render.EnergyBallRenderer;
import de.mccs.mc.entity.EnergyBallEntity;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import de.mccs.mc.client.render.BubbleRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import de.mccs.mc.network.BeamStatePacket;
import de.mccs.mc.network.StaffActionPacket;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Map;

/** MCCS's client on Fabric: key, HUD and Fabric's client events for the shared client code. */
public final class MccsFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        StaffClient.actionSender = action -> {
            FriendlyByteBuf buffer = PacketByteBufs.create();
            StaffActionPacket.encode(new StaffActionPacket(action), buffer);
            ClientPlayNetworking.send(FabricNetwork.STAFF_ACTION, buffer);
        };
        EntityRendererRegistry.register(EnergyBallEntity.TYPE, EnergyBallRenderer::new);
        // the staffs' item models are "builtin/entity": Minecraft hands their drawing to StaffItemRenderer, which draws
        // the body model and the crystal itself
        for (StaffKind kind : StaffKind.values()) {
            BuiltinItemRendererRegistry.INSTANCE.register(BuiltInRegistries.ITEM.get(Mccs.id(kind.id())), StaffItemRenderer::render);
        }
        ModelLoadingPlugin.register(context -> context.addModels(StaffItemRenderer.bodyModels()));
        StaffItemRenderer.models = id -> BakedModelManagerHelper.getModel(Minecraft.getInstance().getModelManager(), id);
        KeyBindings.ALL.forEach(KeyBindingHelper::registerKeyBinding);
        ClientTickEvents.START_CLIENT_TICK.register(client -> StaffClient.onClientTickStart());
        ClientTickEvents.END_CLIENT_TICK.register(client -> StaffClient.onClientTickEnd());
        HudRenderCallback.EVENT.register((graphics, partialTick) ->
                StaffHud.render(ClientCompat.hudCanvas(graphics), graphics.guiWidth(), graphics.guiHeight()));
        // LAST comes after clouds and weather, when the camera rotation is already off the model-view stack
        WorldRenderEvents.LAST.register(context ->
                BubbleRenderer.render(context.matrixStack().last(), false, context.camera(), context.tickDelta()));
        ClientPlayNetworking.registerGlobalReceiver(FabricNetwork.BEAM_STATE, (client, handler, buffer, sender) -> {
            BeamStatePacket packet = BeamStatePacket.decode(buffer);
            client.execute(() -> BeamClient.update(packet));
        });
        ClientPlayNetworking.registerGlobalReceiver(FabricNetwork.SERVER_CONFIG, (client, handler, buffer, sender) -> {
            Map<String, String> values = FabricNetwork.decodeConfig(buffer);
            client.execute(() -> FabricConfigs.SERVER.apply(values));
        });
        // back to the own settings after leaving a server
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> client.execute(FabricConfigs.SERVER::load));
    }
}
