package de.mcqw.mc.client;

import de.mcqw.mc.client.hud.HudCanvas;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/** Client calls that differ between Minecraft versions, for the shared client code. */
public final class ClientCompat {
    private ClientCompat() {}

    public static boolean hudHidden(Minecraft minecraft) {
        return minecraft.options.hideGui;
    }

    public static HudCanvas hudCanvas(GuiGraphics graphics) {
        return new HudCanvas() {
            @Override
            public void fill(int x1, int y1, int x2, int y2, int argb) {
                graphics.fill(x1, y1, x2, y2, argb);
            }

            @Override
            public void text(Font font, Component text, int x, int y, int rgb) {
                graphics.drawString(font, text, x, y, rgb, true);
            }
        };
    }
}
