package de.mcqw.fabric.network;

import de.mcqw.fabric.config.FabricConfigs;
import de.mcqw.mc.Mcqw;
import de.mcqw.mc.network.BeamStatePacket;
import de.mcqw.mc.network.StaffActionPacket;
import de.mcqw.mc.staff.StaffBeam;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;

/** MCQW's messages on Fabric's networking API; the server also sends its settings to joining players. */
public final class FabricNetwork {
    public static final ResourceLocation STAFF_ACTION = Mcqw.id("staff_action");
    public static final ResourceLocation BEAM_STATE = Mcqw.id("beam_state");
    public static final ResourceLocation SERVER_CONFIG = Mcqw.id("server_config");

    private FabricNetwork() {}

    public static void registerServer() {
        ServerPlayNetworking.registerGlobalReceiver(STAFF_ACTION, (server, player, handler, buf, sender) -> {
            StaffActionPacket packet = StaffActionPacket.decode(buf);
            server.execute(() -> packet.handle(player));
        });
        StaffBeam.sender = (shooter, firing, tether, targets) -> {
            FriendlyByteBuf buffer = PacketByteBufs.create();
            BeamStatePacket.encode(new BeamStatePacket(shooter.getId(), firing, tether, targets), buffer);
            for (ServerPlayer player : PlayerLookup.tracking(shooter)) ServerPlayNetworking.send(player, BEAM_STATE, PacketByteBufs.copy(buffer));
            ServerPlayNetworking.send(shooter, BEAM_STATE, buffer);
        };
        // the HUD needs the server's settings (Forge syncs them itself)
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                sender.sendPacket(SERVER_CONFIG, encodeConfig(FabricConfigs.SERVER.values())));
    }

    private static FriendlyByteBuf encodeConfig(Map<String, String> values) {
        FriendlyByteBuf buffer = PacketByteBufs.create();
        buffer.writeMap(values, FriendlyByteBuf::writeUtf, FriendlyByteBuf::writeUtf);
        return buffer;
    }

    public static Map<String, String> decodeConfig(FriendlyByteBuf buffer) {
        return buffer.readMap(FriendlyByteBuf::readUtf, FriendlyByteBuf::readUtf);
    }
}
