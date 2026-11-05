package de.mcqw.mc.entity;

import de.mcqw.core.config.BallConfig;
import de.mcqw.core.config.ServerConfig;
import de.mcqw.core.staff.BallMath;
import de.mcqw.mc.staff.StaffBall;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * The staff's energy ball flies straight and explodes on the first block or entity; what differs between Minecraft
 * versions is in EnergyBallEntity.
 */
public abstract class EnergyBallBase extends Projectile {
    public static final float SIZE = 0.5f;
    private static final double RENDER_DISTANCE = 256;
    private static final double TRAIL_SPEED = 0.05;

    private boolean blockDamage;

    protected EnergyBallBase(EntityType<? extends EnergyBallBase> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 motion = getDeltaMovement();
        if (level() instanceof ServerLevel server) {
            BallConfig config = ServerConfig.BALL;
            if (tickCount > BallMath.lifetime(config.range(), config.speed())) {
                discard();
                return;
            }
            HitResult hit = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
            if (hit.getType() != HitResult.Type.MISS) {
                Entity owner = getOwner();
                StaffBall.explode(server, this, owner, hit.getLocation(), charge(), blockDamage);
                discard();
                return;
            }
        } else {
            level().addParticle(ParticleTypes.ELECTRIC_SPARK, getX(), getY() + SIZE / 2, getZ(),
                    (random.nextDouble() - 0.5) * TRAIL_SPEED, (random.nextDouble() - 0.5) * TRAIL_SPEED, (random.nextDouble() - 0.5) * TRAIL_SPEED);
        }
        setPos(position().add(motion));
    }

    /** Whether the explosion breaks blocks. */
    public void setBlockDamage(boolean blockDamage) {
        this.blockDamage = blockDamage;
    }

    /** How much the ball was charged up, from 0 to 1. */
    public abstract float charge();

    public abstract void setCharge(float charge);

    /** Its shooter and other balls are no targets. */
    @Override
    public boolean canHitEntity(Entity entity) {
        return super.canHitEntity(entity) && entity != getOwner() && !(entity instanceof EnergyBallBase);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distanceSqr) {
        return distanceSqr < RENDER_DISTANCE * RENDER_DISTANCE;
    }
}
