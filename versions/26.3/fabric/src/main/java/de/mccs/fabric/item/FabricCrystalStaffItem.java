package de.mccs.fabric.item;

import de.mccs.core.staff.StaffKind;
import de.mccs.mc.item.CrystalStaffItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** The stack changes constantly while the shield runs: this must neither replay the equip animation nor restart breaking a block. */
public final class FabricCrystalStaffItem extends CrystalStaffItem {
    public FabricCrystalStaffItem(Properties properties, StaffKind kind) {
        super(properties, kind);
    }

    @Override
    public boolean allowComponentsUpdateAnimation(Player player, InteractionHand hand, ItemStack oldStack, ItemStack newStack) {
        return false;
    }

    @Override
    public boolean allowContinuingBlockBreaking(Player player, ItemStack oldStack, ItemStack newStack) {
        return true;
    }
}
