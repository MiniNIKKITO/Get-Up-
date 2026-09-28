package com.nicolas.getup.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Overlay;

/**
 * Native Minecraft overlay used only for the post-respawn white recovery.
 *
 * It deliberately uses the vanilla Gui#setOverlay mechanism rather than a Fabric HUD element,
 * so it can remain visible while Minecraft closes the death screen during respawn.
 */
public final class DeathTransitionOverlay extends Overlay {
    private final long startMs = System.currentTimeMillis();
    private static DeathTransitionOverlay ACTIVE;

    public DeathTransitionOverlay() {
        ACTIVE = this;
        ExposureEffect.start();
        // Disc 13 starts fading out at full white and finishes after half of the configured
        // exposure-recovery duration. Vanilla game audio still fades in over the full duration.
        long discFadeMs = Math.max(1L, GetUpConfig.exposureFadeMs() / 2L);
        GetUpMusicController.beginFadeOut(discFadeMs);
    }

    @Override
    public boolean isPausing() {
        return false;
    }

    @Override
    public void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        long elapsed = elapsedMs();

        float exposureFadeMs = GetUpConfig.exposureFadeMs();
        float progress = clamp01(elapsed / exposureFadeMs);

        // Logarithmic-style recovery: the extreme overexposure disappears quickly,
        // then the remaining exposure eases out more gradually toward normal.
        // This avoids keeping the screen near pure white for most of the transition.
        final float logScale = 31.0f;
        float curve = (float) (Math.log1p(logScale * progress) / Math.log1p(logScale));
        ExposureEffect.setExposure(32.0f * (1.0f - curve));

        // From the instant the screen reaches maximum white, both audio transitions run for
        // the full exposure recovery: vanilla game audio fades in while Disc 13 fades out.
        DeathAudioController.setGameAudioMultiplier(minecraft, progress);
        // The Disc 13 fade is intentionally half the exposure duration.
        GetUpMusicController.updateFadeOut(Math.clamp(progress * 2.0f, 0.0f, 1.0f));

        if (progress >= 1.0f) {
            GetUpMusicController.stop(minecraft);
            ExposureEffect.stop();
            ACTIVE = null;
            DeathAudioController.restore(minecraft);
            minecraft.gui.setOverlay(null);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        // No white GUI layer here: the post-processing shader exposes the actual world image.
    }

    public static float hudOpacity() {
        return ACTIVE == null ? 1.0f : ACTIVE.currentRecoveryProgress();
    }

    private float currentRecoveryProgress() {
        long elapsed = elapsedMs();
        float exposureFadeMs = GetUpConfig.exposureFadeMs();
        if (exposureFadeMs <= 0.0f) {
            return 1.0f;
        }

        float progress = clamp01(elapsed / exposureFadeMs);
        final float logScale = 31.0f;
        return (float) (Math.log1p(logScale * progress) / Math.log1p(logScale));
    }

    private long elapsedMs() {
        return Math.max(0L, System.currentTimeMillis() - this.startMs);
    }

    private static float clamp01(float value) {
        return Math.clamp(value, 0.0f, 1.0f);
    }
}
