package de.mcqw.forge.client;

import de.mcqw.mc.Mcqw;
import de.mcqw.mc.client.BeamClient;
import de.mcqw.mc.client.StaffClient;
import de.mcqw.mc.client.render.BubbleRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
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

    /** The attack key fires the lightning (on the other platforms this is a mixin). */
    @SubscribeEvent
    public static void onInteraction(InputEvent.InteractionKeyMappingTriggered event) {
        if (event.isAttack() && BeamClient.takesAttackKey()) {
            event.setCanceled(true);
            event.setSwingHand(false);
        }
    }

    /** A cloaked player is completely invisible, to others and in third person to themselves; only the bubble is left. */
    @SubscribeEvent
    public static void onRenderLiving(RenderLivingEvent.Pre<?, ?> event) {
        if (event.getEntity() instanceof Player player && StaffClient.isHidden(player)) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        if (StaffClient.hidesOwnHand()) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onRenderStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_WEATHER) {
            BubbleRenderer.render(event.getPoseStack().last(), true, event.getCamera(), event.getPartialTick());
        }
    }
}
