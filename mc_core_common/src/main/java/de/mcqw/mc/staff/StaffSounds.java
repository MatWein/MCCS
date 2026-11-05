package de.mcqw.mc.staff;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

/** The staff's sounds, the same with the same volume and pitch as those of MCPJ's jumper; everyone nearby hears them. */
final class StaffSounds {
    private StaffSounds() {}

    /** Part of the lightning's crackle; it repeats as long as the lightning runs. */
    static void beamCrackle(ServerPlayer player, float pitch) {
        play(player, SoundEvents.LIGHTNING_BOLT_IMPACT, 0.25f, pitch);
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
