package com.nicolas.getup.client;

import net.minecraft.client.Minecraft;

/**
 * Controls the vanilla Music Disc 13 resource used by the Get Up! death presentation.
 *
 * The sound itself is loaded from Minecraft's own resources; the mod does not ship a copy of
 * disc 13. The SoundInstance lets the pitch and volume be changed while it is playing.
 */
public final class GetUpMusicController {
    private static GetUpMusicSoundInstance instance;

    private GetUpMusicController() {
    }

    public static void start(Minecraft minecraft) {
        stop(minecraft);

        GetUpMusicSoundInstance music = new GetUpMusicSoundInstance();
        instance = music;
        minecraft.getSoundManager().play(music);
    }

    public static void beginPitchTransition(long durationMs) {
        GetUpMusicSoundInstance music = instance;
        if (music != null) {
            music.beginPitchTransition(Math.max(1L, durationMs));
        }
    }

    public static void beginFadeOut(long durationMs) {
        GetUpMusicSoundInstance music = instance;
        if (music != null) {
            music.beginFadeOut(Math.max(1L, durationMs));
        }
    }

    public static void updateFadeOut(float progress) {
        GetUpMusicSoundInstance music = instance;
        if (music != null) {
            music.setFadeOutProgress(progress);
        }
    }

    public static void stop(Minecraft minecraft) {
        GetUpMusicSoundInstance music = instance;
        if (music != null) {
            minecraft.getSoundManager().stop(music);
            instance = null;
        }
    }

    public static boolean isOurSound(Object sound) {
        return sound == instance && instance != null;
    }
}
