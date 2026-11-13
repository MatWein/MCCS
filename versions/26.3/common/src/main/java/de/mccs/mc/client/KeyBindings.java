package de.mccs.mc.client;

import com.mojang.blaze3d.platform.InputConstants;
import de.mccs.mc.Mccs;
import net.minecraft.client.KeyMapping;

import java.util.List;

/** The mod's keys; each loader registers them. */
public final class KeyBindings {
    /** Category of the mod's keys (translation key "key.category.mccs.staff"). */
    public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Mccs.id("staff"));

    public static final KeyMapping SHIELD = new KeyMapping("key.mccs.shield", InputConstants.KEY_V, CATEGORY);
    public static final KeyMapping CLOAK = new KeyMapping("key.mccs.cloak", InputConstants.KEY_C, CATEGORY);
    public static final KeyMapping TELEPORT = new KeyMapping("key.mccs.teleport", InputConstants.KEY_R, CATEGORY);
    public static final KeyMapping LIGHTS = new KeyMapping("key.mccs.lights", InputConstants.KEY_L, CATEGORY);
    public static final KeyMapping BLOCK_DAMAGE = new KeyMapping("key.mccs.block_damage", InputConstants.KEY_K, CATEGORY);
    public static final KeyMapping TELEKINESIS = new KeyMapping("key.mccs.telekinesis", InputConstants.Type.MOUSE, InputConstants.MOUSE_BUTTON_MIDDLE, CATEGORY);

    public static final List<KeyMapping> ALL = List.of(SHIELD, CLOAK, TELEPORT, LIGHTS, BLOCK_DAMAGE, TELEKINESIS);

    private KeyBindings() {}
}
