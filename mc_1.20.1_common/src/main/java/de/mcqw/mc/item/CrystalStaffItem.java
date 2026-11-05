package de.mcqw.mc.item;

import de.mcqw.mc.staff.StaffBall;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** The crystal staff. Its abilities are in the staff package; a right click in the main hand fires the energy ball. */
public class CrystalStaffItem extends Item {
    public CrystalStaffItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND || player.isPassenger()) return InteractionResultHolder.pass(stack);
        if (player instanceof ServerPlayer serverPlayer) StaffBall.fire(serverPlayer);
        return InteractionResultHolder.consume(stack);
    }
}
