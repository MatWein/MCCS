package de.mcqw.mc.network;

import de.mcqw.mc.staff.StaffShield;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

/** The player pressed the shield key. */
public record ToggleShieldPacket() {
    public static void encode(ToggleShieldPacket packet, FriendlyByteBuf buffer) {}

    public static ToggleShieldPacket decode(FriendlyByteBuf buffer) {
        return new ToggleShieldPacket();
    }

    public void handle(ServerPlayer sender) {
        StaffShield.toggle(sender);
    }
}
