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
import java.util.Optional;

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

    /**
     * Where natural lightning strikes (Minecraft's choice, which already prefers lightning rods): the nearest player with an
     * active shield under open sky within range takes it instead, like a lightning rod. Called before the bolt exists, so
     * everybody sees it strike there (Fabric and NeoForge 26.x: a mixin on ServerLevel#findLightningTargetAround).
     */
    public static BlockPos target(ServerLevel level, BlockPos chosen) {
        return attractor(level, chosen.getX() + 0.5, chosen.getZ() + 0.5, chosen.getY()).map(Entity::blockPosition).orElse(chosen);
    }

    /**
     * A natural bolt enters the world (Forge 1.20.1, whose event comes before anyone is sent the bolt): it moves to the
     * nearest player with an active shield, as in {@link #target}.
     */
    public static void attract(LightningBolt bolt) {
        if (bolt.getCause() != null || !(bolt.level() instanceof ServerLevel level)) return;
        attractor(level, bolt.getX(), bolt.getZ(), bolt.getY())
                .ifPresent(player -> bolt.setPos(player.getX(), player.getBoundingBox().maxY, player.getZ()));
    }

    /** The nearest player with an active shield under open sky in range of where lightning would strike, during a thunderstorm. */
    private static Optional<ServerPlayer> attractor(ServerLevel level, double x, double z, double y) {
        double range = ServerConfig.STAFF.lightningAttractRange();
        if (range <= 0 || !level.isThundering()) return Optional.empty();
        AABB area = new AABB(x, y, z, x, y, z).inflate(range, ATTRACT_HEIGHT, range);
        return level.getEntitiesOfClass(ServerPlayer.class, area, StaffLightning::attracts).stream()
                .min(Comparator.comparingDouble(player -> player.distanceToSqr(x, player.getY(), z)));
    }

    private static boolean attracts(ServerPlayer player) {
        return player.isAlive() && !StaffSystems.activeShield(player).isEmpty()
                && player.level().canSeeSky(BlockPos.containing(player.getX(), player.getBoundingBox().maxY, player.getZ()));
    }
}
