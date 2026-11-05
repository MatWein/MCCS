package de.mcqw.mc.staff;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

/** Where the lightning goes; on the server for the hits, on the clients for the picture. */
public final class BeamAim {
    private static final double CRYSTAL_RIGHT = 0.43;
    private static final double CRYSTAL_FORWARD = 0.2;
    private static final double CRYSTAL_UP = 1.67;
    private static final double CROUCH_DROP = 0.3;

    private BeamAim() {}

    /** The end of the player's aim: the first block, or the end of the range. */
    public static Vec3 end(Player player, double range, float partialTick) {
        Vec3 eye = player.getEyePosition(partialTick);
        Vec3 far = eye.add(player.getViewVector(partialTick).scale(range));
        return player.level().clip(new ClipContext(eye, far, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player)).getLocation();
    }

    /** With block damage on: sets the block the player aims at on fire, if it can burn there. */
    static void ignite(Player player, double range) {
        Vec3 eye = player.getEyePosition(1f);
        Vec3 far = eye.add(player.getViewVector(1f).scale(range));
        BlockHitResult hit = player.level().clip(new ClipContext(eye, far, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (hit.getType() != HitResult.Type.BLOCK) return;
        BlockPos pos = hit.getBlockPos().relative(hit.getDirection());
        Level level = player.level();
        if (level.isEmptyBlock(pos) && level.mayInteract(player, pos) && BaseFireBlock.canBePlacedAt(level, pos, player.getDirection())) {
            level.setBlockAndUpdate(pos, BaseFireBlock.getState(level, pos));
        }
    }

    /** The nearest entity the player aims at, in front of the first block, or null; paintings and item frames do not count. */
    public static Entity target(Player player, double range, double tolerance) {
        Vec3 eye = player.getEyePosition(1f);
        Vec3 end = end(player, range, 1f);
        Entity nearest = null;
        double best = Double.MAX_VALUE;
        AABB area = new AABB(eye, end).inflate(tolerance + 1);
        for (Entity candidate : player.level().getEntities(player, area, e -> e.isAlive() && e.isPickable() && e.isAttackable() && !e.isSpectator() && !(e instanceof HangingEntity))) {
            Optional<Vec3> hit = candidate.getBoundingBox().inflate(tolerance).clip(eye, end);
            double distance = hit.map(eye::distanceToSqr).orElse(Double.MAX_VALUE);
            if (distance < best) {
                nearest = candidate;
                best = distance;
            }
        }
        return nearest;
    }

    /** Where the crystal of a staff in the given arm is, seen from outside; from the third person pose in tools/make_staff_assets.py. */
    public static Vec3 crystal(Player player, HumanoidArm arm, float partialTick) {
        float yaw = Mth.rotLerp(partialTick, player.yBodyRotO, player.yBodyRot) * Mth.DEG_TO_RAD;
        double side = arm == HumanoidArm.LEFT ? -CRYSTAL_RIGHT : CRYSTAL_RIGHT;
        double forwardX = -Mth.sin(yaw);
        double forwardZ = Mth.cos(yaw);
        double rightX = -Mth.cos(yaw);
        double rightZ = -Mth.sin(yaw);
        double up = CRYSTAL_UP - (player.isCrouching() ? CROUCH_DROP : 0);
        return player.getPosition(partialTick).add(forwardX * CRYSTAL_FORWARD + rightX * side, up, forwardZ * CRYSTAL_FORWARD + rightZ * side);
    }

    public static Vec3 center(Entity entity) {
        return entity.getBoundingBox().getCenter();
    }

    /** Whether no block is between the two points. */
    static boolean canSee(Entity source, Vec3 from, Vec3 to) {
        return source.level().clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, source)).getType() == HitResult.Type.MISS;
    }
}
