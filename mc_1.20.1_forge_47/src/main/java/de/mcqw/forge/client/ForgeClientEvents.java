package de.mcqw.forge.client;

import de.mcqw.mc.Mcqw;
import de.mcqw.mc.client.StaffClient;
import de.mcqw.mc.client.render.ShieldRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Forge's client events for the shared client code. */
@Mod.EventBusSubscriber(modid = Mcqw.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ForgeClientEvents {
    private ForgeClientEvents() {}

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) StaffClient.onClientTickEnd();
    }

    @SubscribeEvent
    public static void onRenderStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_WEATHER) {
            ShieldRenderer.render(event.getPoseStack().last(), true, event.getCamera(), event.getPartialTick());
        }
    }
}
