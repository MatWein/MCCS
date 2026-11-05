package de.mcqw.mc.staff;

import de.mcqw.core.config.ServerConfig;
import de.mcqw.core.config.StaffConfig;
import de.mcqw.core.staff.StaffEnergy;
import de.mcqw.mc.item.CrystalStaffItem;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * The staff's shield. Its state lives on the item stack, which Minecraft syncs to everyone seeing the player. It only
 * works while the staff is held and the player is not riding anything (horse, boat, jumper, ...); otherwise the staff has
 * no effect at all: the shield pauses and resumes afterwards.
 */
public final class StaffShield {
    /** Energy is taken in steps, because each change of the stack goes over the network. */
    private static final int DRAIN_INTERVAL = 10;

    private StaffShield() {}

    /** The staff in the player's hands (main hand first), or an empty stack; riding players count as having none. */
    public static ItemStack heldStaff(LivingEntity entity) {
        if (entity.isPassenger()) return ItemStack.EMPTY;
        if (isStaff(entity.getMainHandItem())) return entity.getMainHandItem();
        return isStaff(entity.getOffhandItem()) ? entity.getOffhandItem() : ItemStack.EMPTY;
    }

    /** The held staff with its shield switched on, or an empty stack. */
    public static ItemStack activeStaff(LivingEntity entity) {
        if (entity.isPassenger()) return ItemStack.EMPTY;
        if (isStaff(entity.getMainHandItem()) && StaffData.shieldOn(entity.getMainHandItem())) return entity.getMainHandItem();
        if (isStaff(entity.getOffhandItem()) && StaffData.shieldOn(entity.getOffhandItem())) return entity.getOffhandItem();
        return ItemStack.EMPTY;
    }

    public static int stored(ItemStack stack) {
        StaffConfig config = ServerConfig.STAFF;
        return StaffEnergy.stored(StaffData.savedEnergy(stack), config.capacity(), config.startCharge());
    }

    /** The key for the shield was pressed. */
    public static void toggle(ServerPlayer player) {
        ItemStack active = activeStaff(player);
        if (!active.isEmpty()) {
            StaffData.setShieldOn(active, false);
            return;
        }
        ItemStack staff = heldStaff(player);
        if (staff.isEmpty()) return;
        if (!ServerConfig.STAFF.consumeEnergy() || stored(staff) > 0) StaffData.setShieldOn(staff, true);
    }

    public static void tick(MinecraftServer server) {
        boolean drainNow = server.getTickCount() % DRAIN_INTERVAL == 0;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            normalize(player.getMainHandItem());
            normalize(player.getOffhandItem());
            ItemStack active = activeStaff(player);
            if (active.isEmpty() || !ServerConfig.STAFF.consumeEnergy()) continue;
            if (drainNow) consume(active, ServerConfig.STAFF.shieldDrain() * (long) DRAIN_INTERVAL);
            if (stored(active) <= 0) StaffData.setShieldOn(active, false);
        }
    }

    /** Writes a new or over-full staff's real energy onto the stack, so it matches what everybody reads from it. */
    private static void normalize(ItemStack stack) {
        if (!isStaff(stack)) return;
        int stored = stored(stack);
        if (StaffData.savedEnergy(stack) != stored) StaffData.setSavedEnergy(stack, stored);
    }

    /**
     * Any damage to a player with an active shield, except what ignores invulnerability (/kill, the void). If the
     * energy for a hit is missing, the shield collapses and the hit goes through.
     *
     * @return true if the damage is cancelled
     */
    public static boolean absorb(Entity entity, DamageSource source, float amount) {
        if (!(entity instanceof ServerPlayer player) || player.getAbilities().invulnerable) return false;
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return false;
        ItemStack active = activeStaff(player);
        if (active.isEmpty()) return false;
        if (ServerConfig.STAFF.consumeEnergy() && !consume(active, StaffEnergy.hitCost(amount, ServerConfig.STAFF.shieldHitCost()))) {
            StaffData.setShieldOn(active, false);
            return false;
        }
        StaffData.setShieldHits(active, StaffData.shieldHits(active) + 1);
        return true;
    }

    /** Takes the amount; if it is not available the staff is emptied and false is returned. */
    private static boolean consume(ItemStack staff, long amount) {
        int stored = stored(staff);
        boolean enough = stored >= amount;
        StaffData.setSavedEnergy(staff, enough ? (int) (stored - amount) : 0);
        return enough;
    }

    private static boolean isStaff(ItemStack stack) {
        return stack.getItem() instanceof CrystalStaffItem;
    }
}
