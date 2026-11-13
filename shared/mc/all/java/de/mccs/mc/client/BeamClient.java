package de.mccs.mc.client;

import de.mccs.core.config.ServerConfig;
import de.mccs.core.render.LightningBolt;
import de.mccs.core.staff.StaffAction;
import de.mccs.mc.network.BeamStatePacket;
import de.mccs.mc.staff.BeamAim;
import de.mccs.mc.staff.StaffSystems;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * The client side of the lightning: the attack key, and what the server said about everyone's lightning. The picture is
 * made here, each Minecraft version only draws the resulting paths.
 */
public final class BeamClient {
    /** Without news from the server the lightning ends: the client may have missed that. */
    private static final int EXPIRY_TICKS = 30;
    /** A new zigzag this often. */
    private static final int ZIGZAG_TICKS = 2;
    private static final float MAIN_AMPLITUDE = 0.28f;
    private static final float CHAIN_AMPLITUDE = 0.22f;
    private static final float TETHER_AMPLITUDE = 0.07f;
    private static final float TETHER_SCALE = 0.5f;
    private static final float CHAIN_SCALE = 0.7f;
    private static final float SECOND_STRAND_SCALE = 0.6f;
    private static final int SPARKS = 2;
    private static final double SPARK_SPREAD = 0.3;
    private static final double SPARK_SPEED = 0.15;

    // The crystal relative to the camera in first person, from the display transform in tools/make_staff_assets.py.
    // Held items have their own field of view: the sideways offsets are scaled from it to the world's.
    private static final double FIRST_PERSON_RIGHT = 0.618;
    private static final double FIRST_PERSON_UP = 0.02;
    private static final double FIRST_PERSON_FORWARD = 0.814;
    private static final double HAND_FOV_DEGREES = 70;

    private static final class Beam {
        private int[] targets;
        private boolean tether;
        private int age;
    }

    private static final Map<Integer, Beam> BEAMS = new HashMap<>();
    private static boolean firing;

    private BeamClient() {}

    /** The attack key belongs to the lightning while the staff is in the main hand: no mining, no melee. */
    public static boolean takesAttackKey() {
        Player player = Minecraft.getInstance().player;
        return player != null && !player.isSpectator() && !StaffSystems.mainStaff(player).isEmpty();
    }

    public static void update(BeamStatePacket packet) {
        if (!packet.firing()) {
            BEAMS.remove(packet.shooter());
            return;
        }
        Beam beam = BEAMS.computeIfAbsent(packet.shooter(), id -> new Beam());
        beam.targets = packet.targets();
        beam.tether = packet.tether();
        beam.age = 0;
    }

    /** What the lightning or telekinesis of this player uses in FE/tick. */
    public static int drain(Player player) {
        Beam beam = BEAMS.get(player.getId());
        if (beam == null) return 0;
        return beam.tether ? ServerConfig.TELEKINESIS.drain() : ServerConfig.BEAM.drain();
    }

