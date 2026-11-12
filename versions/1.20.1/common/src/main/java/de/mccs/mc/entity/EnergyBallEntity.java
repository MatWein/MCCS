package de.mccs.mc.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/** See {@link EnergyBallBase}. */
public final class EnergyBallEntity extends EnergyBallBase {
    /** Set by the loader when it registers the type. */
    public static EntityType<EnergyBallEntity> TYPE;

    private static final EntityDataAccessor<Float> CHARGE = SynchedEntityData.defineId(EnergyBallEntity.class, EntityDataSerializers.FLOAT);

    public EnergyBallEntity(EntityType<? extends EnergyBallEntity> type, Level level) {
        super(type, level);
    }

    public EnergyBallEntity(Level level) {
        this(TYPE, level);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(CHARGE, 0f);
    }

    @Override
    public float charge() {
        return entityData.get(CHARGE);
    }

    @Override
    public void setCharge(float charge) {
        entityData.set(CHARGE, charge);
    }
}
