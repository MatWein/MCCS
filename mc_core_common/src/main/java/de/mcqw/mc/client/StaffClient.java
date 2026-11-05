package de.mcqw.mc.client;

import de.mcqw.core.staff.BubbleAnimation;
import de.mcqw.core.staff.StaffAction;
import de.mcqw.mc.staff.StaffData;
import de.mcqw.mc.staff.StaffSystems;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/** The client side of the staff: the keys and the fades and flash of every visible player's bubble. */
public final class StaffClient {
    /** Sends an action to the server; set by the loader. */
    public static Consumer<StaffAction> actionSender = action -> {};

    private static final Map<Player, BubbleAnimation> BUBBLES = new HashMap<>();

    private StaffClient() {}

    public static void onClientTickEnd() {
        Minecraft minecraft = Minecraft.getInstance();
        BeamClient.onClientTickEnd();
        if (minecraft.level == null || minecraft.player == null) {
            BUBBLES.clear();
            return;
        }
        StaffEffects.onClientTickEnd(minecraft);
        pressed(minecraft, KeyBindings.SHIELD, StaffAction.SHIELD);
        pressed(minecraft, KeyBindings.CLOAK, StaffAction.CLOAK);
        pressed(minecraft, KeyBindings.TELEPORT, StaffAction.TELEPORT);
        Set<Player> seen = new HashSet<>();
        for (Player player : minecraft.level.players()) {
            seen.add(player);
            ItemStack shield = StaffSystems.activeShield(player);
            boolean cloak = !StaffSystems.activeCloak(player).isEmpty();
            BubbleAnimation animation = BUBBLES.get(player);
            if (animation == null) {
                if (shield.isEmpty() && !cloak) continue;
                animation = new BubbleAnimation();
                BUBBLES.put(player, animation);
            }
            animation.tick(!shield.isEmpty(), shield.isEmpty() ? 0 : StaffData.shieldHits(shield), cloak);
        }
        BUBBLES.entrySet().removeIf(entry -> !seen.contains(entry.getKey()) || entry.getValue().isGone());
    }

    private static void pressed(Minecraft minecraft, KeyMapping key, StaffAction action) {
        while (key.consumeClick()) {
            if (!StaffSystems.heldStaff(minecraft.player).isEmpty()) actionSender.accept(action);
        }
    }

    /** The players whose bubble is visible, with its animation. */
    public static Map<Player, BubbleAnimation> bubbles() {
        return BUBBLES;
    }

    /** Whether the cloak hides this player's body and everything they carry. */
    public static boolean isHidden(Player player) {
        BubbleAnimation animation = BUBBLES.get(player);
        return animation != null && animation.isHidden();
    }

    /** Whether the cloak hides the local player's own first person hand and what it holds. */
    public static boolean hidesOwnHand() {
        Player player = Minecraft.getInstance().player;
        return player != null && isHidden(player);
    }
}
