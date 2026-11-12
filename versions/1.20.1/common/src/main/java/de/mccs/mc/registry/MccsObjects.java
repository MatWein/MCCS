package de.mccs.mc.registry;

import de.mccs.core.MccsIds;
import de.mccs.mc.entity.EnergyBallEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

/** Construction of MCCS's objects. The loaders only register them, each in its own way. */
public final class MccsObjects {
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
