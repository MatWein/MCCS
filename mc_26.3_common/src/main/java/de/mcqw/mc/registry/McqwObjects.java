package de.mcqw.mc.registry;

import de.mcqw.core.McqwIds;
import de.mcqw.mc.Mcqw;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

/** Construction of MCQW's objects. The loaders only register them, each in its own way. */
public final class McqwObjects {
    private McqwObjects() {}

    public static Item.Properties crystalStaffProperties() {
        return new Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM, Mcqw.id(McqwIds.CRYSTAL_STAFF)))
                .stacksTo(1)
                .rarity(Rarity.RARE);
    }
}
