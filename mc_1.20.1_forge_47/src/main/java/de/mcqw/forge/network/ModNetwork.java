package de.mcqw.forge.network;

import de.mcqw.mc.Mcqw;
import de.mcqw.mc.network.StaffActionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModNetwork {
    private static final String PROTOCOL_VERSION = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            Mcqw.id("main"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

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
    }

    public static void sendToServer(Object packet) {
        CHANNEL.sendToServer(packet);
    }
}
