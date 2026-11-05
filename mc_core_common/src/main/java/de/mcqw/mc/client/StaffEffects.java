package de.mcqw.mc.client;

import de.mcqw.core.config.ServerConfig;
import de.mcqw.core.render.CrystalGlow;
import de.mcqw.core.render.EnergyOrb;
import de.mcqw.core.staff.BallMath;
import de.mcqw.mc.item.CrystalStaffItem;
import de.mcqw.mc.staff.BeamAim;
import de.mcqw.mc.staff.StaffSystems;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * What a staff in a player's hand shows at its crystal: its light, electric arcs when charged well, and the energy ball while
 * it is being charged. The picture is made here, each Minecraft version only draws it.
 */
public final class StaffEffects {
    /** Held items look smaller in first person: everything at the crystal does too. */
    private static final float FIRST_PERSON_SCALE = 0.42f;
    private static final int ARCS = 2;
    private static final float ARC_WIDTH = 0.35f;
    private static final float SPARK_CHANCE = 0.08f;
    private static final double SPARK_SPREAD = 0.25;

    /** Receives this frame's effects, relative to the camera. */
    public interface Output {
        void halo(CrystalGlow.Halo halo, float[] center, float[] right, float[] up, float scale);

        void arc(float[][] path, float scale);

        void orb(float[] center, float size, float charge, float time);
    }

    private StaffEffects() {}

    static void onClientTickEnd(Minecraft minecraft) {
        for (Player player : minecraft.level.players()) {
            ItemStack staff = StaffSystems.heldStaff(player);
            if (staff.isEmpty() || StaffClient.isHidden(player) || BeamClient.isFirstPerson(minecraft, player)) continue;
            if (!CrystalGlow.crackles(StaffSystems.stored(staff), ServerConfig.STAFF.capacity())) continue;
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

    public static void collect(Camera camera, Vec3 cameraPosition, float partialTick, Output output) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;
        float[][] basis = ClientCompat.cameraBasis(camera);
        float[] up = basis[1];
        float[] right = {-basis[2][0], -basis[2][1], -basis[2][2]};
        for (Player player : minecraft.level.players()) {
            ItemStack staff = StaffSystems.heldStaff(player);
            if (staff.isEmpty() || StaffClient.isHidden(player)) continue;
            float scale = BeamClient.isFirstPerson(minecraft, player) ? FIRST_PERSON_SCALE : 1f;
            Vec3 crystal = BeamClient.crystal(minecraft, camera, player, arm(player, staff), cameraPosition, partialTick);
            float[] center = {(float) (crystal.x - cameraPosition.x), (float) (crystal.y - cameraPosition.y), (float) (crystal.z - cameraPosition.z)};
            float time = player.tickCount + partialTick;
            int stored = StaffSystems.stored(staff);
            int capacity = ServerConfig.STAFF.capacity();
            output.halo(CrystalGlow.halo(stored, capacity, time), center, right, up, scale);
            if (CrystalGlow.crackles(stored, capacity)) {
                for (int i = 0; i < ARCS; i++) {
                    float[][] arc = CrystalGlow.arc(center, scale, player.getId(), time, i);
                    if (arc != null) output.arc(arc, ARC_WIDTH * scale);
                }
            }
            if (player.isUsingItem() && player.getUseItem().getItem() instanceof CrystalStaffItem) {
                float charge = (float) BallMath.chargeShare(player.getTicksUsingItem(), ServerConfig.BALL.chargeTicks());
                float fullSize = (float) ServerConfig.BALL.chargedSizeFactor();
                output.orb(center, EnergyOrb.previewSize(charge, fullSize) * scale, charge, time);
            }
        }
    }
}
