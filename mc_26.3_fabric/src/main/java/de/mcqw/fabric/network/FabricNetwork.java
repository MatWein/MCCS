package de.mcqw.fabric.network;

import de.mcqw.fabric.config.FabricConfigs;
import de.mcqw.mc.Mcqw;
import de.mcqw.mc.network.BeamStatePacket;
import de.mcqw.mc.network.StaffActionPacket;
import de.mcqw.mc.staff.StaffBeam;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;

/** MCQW's messages on Fabric's networking API; the server also sends its settings to joining players. */
public final class FabricNetwork {
    private FabricNetwork() {}

    /** The server's settings for the client (the HUD needs them; NeoForge syncs them itself). */
    public record ServerConfigPacket(Map<String, String> values) implements CustomPacketPayload {
        public static final Type<ServerConfigPacket> TYPE = new Type<>(Mcqw.id("server_config"));
        public static final StreamCodec<FriendlyByteBuf, ServerConfigPacket> STREAM_CODEC = ByteBufCodecs
                .<FriendlyByteBuf, String, String, Map<String, String>>map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.STRING_UTF8)
                .map(ServerConfigPacket::new, ServerConfigPacket::values);

        @Override
        public Type<ServerConfigPacket> type() {
            return TYPE;
        }
    }

    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(StaffActionPacket.TYPE, StaffActionPacket.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(BeamStatePacket.TYPE, BeamStatePacket.STREAM_CODEC);
        StaffBeam.sender = (shooter, firing, targets) -> {
            BeamStatePacket packet = new BeamStatePacket(shooter.getId(), firing, targets);
            for (ServerPlayer player : PlayerLookup.tracking(shooter)) ServerPlayNetworking.send(player, packet);
            ServerPlayNetworking.send(shooter, packet);
        };
        PayloadTypeRegistry.clientboundPlay().register(ServerConfigPacket.TYPE, ServerConfigPacket.STREAM_CODEC);
        // handlers run on the server thread
        ServerPlayNetworking.registerGlobalReceiver(StaffActionPacket.TYPE, (packet, context) -> packet.handle(context.player()));
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                sender.sendPacket(new ServerConfigPacket(FabricConfigs.SERVER.values())));
    }
}
