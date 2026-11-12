package de.mccs.mc.client;

import net.minecraft.client.model.geom.ModelPart;

/**
 * Holds the arm carrying the staff slightly away from the body and keeps it still, so the staff stays upright while
 * walking instead of swinging with the arm. Crouching, riding and attacking are added by Minecraft afterwards and
 * still apply.
 */
public final class StaffArmPose {
    /**
     * The display transform of the staff in third person (models/item/crystal_staff.json) cancels this pose out. When
     * changing these angles, also change ARM_FORWARD and ARM_OUTWARD in tools/make_staff_assets.py and run it.
     */
    private static final float ARM_FORWARD = (float) Math.toRadians(22.0);
    private static final float ARM_OUTWARD = (float) Math.toRadians(9.0);

    private StaffArmPose() {}

    public static void applyRight(ModelPart arm) {
        arm.xRot = -ARM_FORWARD;
        arm.yRot = 0.0F;
        arm.zRot = ARM_OUTWARD;
    }

    public static void applyLeft(ModelPart arm) {
        arm.xRot = -ARM_FORWARD;
        arm.yRot = 0.0F;
        arm.zRot = -ARM_OUTWARD;
    }
}
