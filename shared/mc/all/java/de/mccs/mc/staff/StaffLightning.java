package de.mccs.mc.staff;

import de.mccs.core.config.ServerConfig;
import de.mccs.core.config.StaffConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;

/**
 * Lightning charges the staff in a player's hands. During thunderstorms it strikes players with an active shield under open
 * sky, like MCPJ's jumper: holding up the shield is the way to charge without any other mods.
 */
public final class StaffLightning {
    private static final double ATTRACT_HEIGHT = 512;

    private StaffLightning() {}

    /** An entity was struck; Minecraft does this once per bolt and entity. */
    public static void struck(Entity entity) {
        if (!(entity instanceof ServerPlayer player)) return;
        ItemStack staff = StaffSystems.heldStaff(player);
        if (staff.isEmpty()) return;
        StaffConfig config = ServerConfig.STAFF;
        int capacity = StaffSystems.capacity(staff);
        long charged = StaffSystems.stored(staff) + Math.round(capacity * config.lightningCharge());
        StaffData.setSavedEnergy(staff, (int) Math.min(charged, capacity));
    }

    /** A bolt enters the world: natural lightning in a thunderstorm moves to the nearest player with an active shield. */
    public static void attract(LightningBolt bolt) {
        double range = ServerConfig.STAFF.lightningAttractRange();
        if (range <= 0 || bolt.getCause() != null || !(bolt.level() instanceof ServerLevel level) || !level.isThundering()) return;
        AABB area = new AABB(bolt.position(), bolt.position()).inflate(range, ATTRACT_HEIGHT, range);
        level.getEntitiesOfClass(ServerPlayer.class, area, StaffLightning::attracts).stream()
                .min(Comparator.comparingDouble(player -> player.distanceToSqr(bolt.getX(), player.getY(), bolt.getZ())))
                .ifPresent(player -> bolt.setPos(player.getX(), player.getBoundingBox().maxY, player.getZ()));
    }

    private static boolean attracts(ServerPlayer player) {
        return player.isAlive() && !StaffSystems.activeShield(player).isEmpty()
                && player.level().canSeeSky(BlockPos.containing(player.getX(), player.getBoundingBox().maxY, player.getZ()));
    }
}
