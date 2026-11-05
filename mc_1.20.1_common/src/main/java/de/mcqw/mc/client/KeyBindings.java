package de.mcqw.mc.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

/** The mod's keys; each loader registers them. */
public final class KeyBindings {
    private static final String CATEGORY = "key.categories.mcqw";

    public static final KeyMapping SHIELD = new KeyMapping("key.mcqw.shield", GLFW.GLFW_KEY_V, CATEGORY);

    public static final KeyMapping CLOAK = new KeyMapping("key.mcqw.cloak", GLFW.GLFW_KEY_C, CATEGORY);

    public static final KeyMapping TELEPORT = new KeyMapping("key.mcqw.teleport", GLFW.GLFW_KEY_R, CATEGORY);

    public static final KeyMapping LIGHTS = new KeyMapping("key.mcqw.lights", GLFW.GLFW_KEY_L, CATEGORY);
    public static final KeyMapping BLOCK_DAMAGE = new KeyMapping("key.mcqw.block_damage", GLFW.GLFW_KEY_K, CATEGORY);

    public static final KeyMapping TELEKINESIS = new KeyMapping("key.mcqw.telekinesis", InputConstants.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_MIDDLE, CATEGORY);

    private KeyBindings() {}
}
