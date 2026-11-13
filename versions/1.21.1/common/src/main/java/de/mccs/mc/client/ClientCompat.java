package de.mccs.mc.client;

import de.mccs.mc.client.hud.HudCanvas;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.joml.Vector3f;

/** Client calls that differ between Minecraft versions, for the shared client code. */
public final class ClientCompat {
    private ClientCompat() {}

    public static boolean screenOpen(Minecraft minecraft) {
        return minecraft.screen != null;
    }

    /** How far the current frame is between two game ticks. */
    public static float partialTick(Minecraft minecraft) {
        return minecraft.getTimer().getGameTimeDeltaPartialTick(false);
    }

    public static boolean hudHidden(Minecraft minecraft) {
        return minecraft.options.hideGui;
    }

    /** The camera's forward, up and left directions. */
    public static float[][] cameraBasis(Camera camera) {
        return new float[][]{vector(camera.getLookVector()), vector(camera.getUpVector()), vector(camera.getLeftVector())};
    }

    private static float[] vector(Vector3f v) {
        return new float[]{v.x(), v.y(), v.z()};
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
