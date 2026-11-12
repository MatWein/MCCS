package de.mccs.mc.staff;

import de.mccs.core.config.BallConfig;
import de.mccs.core.config.ServerConfig;
import de.mccs.core.staff.BallMath;
import de.mccs.mc.compat.StaffCompat;
import de.mccs.mc.entity.EnergyBallEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** The staff's energy ball: firing it, and its explosion, which hurts living things but leaves blocks and items alone. */
public final class StaffBall {
    private static final double MIN_AIM_DISTANCE_SQR = 1;
    private static final double ROUGH_EDGE = 0.75;
    private static final int SPARKS = 60;
    private static final double SPARK_SPREAD = 1.5;
    private static final double SPARK_SPEED = 0.3;
    /** Knockback lifts targets a little, or friction on the ground would eat most of it. */
    private static final double KNOCKBACK_LIFT = 0.3;
    private static final int CHARGE_SOUND_INTERVAL = 8;
    private static final float CHARGE_PITCH_START = 0.6f;
    private static final float CHARGE_PITCH_RANGE = 1.2f;

    private StaffBall() {}

    /** The use key is being held: the ball charges up, which can be heard. */
    public static void charging(LivingEntity entity, int usedTicks) {
        if (!(entity instanceof ServerPlayer player) || StaffSystems.mainStaff(player).isEmpty()) return;
        int full = ServerConfig.BALL.chargeTicks();
        if (usedTicks == full) {
            StaffSounds.ballFullyCharged(player);
        } else if (usedTicks < full && usedTicks % CHARGE_SOUND_INTERVAL == 0) {
            StaffSounds.ballCharging(player, CHARGE_PITCH_START + CHARGE_PITCH_RANGE * usedTicks / full);
        }
    }

    /** The use key was released after holding it for the given ticks: a short click is a plain shot. */
    public static void fire(ServerPlayer player, int usedTicks) {
        ItemStack staff = StaffSystems.mainStaff(player);
        if (staff.isEmpty()) return;
        BallConfig config = ServerConfig.BALL;
        double share = BallMath.chargeShare(usedTicks, config.chargeTicks());
        if (ServerConfig.STAFF.consumeEnergy()) {
            share = BallMath.affordableShare(share, StaffSystems.stored(staff), config.energy(), config.chargedEnergyFactor());
            if (share < 0) {
                StaffSounds.denied(player);
                return;
            }
            StaffSystems.consume(staff, Math.round(BallMath.charged(config.energy(), config.chargedEnergyFactor(), share)));
        }
        StaffSystems.endCloak(player);
        // from the crystal, where it was charged, to what the player aims at
        Vec3 eye = player.getEyePosition(1f);
        Vec3 crystal = BeamAim.crystal(player, player.getMainArm(), 1f);
        Vec3 start = BeamAim.canSee(player, eye, crystal) ? crystal : eye;
        Vec3 aim = BeamAim.end(player, config.range(), 1f);
        Vec3 direction = aim.distanceToSqr(start) > MIN_AIM_DISTANCE_SQR ? aim.subtract(start).normalize() : player.getViewVector(1f);
        EnergyBallEntity ball = new EnergyBallEntity(player.level());
        ball.setOwner(player);
        ball.setCharge((float) share);
        ball.setBlockDamage(StaffData.blockDamage(staff));
        ball.setPos(start.subtract(0, EnergyBallEntity.SIZE / 2, 0));
        ball.setDeltaMovement(direction.scale(config.speed()));
        player.level().addFreshEntity(ball);
        StaffSounds.ballLaunched(player);
        StaffCompat.cooldown(player, staff, config.cooldownTicks());
    }

    /** With block damage on: what the blast can break within its radius, with a rough edge. Not what the shooter may not touch. */
    private static void breakBlocks(ServerLevel level, Entity ball, Entity owner, Vec3 center, double radius) {
        BallConfig config = ServerConfig.BALL;
        BlockPos middle = BlockPos.containing(center);
        int reach = (int) Math.ceil(radius);
        var random = level.getRandom();
        for (BlockPos pos : BlockPos.betweenClosed(middle.offset(-reach, -reach, -reach), middle.offset(reach, reach, reach))) {
            double edge = radius * (ROUGH_EDGE + (1 - ROUGH_EDGE) * random.nextDouble());
            if (Vec3.atCenterOf(pos).distanceTo(center) > edge) continue;
            BlockState state = level.getBlockState(pos);
            if (state.isAir() || !state.getFluidState().isEmpty()) continue;
            if (state.getDestroySpeed(level, pos) < 0 || state.getBlock().getExplosionResistance() > config.maxBlockResistance()) continue;
            if (owner instanceof Player player && !level.mayInteract(player, pos)) continue;
            // not destroyBlock: that plays each block's breaking sound on top of the explosion
            if (random.nextDouble() < config.blockDropChance()) {
                Block.dropResources(state, level, pos, state.hasBlockEntity() ? level.getBlockEntity(pos) : null, ball, ItemStack.EMPTY);
            }
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        }
    }

    /** Everything the blast reaches takes damage, except the shooter and what is behind blocks. */
    public static void explode(ServerLevel level, Entity ball, Entity owner, Vec3 center, double charge, boolean blockDamage) {
        BallConfig config = ServerConfig.BALL;
        double radius = BallMath.charged(config.radius(), config.chargedRadiusFactor(), charge);
        double damage = BallMath.charged(config.damage(), config.chargedDamageFactor(), charge);
        DamageSource source = ball.damageSources().explosion(ball, owner);
        AABB area = new AABB(center, center).inflate(radius);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area, e -> e != owner && e.isAlive() && !e.isSpectator())) {
            Vec3 targetCenter = BeamAim.center(target);
            double share = BallMath.blastShare(targetCenter.distanceTo(center), radius, config.edgeDamage());
            if (share <= 0 || !BeamAim.canSee(ball, center, targetCenter)) continue;
            if (StaffCompat.hurt(target, source, (float) (damage * share))) {
                Vec3 away = targetCenter.subtract(center).normalize().scale(config.knockback() * share);
                StaffCompat.launch(target, target.getDeltaMovement().add(away.x, away.y + KNOCKBACK_LIFT * share, away.z));
            }
        }
        if (blockDamage) breakBlocks(level, ball, owner, center, radius);
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, center.x, center.y, center.z, 1, 0, 0, 0, 0);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, center.x, center.y, center.z, SPARKS, SPARK_SPREAD, SPARK_SPREAD, SPARK_SPREAD, SPARK_SPEED);
        StaffSounds.ballExploded(level, center);
    }
}
