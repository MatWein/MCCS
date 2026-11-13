package de.mccs.fabric.client;

import de.mccs.core.staff.StaffKind;
import de.mccs.fabric.config.FabricConfigs;
import de.mccs.fabric.network.FabricNetwork;
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
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;

/** MCCS's client on Fabric: key, HUD and Fabric's client events for the shared client code; the vanilla mixins are shared with NeoForge. */
public final class MccsFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        StaffClient.actionSender = action -> ClientPlayNetworking.send(new StaffActionPacket(action));
        EntityRendererRegistry.register(EnergyBallEntity.TYPE, EnergyBallRenderer::new);
        // the staffs' item models are "builtin/entity": Minecraft hands their drawing to StaffItemRenderer, which draws
        // the body model and the crystal itself
        for (StaffKind kind : StaffKind.values()) {
            BuiltinItemRendererRegistry.INSTANCE.register(BuiltInRegistries.ITEM.get(Mccs.id(kind.id())), StaffItemRenderer::render);
        }
        ModelLoadingPlugin.register(context -> context.addModels(StaffItemRenderer.bodyModels()));
        StaffItemRenderer.models = id -> Minecraft.getInstance().getModelManager().getModel(id);
        KeyBindings.ALL.forEach(KeyBindingHelper::registerKeyBinding);
        ClientTickEvents.START_CLIENT_TICK.register(client -> StaffClient.onClientTickStart());
        ClientTickEvents.END_CLIENT_TICK.register(client -> StaffClient.onClientTickEnd());
        HudRenderCallback.EVENT.register((graphics, deltaTracker) ->
                StaffHud.render(ClientCompat.hudCanvas(graphics), graphics.guiWidth(), graphics.guiHeight()));
        WorldRenderEvents.LAST.register(context -> BubbleRenderer.render(context.matrixStack().last(), true, context.camera(),
                context.tickCounter().getGameTimeDeltaPartialTick(false)));
        // handlers run on the client thread
        ClientPlayNetworking.registerGlobalReceiver(BeamStatePacket.TYPE, (packet, context) -> BeamClient.update(packet));
        ClientPlayNetworking.registerGlobalReceiver(FabricNetwork.ServerConfigPacket.TYPE,
                (packet, context) -> FabricConfigs.SERVER.apply(packet.values()));
        // back to the own settings after leaving a server
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> client.execute(FabricConfigs.SERVER::load));
    }
}
