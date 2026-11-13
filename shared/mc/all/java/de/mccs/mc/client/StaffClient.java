package de.mccs.mc.client;

import de.mccs.core.config.ServerConfig;
import de.mccs.core.render.EnergyBubble;
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

    /** One visible bubble in this frame; {@code inside}: the camera sits inside it (the own bubble in first person). */
    public record Bubble(Player player, float intensity, float wobble, float time, boolean inside) {}

    /** Hands over the bubbles visible in this frame; each Minecraft version only draws them. */
    public static void forEachBubble(float partialTick, Consumer<Bubble> bubbles) {
        Minecraft minecraft = Minecraft.getInstance();
        BUBBLES.forEach((player, animation) -> {
            float shield = animation.shield(partialTick);
            float flash = animation.flash(partialTick);
            boolean inside = BeamClient.isFirstPerson(minecraft, player);
            float intensity = inside ? EnergyBubble.insideIntensity(shield, flash)
                    : EnergyBubble.intensity(shield, animation.cloak(partialTick), flash, player == minecraft.player);
            if (EnergyBubble.isVisible(intensity)) {
                bubbles.accept(new Bubble(player, intensity, EnergyBubble.wobble(shield), player.tickCount + partialTick, inside));
            }
        });
    }

    /** Whether the cloak hides this player's body and everything they carry. */
    public static boolean isHidden(Player player) {
        BubbleAnimation animation = BUBBLES.get(player);
        return animation != null && animation.isHidden();
    }

    /**
     * Whether a player's shield is under fire as far as this client can tell (the server keeps its own count): it took a hit
     * within the configured time, which raises what shield and cloak use.
     */
    public static boolean isUnderFire(Player player) {
        BubbleAnimation animation = BUBBLES.get(player);
        return animation != null && animation.ticksSinceHit() <= ServerConfig.STAFF.underFireTicks();
    }

    /** Whether the cloak hides the local player's own first person hand and what it holds. */
    public static boolean hidesOwnHand() {
        Player player = Minecraft.getInstance().player;
        return player != null && isHidden(player);
    }
}
