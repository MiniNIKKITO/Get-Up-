package com.nicolas.getup.client;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

/** Dynamic client-side instance for Minecraft's vanilla Music Disc 13. */
final class GetUpMusicSoundInstance extends AbstractTickableSoundInstance {
    private static final float START_PITCH = 1.0f;
    private static final float MAX_PITCH = 2.0f;

    private boolean pitchTransition;
    private long pitchStartMs;
    private long pitchDurationMs = 1L;

    private boolean fadingOut;
    private long fadeStartMs;
    private long fadeDurationMs = 1L;

    GetUpMusicSoundInstance() {
        super(SoundEvents.MUSIC_DISC_13.value(), SoundSource.RECORDS, SoundInstance.createUnseededRandom());
        this.volume = 1.0f;
        this.pitch = START_PITCH;
        this.looping = false;
        this.relative = true;
        this.attenuation = SoundInstance.Attenuation.NONE;
    }

    void beginPitchTransition(long durationMs) {
        this.pitchTransition = true;
        this.pitchStartMs = System.currentTimeMillis();
        this.pitchDurationMs = Math.max(1L, durationMs);
    }

    void beginFadeOut(long durationMs) {
        this.fadingOut = true;
        this.fadeStartMs = System.currentTimeMillis();
        this.fadeDurationMs = Math.max(1L, durationMs);
        this.volume = 1.0f;
    }

    void setFadeOutProgress(float progress) {
        if (!this.fadingOut) {
            return;
        }
        this.volume = 1.0f - Math.clamp(progress, 0.0f, 1.0f);
    }

    @Override
    public void tick() {
        if (this.pitchTransition) {
            float progress = Math.clamp(
                    (System.currentTimeMillis() - this.pitchStartMs) / (float) this.pitchDurationMs,
                    0.0f,
                    1.0f
            );
            this.pitch = START_PITCH + (MAX_PITCH - START_PITCH) * progress;
        }

        if (this.fadingOut) {
            float progress = Math.clamp(
                    (System.currentTimeMillis() - this.fadeStartMs) / (float) this.fadeDurationMs,
                    0.0f,
                    1.0f
            );
            this.volume = 1.0f - progress;
            if (progress >= 1.0f) {
                this.stop();
            }
        }
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }
}
