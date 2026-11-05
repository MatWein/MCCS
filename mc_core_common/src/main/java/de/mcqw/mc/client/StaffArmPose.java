package de.mcqw.mc.client;

import net.minecraft.client.model.geom.ModelPart;

/**
 * Keeps the arm holding the staff still, so the staff stays upright while walking instead of swinging with the arm.
 * Crouching, riding and attacking are added by Minecraft afterwards and still apply.
 */
public final class StaffArmPose {
    /**
     * Vanilla's angle for an arm holding an item. The display rotation of the staff in third person
     * (models/item/crystal_staff.json) cancels it out: change one and the other has to follow.
     */
    private static final float ARM_X_ROT = (float) (-Math.PI / 10);

    private StaffArmPose() {}

    public static void apply(ModelPart arm) {
        arm.xRot = ARM_X_ROT;
        arm.yRot = 0.0F;
    }
}
