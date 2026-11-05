package de.mcqw.mc.staff;

import de.mcqw.core.config.ServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * The staff's light. Minecraft has no moving light sources, so, like MCPJ's jumper, the server places an invisible light block
 * at the player and removes it behind them. It stays dark while the player is cloaked, riding or not holding the staff.
 */
public final class StaffLights {
    private static final int UPDATE_INTERVAL = 4;
    private static final double MOVE_DISTANCE = 2;

    private record Placed(ServerLevel level, BlockPos pos) {}

    private static final Map<ServerPlayer, Placed> PLACED = new HashMap<>();

    private StaffLights() {}

    static void tick(MinecraftServer server) {
        for (Map.Entry<ServerPlayer, Placed> entry : new ArrayList<>(PLACED.entrySet())) {
            ServerPlayer player = entry.getKey();
            if (player.isRemoved() || !shines(player) || player.level() != entry.getValue().level()) {
                remove(entry.getValue());
                PLACED.remove(player);
            }
        }
        if (server.getTickCount() % UPDATE_INTERVAL != 0) return;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (shines(player)) update(player);
        }
    }

    private static boolean shines(ServerPlayer player) {
        return player.isAlive() && !StaffSystems.activeLights(player).isEmpty() && StaffSystems.activeCloak(player).isEmpty();
    }

    private static void update(ServerPlayer player) {
        Placed current = PLACED.get(player);
        Vec3 eye = player.getEyePosition();
        if (current != null && Vec3.atCenterOf(current.pos()).distanceTo(eye) <= MOVE_DISTANCE) return;
        ServerLevel level = (ServerLevel) player.level();
        BlockPos target = BlockPos.containing(eye);
        if (!place(level, target) && !place(level, target.below())) return;
        BlockPos placed = level.getBlockState(target).is(Blocks.LIGHT) ? target : target.below();
        if (current != null && !current.pos().equals(placed)) remove(current);
        PLACED.put(player, new Placed(level, placed));
    }

    private static boolean place(ServerLevel level, BlockPos pos) {
        if (!level.isLoaded(pos)) return false;
        BlockState existing = level.getBlockState(pos);
        if (existing.is(Blocks.LIGHT)) return true;
        boolean water = existing.is(Blocks.WATER) && existing.getFluidState().isSource();
        if (!existing.isAir() && !water) return false;
        BlockState light = Blocks.LIGHT.defaultBlockState()
                .setValue(LightBlock.LEVEL, ServerConfig.STAFF.lightLevel())
                .setValue(BlockStateProperties.WATERLOGGED, water);
        return level.setBlock(pos, light, Block.UPDATE_CLIENTS);
    }

    private static void remove(Placed placed) {
        ServerLevel level = placed.level();
        if (!level.isLoaded(placed.pos())) return;
        BlockState state = level.getBlockState(placed.pos());
        if (!state.is(Blocks.LIGHT)) return;
        BlockState restored = state.getValue(BlockStateProperties.WATERLOGGED) ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState();
        level.setBlock(placed.pos(), restored, Block.UPDATE_CLIENTS);
    }

    /** The server stops: no light blocks stay in the world. */
    public static void removeAll() {
        PLACED.values().forEach(StaffLights::remove);
        PLACED.clear();
    }
}
