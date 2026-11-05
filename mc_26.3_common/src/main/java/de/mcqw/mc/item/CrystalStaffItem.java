package de.mcqw.mc.item;

import de.mcqw.mc.staff.StaffBall;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/** The crystal staff. Its abilities are in the staff package; a right click in the main hand fires the energy ball. */
public class CrystalStaffItem extends Item {
    public CrystalStaffItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND || player.isPassenger()) return InteractionResult.PASS;
        if (player instanceof ServerPlayer serverPlayer) StaffBall.fire(serverPlayer);
        return InteractionResult.CONSUME;
    }
}
