package com.nicolas.getup.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.resources.sounds.SoundInstance;
import com.nicolas.getup.mixin.SoundManagerAccessor;
import net.minecraft.sounds.SoundSource;

/**
 * Runtime-only master audio multiplier for the death presentation.
 *
 * The user's actual Minecraft volume sliders are never modified. Instead, the SoundEngine
 * mixin multiplies the volume calculated by Minecraft by this value. This lets us smoothly
 * fade all game audio back in without changing persistent options.
 */
public final class DeathAudioController {
    private static volatile float gameAudioMultiplier = 1.0f;

    private DeathAudioController() {
    }

    public static void beginDeathMute(Minecraft minecraft) {
        gameAudioMultiplier = 0.0f;
        // Remove anything that was already playing at the instant of death, including music,
        // block sounds and looping/ticking instances. New sounds are also kept silent by the mixin.
        stopAll(minecraft);
    }

    public static void stopAll(Minecraft minecraft) {
        SoundManagerAccessor soundManager = (SoundManagerAccessor) (Object) minecraft.getSoundManager();
        soundManager.getup$getSoundEngine().stopAll();
    }

    public static void setGameAudioMultiplier(Minecraft minecraft, float multiplier) {
        gameAudioMultiplier = clamp01(multiplier);
        refreshAllCategories(minecraft);
    }

    public static void restore(Minecraft minecraft) {
        gameAudioMultiplier = 1.0f;
        refreshAllCategories(minecraft);
    }

    /**
     * Called from the SoundEngine mixin after vanilla has calculated the sound's normal volume.
     */
    public static float applyMultiplier(SoundInstance sound, float vanillaVolume) {
        // Music Disc 13 is deliberately controlled independently by GetUpMusicSoundInstance.
        if (GetUpMusicController.isOurSound(sound)) {
            return vanillaVolume;
        }
        return vanillaVolume * gameAudioMultiplier;
    }

    private static void refreshAllCategories(Minecraft minecraft) {
        // Recalculate the volume of already-active channels so runtime fades affect sounds that
        // were already playing, not merely newly-created sounds.
        SoundEngine soundEngine = ((SoundManagerAccessor) (Object) minecraft.getSoundManager()).getup$getSoundEngine();
        for (SoundSource source : SoundSource.values()) {
            soundEngine.refreshCategoryVolume(source);
        }
    }

    private static float clamp01(float value) {
        return Math.clamp(value, 0.0f, 1.0f);
    }
}
