package de.mccs.mc.client.hud;

import de.mccs.core.config.ServerConfig;
import de.mccs.core.config.StaffConfig;
import de.mccs.core.hud.HudColors;
import de.mccs.core.hud.HudLine;
import de.mccs.core.hud.HudText;
import de.mccs.core.hud.StaffHudModel;
import de.mccs.mc.client.BeamClient;
import de.mccs.mc.client.ClientCompat;
import de.mccs.mc.staff.StaffData;
import de.mccs.mc.staff.StaffSystems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Draws the core's HUD lines ({@link StaffHudModel}) bottom left, in the look of MCPJ's cockpit HUD. */
public final class StaffHud {
    private static final int MARGIN_LEFT = 8;
    private static final int MARGIN_BOTTOM = 12;
    private static final int LINE_HEIGHT = 11;
    private static final int PANEL_PADDING = 4;
    private static final int ACCENT_WIDTH = 2;

    private StaffHud() {}

    public static void render(HudCanvas canvas, int width, int height) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || ClientCompat.hudHidden(minecraft)) return;
        ItemStack shield = StaffSystems.activeShield(player);
        ItemStack staff = shield.isEmpty() ? StaffSystems.heldStaff(player) : shield;
        if (staff.isEmpty()) return;

        StaffConfig config = ServerConfig.STAFF;
        boolean shieldOn = !shield.isEmpty();
        boolean cloakOn = !StaffSystems.activeCloak(player).isEmpty();
        int drain = config.consumeEnergy() ? StaffSystems.drain(player, false) + BeamClient.drain(player) : 0;
        List<HudLine> lines = StaffHudModel.lines(new StaffHudModel.State(StaffSystems.stored(staff), config.capacity(), shieldOn, cloakOn,
                !StaffSystems.activeLights(player).isEmpty(), StaffData.blockDamage(staff), drain, config.consumeEnergy()), player.tickCount);

        Font font = minecraft.font;
        List<Component> texts = new ArrayList<>();
        int textWidth = 0;
        for (HudLine line : lines) {
            Component text = component(line);
            texts.add(text);
            textWidth = Math.max(textWidth, font.width(text));
        }
        int top = height - MARGIN_BOTTOM - lines.size() * LINE_HEIGHT;
        int bottom = top + lines.size() * LINE_HEIGHT;
        canvas.fill(MARGIN_LEFT - PANEL_PADDING, top - PANEL_PADDING, MARGIN_LEFT + textWidth + PANEL_PADDING, bottom, HudColors.PANEL_BACKGROUND);
        canvas.fill(MARGIN_LEFT - PANEL_PADDING, top - PANEL_PADDING, MARGIN_LEFT - PANEL_PADDING + ACCENT_WIDTH, bottom, HudColors.PANEL_ACCENT);
        for (int i = 0; i < lines.size(); i++) {
            canvas.text(font, texts.get(i), MARGIN_LEFT, top + i * LINE_HEIGHT, lines.get(i).color());
        }
    }

    private static Component component(HudLine line) {
        MutableComponent text = Component.empty();
        line.parts().forEach(part -> text.append(component(part)));
        return text;
    }

    private static Component component(HudText text) {
        if (text instanceof HudText.Translated translated) {
            return Component.translatable(translated.key(), Arrays.stream(translated.args()).map(StaffHud::component).toArray());
        }
        if (text instanceof HudText.Name name) return Component.literal(String.valueOf(name.source()));
        return Component.literal(((HudText.Literal) text).text());
    }
}
