package de.mcqw.mc.client;

import de.mcqw.mc.client.hud.HudCanvas;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import org.joml.Vector3fc;

/** Client calls that differ between Minecraft versions, for the shared client code. */
public final class ClientCompat {
    private ClientCompat() {}

    public static boolean screenOpen(Minecraft minecraft) {
        return minecraft.gui.screen() != null;
    }

    public static boolean hudHidden(Minecraft minecraft) {
        return minecraft.gui.hud.isHidden();
    }

    /** The camera's forward, up and left directions. */
    public static float[][] cameraBasis(Camera camera) {
        return new float[][]{vector(camera.forwardVector()), vector(camera.upVector()), vector(camera.leftVector())};
    }

    private static float[] vector(Vector3fc v) {
        return new float[]{v.x(), v.y(), v.z()};
    }

    public static HudCanvas hudCanvas(GuiGraphicsExtractor graphics) {
        return new HudCanvas() {
            @Override
            public void fill(int x1, int y1, int x2, int y2, int argb) {
                graphics.fill(x1, y1, x2, y2, argb);
            }

            @Override
            public void text(Font font, Component text, int x, int y, int rgb) {
                graphics.text(font, text, x, y, ARGB.opaque(rgb), true);
            }
        };
    }
}
