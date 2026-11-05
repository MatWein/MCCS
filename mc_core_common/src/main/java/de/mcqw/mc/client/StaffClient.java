package de.mcqw.mc.client;

import de.mcqw.core.staff.ShieldAnimation;
import de.mcqw.mc.staff.StaffData;
import de.mcqw.mc.staff.StaffShield;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** The client side of the shield: the key and the fade and flash of every visible player's bubble. */
public final class StaffClient {
    /** Sends the toggle message to the server; set by the loader. */
    public static Runnable toggleSender = () -> {};

    private static final Map<Player, ShieldAnimation> BUBBLES = new HashMap<>();

    private StaffClient() {}

    public static void onClientTickEnd() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            BUBBLES.clear();
            return;
        }
        while (KeyBindings.SHIELD.consumeClick()) {
            if (!StaffShield.heldStaff(minecraft.player).isEmpty()) toggleSender.run();
        }
        Set<Player> seen = new HashSet<>();
        for (Player player : minecraft.level.players()) {
            seen.add(player);
            ItemStack active = StaffShield.activeStaff(player);
            ShieldAnimation animation = BUBBLES.get(player);
            if (animation == null) {
                if (active.isEmpty()) continue;
                animation = new ShieldAnimation();
                BUBBLES.put(player, animation);
            }
            animation.tick(!active.isEmpty(), active.isEmpty() ? 0 : StaffData.shieldHits(active));
        }
        BUBBLES.entrySet().removeIf(entry -> !seen.contains(entry.getKey()) || entry.getValue().isGone());
    }

    /** The players whose shield is visible, with its animation. */
    public static Map<Player, ShieldAnimation> bubbles() {
        return BUBBLES;
    }
}
