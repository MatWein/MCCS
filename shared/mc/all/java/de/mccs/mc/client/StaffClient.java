package de.mccs.mc.client;

import de.mccs.core.staff.BubbleAnimation;
import de.mccs.core.staff.StaffAction;
import de.mccs.mc.staff.StaffData;
import de.mccs.mc.staff.StaffSystems;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
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

    /** Before Minecraft handles its keys: with the staff in hand L is the light, the middle mouse button the telekinesis. */
    public static void onClientTickStart() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || StaffSystems.heldStaff(minecraft.player).isEmpty()) return;
        discardIfShared(minecraft.options.keyAdvancements, KeyBindings.LIGHTS);
        discardIfShared(minecraft.options.keyPickItem, KeyBindings.TELEKINESIS);
    }

    private static void discardIfShared(KeyMapping vanilla, KeyMapping ours) {
        if (!vanilla.same(ours)) return;
        boolean pending = vanilla.consumeClick();
        while (pending) pending = vanilla.consumeClick();
    }

    /** Whether using this hand must not replay the item's equip animation: the staff just stays in the picture. */
    public static boolean keepsHandSteady(InteractionHand hand) {
        Player player = Minecraft.getInstance().player;
        return player != null && hand == InteractionHand.MAIN_HAND && !StaffSystems.mainStaff(player).isEmpty();
    }

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
        pressed(minecraft, KeyBindings.LIGHTS, StaffAction.LIGHTS);
        pressed(minecraft, KeyBindings.BLOCK_DAMAGE, StaffAction.BLOCK_DAMAGE);
        pressed(minecraft, KeyBindings.TELEKINESIS, StaffAction.TELEKINESIS);
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
