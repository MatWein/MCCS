package de.mcqw.mc.staff;

import de.mcqw.mc.compat.StaffCompat;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** The staff's sounds, the same with the same volume and pitch as those of MCPJ's jumper; everyone nearby hears them. */
final class StaffSounds {
    private StaffSounds() {}

    /** Part of the lightning's crackle; it repeats as long as the lightning runs. */
    static void beamCrackle(ServerPlayer player, float pitch) {
        play(player, SoundEvents.LIGHTNING_BOLT_IMPACT, 0.25f, pitch);
    }

    /** The sounds of the jumper's drone launch. */
    static void ballLaunched(ServerPlayer player) {
        play(player, SoundEvents.FIRECHARGE_USE, 0.8f, 1.8f);
        play(player, SoundEvents.AMETHYST_BLOCK_CHIME, 1.5f, 0.6f);
    }

    static void ballExploded(Level level, Vec3 position) {
        level.playSound(null, position.x, position.y, position.z, StaffCompat.explodeSound(), SoundSource.PLAYERS, 2.0f, 1.3f);
    }

    static void ballCharging(ServerPlayer player, float pitch) {
        play(player, SoundEvents.AMETHYST_BLOCK_CHIME, 1.0f, pitch);
    }

    static void ballFullyCharged(ServerPlayer player) {
        play(player, SoundEvents.BEACON_POWER_SELECT, 0.6f, 1.6f);
    }

    /** At the given place, for the start and the end of a teleport. */
    static void teleported(Level level, Vec3 position) {
        level.playSound(null, position.x, position.y, position.z, SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.2f);
    }

    static void lightsToggled(ServerPlayer player, boolean on) {
        play(player, SoundEvents.LEVER_CLICK, 0.6f, on ? 1.2f : 0.9f);
    }

    static void blockDamageToggled(ServerPlayer player, boolean on) {
        play(player, SoundEvents.LEVER_CLICK, 0.6f, on ? 0.8f : 0.6f);
    }

    static void telekinesisLifted(ServerPlayer player) {
        play(player, SoundEvents.ILLUSIONER_CAST_SPELL, 0.8f, 1.4f);
    }

    static void telekinesisReleased(ServerPlayer player) {
        play(player, SoundEvents.CONDUIT_DEACTIVATE, 0.5f, 1.6f);
    }

    static void telekinesisThrown(ServerPlayer player) {
        play(player, SoundEvents.FIRECHARGE_USE, 0.8f, 0.8f);
    }

    static void shieldHit(ServerPlayer player) {
        play(player, SoundEvents.AMETHYST_BLOCK_RESONATE, 0.8f, 1.6f);
    }

    static void shieldToggled(ServerPlayer player, boolean on) {
        // conduit: a shimmering, watery energy hum that matches the bubble's water look
        play(player, on ? SoundEvents.CONDUIT_ACTIVATE : SoundEvents.CONDUIT_DEACTIVATE, 1.0f, on ? 1.2f : 1.4f);
    }

    static void shieldCollapsed(ServerPlayer player) {
        play(player, SoundEvents.BEACON_DEACTIVATE, 1.2f, 0.8f);
    }

    static void cloakToggled(ServerPlayer player, boolean on) {
        // illusioner vanishing into its mirror images
        play(player, SoundEvents.ILLUSIONER_MIRROR_MOVE, 1.0f, on ? 1.2f : 0.8f);
    }

    static void powerFailure(ServerPlayer player) {
        play(player, SoundEvents.CONDUIT_DEACTIVATE, 1.5f, 0.7f);
    }

    static void denied(ServerPlayer player) {
        play(player, SoundEvents.DISPENSER_FAIL, 0.8f, 1.2f);
    }

    private static void play(ServerPlayer player, SoundEvent sound, float volume, float pitch) {
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), sound, SoundSource.PLAYERS, volume, pitch);
    }
}
