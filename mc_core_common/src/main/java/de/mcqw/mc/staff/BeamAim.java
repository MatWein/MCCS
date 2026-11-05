package de.mcqw.mc.staff;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

/** Where the lightning goes; on the server for the hits, on the clients for the picture. */
public final class BeamAim {
    private BeamAim() {}

    /** The end of the player's aim: the first block, or the end of the range. */
    public static Vec3 end(Player player, double range, float partialTick) {
        Vec3 eye = player.getEyePosition(partialTick);
        Vec3 far = eye.add(player.getViewVector(partialTick).scale(range));
        return player.level().clip(new ClipContext(eye, far, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player)).getLocation();
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

    public static Vec3 center(Entity entity) {
        return entity.getBoundingBox().getCenter();
    }

    /** Whether no block is between the two points. */
    static boolean canSee(Entity source, Vec3 from, Vec3 to) {
        return source.level().clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, source)).getType() == HitResult.Type.MISS;
    }
}
