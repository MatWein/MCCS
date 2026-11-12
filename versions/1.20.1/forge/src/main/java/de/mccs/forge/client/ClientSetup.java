package de.mccs.forge.client;

import de.mccs.forge.network.ModNetwork;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/** Client registration on the mod event bus. */
@Mod.EventBusSubscriber(modid = Mccs.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ClientSetup {
    private ClientSetup() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        BeamStatePacket.handler = packet -> BeamClient.update(packet.shooter(), packet.firing(), packet.tether(), packet.targets());
        StaffClient.actionSender = action -> ModNetwork.sendToServer(new StaffActionPacket(action));
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EnergyBallEntity.TYPE, EnergyBallRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterKeys(RegisterKeyMappingsEvent event) {
        event.register(KeyBindings.SHIELD);
        event.register(KeyBindings.CLOAK);
        event.register(KeyBindings.TELEPORT);
        event.register(KeyBindings.LIGHTS);
        event.register(KeyBindings.BLOCK_DAMAGE);
        event.register(KeyBindings.TELEKINESIS);
    }

    @SubscribeEvent
    public static void onRegisterOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "staff_hud",
                (gui, graphics, partialTick, width, height) -> StaffHud.render(ClientCompat.hudCanvas(graphics), width, height));
    }
}
