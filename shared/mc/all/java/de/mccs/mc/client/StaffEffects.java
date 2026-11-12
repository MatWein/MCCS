package de.mccs.mc.client;

import de.mccs.core.config.ServerConfig;
import de.mccs.core.render.CrystalRender;
import de.mccs.core.render.EnergyOrb;
import de.mccs.core.staff.BallMath;
import de.mccs.core.staff.StaffKind;
import de.mccs.mc.item.CrystalStaffItem;
import de.mccs.mc.staff.BeamAim;
import de.mccs.mc.staff.StaffSystems;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4fc;

/**
 * The staff's crystal on Minecraft's side: what of a stack decides its look, and the energy ball while it is being
 * charged. The crystal and its light come from {@link CrystalRender}; each Minecraft version calls {@link #draw} (in the
 * hands) or {@link #drawCrystal} (elsewhere) while it renders the staff item, with the item's own pose, so all of it sits
 * exactly on the staff in every view.
 */
public final class StaffEffects {
    private static final float SPARK_CHANCE = 0.08f;
    private static final double SPARK_SPREAD = 0.25;
    /** Marks a staff nobody is charging a ball with. */
    private static final float NOT_CHARGING = -1f;

    /** Receives the crystal and its light ({@link CrystalRender.Output}) and the ball being charged. */
    public interface Output extends CrystalRender.Output {
        /** The ball being charged at the crystal, in render space. */
        void orb(float[] center, float size, float charge, float time);
    }

    /** What decides the crystal of a staff stack in this frame. */
    public record Look(StaffKind kind, int stored, int capacity, int seed, float charge) {
        CrystalRender.State state(float time, boolean firstPerson) {
            return new CrystalRender.State(kind, stored, capacity, time, seed, firstPerson);
        }
    }

    private StaffEffects() {}

    /** The look of a staff stack; the ball's charge comes from the player who is charging with exactly this stack. */
    public static Look look(ItemStack stack) {
        return new Look(StaffSystems.kind(stack), StaffSystems.stored(stack), StaffSystems.capacity(stack),
                System.identityHashCode(stack), charge(stack));
    }

    private static float charge(ItemStack stack) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return NOT_CHARGING;
        for (Player player : minecraft.level.players()) {
            if (player.isUsingItem() && player.getUseItem().getItem() instanceof CrystalStaffItem
                    && player.getItemInHand(player.getUsedItemHand()) == stack) {
                return (float) BallMath.chargeShare(player.getTicksUsingItem(), ServerConfig.BALL.chargeTicks());
            }
        }
        return NOT_CHARGING;
    }

    /**
     * The crystal with all of its light, and the ball being charged, for a staff in a hand. {@code itemPose} maps the
     * item's model space to render space (the camera at its origin); {@code right} and {@code up} are the camera's
     * directions in render space. {@code firstPerson}: the staff is seen in first person, where the crystal keeps its
     * proportions by being drawn narrower and taller.
     */
    public static void draw(Look look, Matrix4fc itemPose, float[] right, float[] up, float time, boolean firstPerson, Output output) {
        float[] pose = itemPose.get(new float[16]);
        CrystalRender.State state = look.state(time, firstPerson);
        CrystalRender.draw(state, pose, right, up, output);
        if (look.charge() >= 0) {
            float fullSize = (float) ServerConfig.BALL.chargedSizeFactor();
            output.orb(CrystalRender.center(state, pose), EnergyOrb.previewSize(look.charge(), fullSize) * CrystalRender.scale(pose), look.charge(), time);
        }
    }

    /** The crystal alone: for the inventory, the ground and item frames. */
    public static void drawCrystal(Look look, float time, CrystalRender.Output output) {
        CrystalRender.drawBody(look.state(time, false), output);
    }

    /** Sparks fly off well charged crystals of other players (in first person they would fly in front of the eyes). */
    static void onClientTickEnd(Minecraft minecraft) {
        for (Player player : minecraft.level.players()) {
            ItemStack staff = StaffSystems.heldStaff(player);
            if (staff.isEmpty() || StaffClient.isHidden(player) || BeamClient.isFirstPerson(minecraft, player)) continue;
            if (!CrystalRender.crackles(StaffSystems.stored(staff), StaffSystems.capacity(staff))) continue;
            var random = minecraft.level.getRandom();
            if (random.nextFloat() >= SPARK_CHANCE) continue;
            Vec3 crystal = BeamAim.crystal(player, arm(player, staff), 1f);
            minecraft.level.addParticle(ParticleTypes.ELECTRIC_SPARK,
                    crystal.x + (random.nextDouble() - 0.5) * SPARK_SPREAD, crystal.y + (random.nextDouble() - 0.5) * SPARK_SPREAD, crystal.z + (random.nextDouble() - 0.5) * SPARK_SPREAD,
                    0, 0, 0);
        }
    }

    private static HumanoidArm arm(Player player, ItemStack staff) {
        return staff == player.getMainHandItem() ? player.getMainArm() : player.getMainArm().getOpposite();
    }
}
