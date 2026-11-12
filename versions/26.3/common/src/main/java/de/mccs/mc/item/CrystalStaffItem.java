package de.mccs.mc.item;

import de.mccs.core.staff.StaffKind;
import de.mccs.mc.staff.StaffBall;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** A crystal staff of one of the kinds (normal, high-energy), which all work alike. Its abilities are in the staff package; holding the use key in the main hand charges the energy ball. */
public class CrystalStaffItem extends Item {
    private static final int USE_DURATION = 72000;

    private final StaffKind kind;

    public CrystalStaffItem(Properties properties, StaffKind kind) {
        super(properties);
        this.kind = kind;
    }

    public StaffKind kind() {
        return kind;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND || player.isPassenger()) return InteractionResult.PASS;
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return USE_DURATION;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remaining) {
        StaffBall.charging(entity, USE_DURATION - remaining);
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remaining) {
        if (entity instanceof ServerPlayer player) StaffBall.fire(player, USE_DURATION - remaining);
        return true;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return StaffBar.width(stack);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return StaffBar.color(stack);
    }
}
