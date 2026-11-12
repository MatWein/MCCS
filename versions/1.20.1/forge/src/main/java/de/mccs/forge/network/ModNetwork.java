package de.mccs.forge.network;

import de.mccs.mc.Mccs;
import de.mccs.mc.network.BeamStatePacket;
import de.mccs.mc.network.StaffActionPacket;
import de.mccs.mc.staff.StaffBeam;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModNetwork {
    private static final String PROTOCOL_VERSION = "3";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            Mccs.id("main"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

    private ModNetwork() {}

    public static void register() {
        CHANNEL.messageBuilder(StaffActionPacket.class, 0, NetworkDirection.PLAY_TO_SERVER)
                .encoder(StaffActionPacket::encode)
                .decoder(StaffActionPacket::decode)
                .consumerMainThread((packet, context) -> {
                    ServerPlayer sender = context.get().getSender();
                    if (sender != null) packet.handle(sender);
                    context.get().setPacketHandled(true);
                })
                .add();
        CHANNEL.messageBuilder(BeamStatePacket.class, 1, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(BeamStatePacket::encode)
                .decoder(BeamStatePacket::decode)
                .consumerNetworkThread((packet, context) -> {
                    context.get().enqueueWork(packet::handle);
                    context.get().setPacketHandled(true);
                })
                .add();
        StaffBeam.sender = (shooter, firing, tether, targets) ->
                CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> shooter), new BeamStatePacket(shooter.getId(), firing, tether, targets));
    }

    public static void sendToServer(Object packet) {
        CHANNEL.sendToServer(packet);
    }
}