    static void onClientTickEnd() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            BEAMS.clear();
            firing = false;
            return;
        }
        boolean wanted = takesAttackKey() && !ClientCompat.screenOpen(minecraft) && minecraft.options.keyAttack.isDown();
        if (wanted != firing) {
            firing = wanted;
            StaffClient.actionSender.accept(wanted ? StaffAction.BEAM_START : StaffAction.BEAM_STOP);
        }
        BEAMS.values().removeIf(beam -> ++beam.age > EXPIRY_TICKS);
        BEAMS.forEach((shooter, beam) -> sparks(minecraft, shooter, beam));
    }

    private static void sparks(Minecraft minecraft, int shooter, Beam beam) {
        if (beam.targets.length == 0) {
            if (minecraft.level.getEntity(shooter) instanceof Player player) {
                sparksAt(minecraft, BeamAim.end(player, ServerConfig.BEAM.range(), 1f));
            }
            return;
        }
        for (int id : beam.targets) {
            Entity target = minecraft.level.getEntity(id);
            if (target != null) sparksAt(minecraft, BeamAim.center(target));
        }
    }

    private static void sparksAt(Minecraft minecraft, Vec3 point) {
        var random = minecraft.level.getRandom();
        for (int i = 0; i < SPARKS; i++) {
            minecraft.level.addParticle(ParticleTypes.ELECTRIC_SPARK,
                    point.x + (random.nextDouble() - 0.5) * SPARK_SPREAD, point.y + (random.nextDouble() - 0.5) * SPARK_SPREAD, point.z + (random.nextDouble() - 0.5) * SPARK_SPREAD,
                    (random.nextDouble() - 0.5) * SPARK_SPEED, (random.nextDouble() - 0.5) * SPARK_SPEED, (random.nextDouble() - 0.5) * SPARK_SPEED);
        }
    }

    /** Hands over this frame's lightning paths, relative to the camera, with their width scale. */
    public static void forEachPath(Camera camera, Vec3 cameraPosition, float partialTick, BiConsumer<float[][], Float> paths) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || BEAMS.isEmpty()) return;
        BEAMS.forEach((shooter, beam) -> {
            if (!(minecraft.level.getEntity(shooter) instanceof Player player)) return;
            long seed = (player.tickCount / ZIGZAG_TICKS) * 31L + shooter;
            Vec3 start = crystal(minecraft, camera, player, player.getMainArm(), cameraPosition, partialTick);
            Vec3 first = beam.targets.length > 0 && minecraft.level.getEntity(beam.targets[0]) != null
                    ? BeamAim.center(minecraft.level.getEntity(beam.targets[0]))
                    : BeamAim.end(player, ServerConfig.BEAM.range(), partialTick);
            float[] from = relative(start, cameraPosition);
            float[] to = relative(first, cameraPosition);
            if (beam.tether) {
                // the telekinesis: one calm line instead of the lightning
                paths.accept(LightningBolt.path(from, to, seed, TETHER_AMPLITUDE), TETHER_SCALE);
                return;
            }
            paths.accept(LightningBolt.path(from, to, seed, MAIN_AMPLITUDE), 1f);
            paths.accept(LightningBolt.path(from, to, seed + 1, MAIN_AMPLITUDE), SECOND_STRAND_SCALE);
            Vec3 previous = first;
            for (int i = 1; i < beam.targets.length; i++) {
                Entity target = minecraft.level.getEntity(beam.targets[i]);
                if (target == null) continue;
                Vec3 next = BeamAim.center(target);
                paths.accept(LightningBolt.path(relative(previous, cameraPosition), relative(next, cameraPosition), seed + 2L * i, CHAIN_AMPLITUDE), CHAIN_SCALE);
                previous = next;
            }
        });
    }

    private static float[] relative(Vec3 point, Vec3 camera) {
        return new float[]{(float) (point.x - camera.x), (float) (point.y - camera.y), (float) (point.z - camera.z)};
    }

    /** Whether this is the view from inside the player's eyes, where held items are drawn separately. */
    static boolean isFirstPerson(Minecraft minecraft, Player player) {
        return player == minecraft.player && minecraft.getCameraEntity() == player && minecraft.options.getCameraType().isFirstPerson();
    }

    /** Where the crystal of a staff in the given arm appears in this frame. */
    static Vec3 crystal(Minecraft minecraft, Camera camera, Player player, HumanoidArm arm, Vec3 cameraPosition, float partialTick) {
        if (!isFirstPerson(minecraft, player)) return BeamAim.crystal(player, arm, partialTick);
        float[][] basis = ClientCompat.cameraBasis(camera);
        double fovScale = Math.tan(Math.toRadians(minecraft.options.fov().get()) / 2) / Math.tan(Math.toRadians(HAND_FOV_DEGREES) / 2);
        double right = (arm == HumanoidArm.LEFT ? -FIRST_PERSON_RIGHT : FIRST_PERSON_RIGHT) * fovScale;
        double up = FIRST_PERSON_UP * fovScale;
        return cameraPosition
                .add(basis[0][0] * FIRST_PERSON_FORWARD, basis[0][1] * FIRST_PERSON_FORWARD, basis[0][2] * FIRST_PERSON_FORWARD)
                .add(basis[1][0] * up, basis[1][1] * up, basis[1][2] * up)
                .add(-basis[2][0] * right, -basis[2][1] * right, -basis[2][2] * right);
    }
}
