package de.mcqw.mc.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/** See {@link EnergyBallBase}. */
public final class EnergyBallEntity extends EnergyBallBase {
    /** Set by the loader when it registers the type. */
    public static EntityType<EnergyBallEntity> TYPE;

    public EnergyBallEntity(EntityType<? extends EnergyBallEntity> type, Level level) {
        super(type, level);
    }

    public EnergyBallEntity(Level level) {
        this(TYPE, level);
    }

    @Override
    protected void defineSynchedData() {}
}
