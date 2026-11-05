package de.mcqw.mc.staff;

import de.mcqw.core.config.BallConfig;
import de.mcqw.core.config.ServerConfig;
import de.mcqw.core.staff.BallMath;
import de.mcqw.mc.compat.StaffCompat;
import de.mcqw.mc.entity.EnergyBallEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** The staff's energy ball: firing it, and its explosion, which hurts living things but leaves blocks and items alone. */
public final class StaffBall {
    /** Where the ball appears, relative to the eyes: near the crystal, in front of the player. */
    private static final double START_FORWARD = 0.8;
    private static final double START_DOWN = 0.2;
    private static final int SPARKS = 60;
    private static final double SPARK_SPREAD = 1.5;
    private static final double SPARK_SPEED = 0.3;
    /** Knockback lifts targets a little, or friction on the ground would eat most of it. */
    private static final double KNOCKBACK_LIFT = 0.3;

    private StaffBall() {}

    /** Right click with the staff in the main hand. */
    public static void fire(ServerPlayer player) {
        ItemStack staff = StaffSystems.mainStaff(player);
        if (staff.isEmpty()) return;
        BallConfig config = ServerConfig.BALL;
        if (ServerConfig.STAFF.consumeEnergy()) {
            if (StaffSystems.stored(staff) < config.energy()) {
                StaffSounds.denied(player);
                return;
            }
            StaffSystems.consume(staff, config.energy());
        }
        StaffSystems.endCloak(player);
        Vec3 look = player.getViewVector(1f);
        Vec3 start = player.getEyePosition(1f).add(look.scale(START_FORWARD)).add(0, -START_DOWN - EnergyBallEntity.SIZE / 2, 0);
        EnergyBallEntity ball = new EnergyBallEntity(player.level());
        ball.setOwner(player);
        ball.setPos(start);
        ball.setDeltaMovement(look.scale(config.speed()));
        player.level().addFreshEntity(ball);
        StaffSounds.ballLaunched(player);
        StaffCompat.cooldown(player, staff, config.cooldownTicks());
    }

    /** Everything the blast reaches takes damage, except the shooter and what is behind blocks. */
    public static void explode(ServerLevel level, Entity ball, Entity owner, Vec3 center) {
        BallConfig config = ServerConfig.BALL;
        double radius = config.radius();
        DamageSource source = ball.damageSources().explosion(ball, owner);
        AABB area = new AABB(center, center).inflate(radius);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area, e -> e != owner && e.isAlive() && !e.isSpectator())) {
            Vec3 targetCenter = BeamAim.center(target);
            double share = BallMath.blastShare(targetCenter.distanceTo(center), radius, config.edgeDamage());
            if (share <= 0 || !BeamAim.canSee(ball, center, targetCenter)) continue;
            if (StaffCompat.hurt(target, source, (float) (config.damage() * share))) {
                Vec3 away = targetCenter.subtract(center).normalize().scale(config.knockback() * share);
                StaffCompat.launch(target, target.getDeltaMovement().add(away.x, away.y + KNOCKBACK_LIFT * share, away.z));
            }
        }
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, center.x, center.y, center.z, 1, 0, 0, 0, 0);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, center.x, center.y, center.z, SPARKS, SPARK_SPREAD, SPARK_SPREAD, SPARK_SPREAD, SPARK_SPEED);
        StaffSounds.ballExploded(level, center);
    }
}
