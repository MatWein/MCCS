package de.mccs.mc.registry;

import com.mojang.serialization.Codec;
import de.mccs.core.MccsIds;
import de.mccs.mc.entity.EnergyBallEntity;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

import java.util.Map;

/** Construction of MCCS's objects. The loaders only register them, each in its own way. */
public final class MccsObjects {
    public static final DataComponentType<Integer> ENERGY = DataComponentType.<Integer>builder()
            .persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT).build();
    public static final DataComponentType<Boolean> SHIELD_ON = DataComponentType.<Boolean>builder()
            .persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build();
    public static final DataComponentType<Boolean> CLOAK_ON = DataComponentType.<Boolean>builder()
            .persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build();
    public static final DataComponentType<Boolean> LIGHTS_ON = DataComponentType.<Boolean>builder()
            .persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build();
    public static final DataComponentType<Boolean> BLOCK_DAMAGE = DataComponentType.<Boolean>builder()
            .persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build();
    public static final DataComponentType<Integer> SHIELD_HITS = DataComponentType.<Integer>builder()
            .persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT).build();

    /** The staff's data components by registry name; each loader registers them. */
    public static final Map<String, DataComponentType<?>> COMPONENTS = Map.of(
            "energy", ENERGY,
            "shield_on", SHIELD_ON,
            "cloak_on", CLOAK_ON,
            "lights_on", LIGHTS_ON,
            "block_damage", BLOCK_DAMAGE,
            "shield_hits", SHIELD_HITS);

    private static final int TRACKING_RANGE = 12;

    private MccsObjects() {}

    public static EntityType<EnergyBallEntity> energyBallType() {
        return EntityType.Builder.<EnergyBallEntity>of(EnergyBallEntity::new, MobCategory.MISC)
                .sized(EnergyBallEntity.SIZE, EnergyBallEntity.SIZE)
                .fireImmune()
                .clientTrackingRange(TRACKING_RANGE)
                .updateInterval(1)
                .build(MccsIds.ENERGY_BALL);
    }

    public static Item.Properties crystalStaffProperties() {
        return new Item.Properties().stacksTo(1).rarity(Rarity.RARE);
    }
}
