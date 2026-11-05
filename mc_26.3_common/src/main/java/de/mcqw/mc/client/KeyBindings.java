package de.mcqw.mc.client;

import com.mojang.blaze3d.platform.InputConstants;
import de.mcqw.mc.Mcqw;
import net.minecraft.client.KeyMapping;

/** The mod's keys; each loader registers them. */
public final class KeyBindings {
    /** Category of the mod's keys (translation key "key.category.mcqw.staff"). */
    public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Mcqw.id("staff"));

    public static final KeyMapping SHIELD = new KeyMapping("key.mcqw.shield", InputConstants.KEY_V, CATEGORY);

    public static final KeyMapping CLOAK = new KeyMapping("key.mcqw.cloak", InputConstants.KEY_C, CATEGORY);

    public static final KeyMapping TELEPORT = new KeyMapping("key.mcqw.teleport", InputConstants.KEY_R, CATEGORY);

    private KeyBindings() {}
}
