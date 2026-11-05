package de.mcqw.mc.registry;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

/** Construction of MCQW's objects. The loaders only register them, each in its own way. */
public final class McqwObjects {
    private McqwObjects() {}

    public static Item.Properties crystalStaffProperties() {
        return new Item.Properties().stacksTo(1).rarity(Rarity.RARE);
    }
}
