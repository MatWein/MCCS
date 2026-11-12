package de.mccs.mc.item;

import de.mccs.core.config.ServerConfig;
import de.mccs.core.hud.HudColors;
import de.mccs.core.staff.StaffEnergy;
import de.mccs.mc.staff.StaffSystems;
import net.minecraft.world.item.ItemStack;

/** The energy bar on the item. */
public final class StaffBar {
    private static final int LOW_LOOK = 3;

    private StaffBar() {}

    public static int width(ItemStack stack) {
        return StaffEnergy.barWidth(StaffSystems.stored(stack), ServerConfig.STAFF.capacity());
    }

    public static int color(ItemStack stack) {
        boolean low = StaffEnergy.crystalLook(StaffSystems.stored(stack), ServerConfig.STAFF.capacity()) == LOW_LOOK;
        return low ? HudColors.RED : HudColors.TEAL;
    }
}
