package de.mcqw.mc.network;

import net.minecraft.network.FriendlyByteBuf;

import java.util.function.Consumer;

/** A player's lightning for everyone seeing them: whether it runs, and the entities it goes through, in order. */
public record BeamStatePacket(int shooter, boolean firing, int[] targets) {
    /** Set on the client. */
    public static Consumer<BeamStatePacket> handler = packet -> {};

    public static void encode(BeamStatePacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.shooter);
        buffer.writeBoolean(packet.firing);
        buffer.writeVarIntArray(packet.targets);
    }

    public static BeamStatePacket decode(FriendlyByteBuf buffer) {
        return new BeamStatePacket(buffer.readVarInt(), buffer.readBoolean(), buffer.readVarIntArray());
    }

    /** Client thread. */
    public void handle() {
        handler.accept(this);
    }
}
