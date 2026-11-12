package de.mccs.mc.network;

import de.mccs.core.staff.StaffAction;
import de.mccs.mc.staff.StaffSystems;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

/** The player pressed the key for a staff system. */
public record StaffActionPacket(StaffAction action) {
    public static void encode(StaffActionPacket packet, FriendlyByteBuf buffer) {
        buffer.writeEnum(packet.action);
    }

    public static StaffActionPacket decode(FriendlyByteBuf buffer) {
        return new StaffActionPacket(buffer.readEnum(StaffAction.class));
    }

    public void handle(ServerPlayer sender) {
        StaffSystems.perform(sender, action);
    }
}
