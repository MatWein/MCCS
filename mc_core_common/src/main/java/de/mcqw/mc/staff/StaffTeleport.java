package de.mcqw.mc.staff;

import de.mcqw.core.config.ServerConfig;
import de.mcqw.core.config.TeleportConfig;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.WeakHashMap;

/** The staff's teleport to where the player looks, stopping before the first block. */
final class StaffTeleport {
    private static final double STEP_BACK = 0.5;
    private static final double SPARK_SPACING = 0.5;
    private static final double GAP_BELOW_CEILING = 0.02;

    /** Player tick of the last teleport. */
    private static final Map<ServerPlayer, Integer> LAST = new WeakHashMap<>();

    private StaffTeleport() {}

    static void perform(ServerPlayer player) {
        ItemStack staff = StaffSystems.heldStaff(player);
        if (staff.isEmpty()) return;
        TeleportConfig config = ServerConfig.TELEPORT;
        Integer last = LAST.get(player);
        if (last != null && player.tickCount >= last && player.tickCount - last < config.cooldownTicks()) return;
        boolean paid = ServerConfig.STAFF.consumeEnergy();
        if (paid && StaffSystems.stored(staff) < config.energy()) {
            StaffSounds.denied(player);
            return;
        }
        Vec3 target = findPlace(player, config.range());
        if (target == null) {
            StaffSounds.denied(player);
            return;
        }
        if (paid) StaffSystems.consume(staff, config.energy());
        LAST.put(player, player.tickCount);
        Vec3 from = player.position();
        sparksAlong((ServerLevel) player.level(), from.add(0, player.getBbHeight() / 2, 0), target.add(0, player.getBbHeight() / 2, 0));
        StaffSounds.teleported(player.level(), from);
        player.teleportTo(target.x, target.y, target.z);
        player.resetFallDistance();
        StaffSounds.teleported(player.level(), target);
    }

    /** Where the feet can go: the end of the view, or the nearest place before it with room for the player; null if none. */
    private static Vec3 findPlace(ServerPlayer player, double range) {
        Vec3 eye = player.getEyePosition(1f);
        Vec3 look = player.getViewVector(1f);
        BlockHitResult hit = player.level().clip(new ClipContext(eye, eye.add(look.scale(range)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        Vec3 end = hit.getLocation();
        Vec3 feet;
        if (hit.getType() == HitResult.Type.BLOCK && hit.getDirection() == Direction.UP) {
            feet = end;
        } else if (hit.getType() == HitResult.Type.BLOCK && hit.getDirection() == Direction.DOWN) {
            feet = end.subtract(0, player.getBbHeight() + GAP_BELOW_CEILING, 0);
        } else {
            feet = end.subtract(0, eye.y - player.getY(), 0);
        }
        int steps = (int) Math.ceil(end.distanceTo(eye) / STEP_BACK);
        for (int i = 0; i < steps; i++) {
            Vec3 place = feet.subtract(look.scale(i * STEP_BACK));
            if (fits(player, place)) return place;
            Vec3 above = place.add(0, 1, 0);
            if (fits(player, above)) return above;
        }
        return null;
    }

    private static boolean fits(ServerPlayer player, Vec3 feet) {
        return player.level().noCollision(player, player.getBoundingBox().move(feet.subtract(player.position())));
    }

    private static void sparksAlong(ServerLevel level, Vec3 from, Vec3 to) {
        double length = from.distanceTo(to);
        Vec3 direction = to.subtract(from).normalize();
        for (double d = 0; d < length; d += SPARK_SPACING) {
            Vec3 point = from.add(direction.scale(d));
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, point.x, point.y, point.z, 1, 0.05, 0.05, 0.05, 0.02);
        }
    }
}
