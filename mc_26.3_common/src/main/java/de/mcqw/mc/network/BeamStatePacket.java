package de.mcqw.mc.network;

import de.mcqw.mc.Mcqw;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.function.Consumer;

/** A player's lightning for everyone seeing them: whether it runs, and the entities it goes through, in order. */
public record BeamStatePacket(int shooter, boolean firing, int[] targets) implements CustomPacketPayload {
    public static final Type<BeamStatePacket> TYPE = new Type<>(Mcqw.id("beam_state"));
    public static final StreamCodec<FriendlyByteBuf, BeamStatePacket> STREAM_CODEC = StreamCodec.ofMember(BeamStatePacket::encode, BeamStatePacket::decode);

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

    @Override
    public Type<BeamStatePacket> type() {
        return TYPE;
    }
}
