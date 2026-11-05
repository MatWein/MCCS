package de.mcqw.mc.client;

import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

/** The mod's keys; each loader registers them. */
public final class KeyBindings {
    private static final String CATEGORY = "key.categories.mcqw";

    public static final KeyMapping SHIELD = new KeyMapping("key.mcqw.shield", GLFW.GLFW_KEY_V, CATEGORY);

    private KeyBindings() {}
}
