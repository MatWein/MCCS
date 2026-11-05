package de.mcqw.mc.network;

import de.mcqw.mc.Mcqw;
import de.mcqw.mc.staff.StaffShield;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

/** The player pressed the shield key. */
public record ToggleShieldPacket() implements CustomPacketPayload {
    public static final Type<ToggleShieldPacket> TYPE = new Type<>(Mcqw.id("toggle_shield"));
    public static final StreamCodec<FriendlyByteBuf, ToggleShieldPacket> STREAM_CODEC = StreamCodec.unit(new ToggleShieldPacket());

    public void handle(ServerPlayer sender) {
        StaffShield.toggle(sender);
    }

    @Override
    public Type<ToggleShieldPacket> type() {
        return TYPE;
    }
}
