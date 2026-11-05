package de.mcqw.mc.client;

import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

/** The mod's keys; each loader registers them. */
public final class KeyBindings {
    private static final String CATEGORY = "key.categories.mcqw";

    public static final KeyMapping SHIELD = new KeyMapping("key.mcqw.shield", GLFW.GLFW_KEY_V, CATEGORY);

    public static final KeyMapping CLOAK = new KeyMapping("key.mcqw.cloak", GLFW.GLFW_KEY_C, CATEGORY);

    public static final KeyMapping TELEPORT = new KeyMapping("key.mcqw.teleport", GLFW.GLFW_KEY_R, CATEGORY);

    private KeyBindings() {}
}
