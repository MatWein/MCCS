package de.mcqw.mc.registry;

import com.mojang.serialization.Codec;
import de.mcqw.core.McqwIds;
import de.mcqw.mc.Mcqw;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

/** Construction of MCQW's objects. The loaders only register them, each in its own way. */
public final class McqwObjects {
    public static final DataComponentType<Integer> ENERGY = DataComponentType.<Integer>builder()
            .persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT).build();
    public static final DataComponentType<Boolean> SHIELD_ON = DataComponentType.<Boolean>builder()
            .persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build();
    public static final DataComponentType<Integer> SHIELD_HITS = DataComponentType.<Integer>builder()
            .persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT).build();

    private McqwObjects() {}

    public static Item.Properties crystalStaffProperties() {
        return new Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM, Mcqw.id(McqwIds.CRYSTAL_STAFF)))
                .stacksTo(1)
                .rarity(Rarity.RARE);
    }
}
