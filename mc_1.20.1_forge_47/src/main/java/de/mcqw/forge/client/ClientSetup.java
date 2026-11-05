package de.mcqw.forge.client;

import de.mcqw.forge.network.ModNetwork;
import de.mcqw.mc.Mcqw;
import de.mcqw.mc.client.BeamClient;
import de.mcqw.mc.client.ClientCompat;
import de.mcqw.mc.client.KeyBindings;
import de.mcqw.mc.client.StaffClient;
import de.mcqw.mc.client.hud.StaffHud;
import de.mcqw.mc.network.BeamStatePacket;
import de.mcqw.mc.network.StaffActionPacket;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/** Client registration on the mod event bus. */
@Mod.EventBusSubscriber(modid = Mcqw.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ClientSetup {
    private ClientSetup() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        BeamStatePacket.handler = packet -> BeamClient.update(packet.shooter(), packet.firing(), packet.targets());
        StaffClient.actionSender = action -> ModNetwork.sendToServer(new StaffActionPacket(action));
    }

    @SubscribeEvent
    public static void onRegisterKeys(RegisterKeyMappingsEvent event) {
        event.register(KeyBindings.SHIELD);
        event.register(KeyBindings.CLOAK);
    }

    @SubscribeEvent
    public static void onRegisterOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "staff_hud",
                (gui, graphics, partialTick, width, height) -> StaffHud.render(ClientCompat.hudCanvas(graphics), width, height));
    }
}
