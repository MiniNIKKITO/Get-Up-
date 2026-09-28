package com.nicolas.getup.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Custom singleplayer death presentation.
 *
 * Death sequence:
 *   Timings are runtime-configurable through /getup commands.
 */
public final class GetUpScreen extends Screen {
    private static final Component YOU_DIED = Component.literal("You died");
    private static final Component GET_UP = Component.literal("Get Up!");
    private static final Component QUIT_MARK = Component.literal("X");

    private static final int JITTER_PIXELS = 1;
    private static final long QUIT_FADE_MS = 500L;

    private static final int GRAY = 0xFF8E8E8E;
    private static final int WHITE = 0xFFFFFFFF;

    private static final int QUIT_MARGIN = 9;
    private static final int QUIT_BOX_SIZE = 22;
    private static final int GET_UP_PAD_X = 10;
    private static final int GET_UP_PAD_Y = 7;

    private final Minecraft minecraft;
    private final long deathStartMs;

    private Phase phase = Phase.DEATH_SEQUENCE;
    private long transitionStartMs = -1L;
    private boolean hoveredGetUp;
    private boolean getUpActivated;
    private boolean hoveredQuit;
    private int jitterX;
    private int jitterY;

    private enum Phase {
        DEATH_SEQUENCE,
        TRANSITION,
        FINISHED
    }

    public GetUpScreen(LocalPlayer player) {
        super(Component.empty());
        this.minecraft = Minecraft.getInstance();
        this.deathStartMs = System.currentTimeMillis();
        DeathAudioController.beginDeathMute(this.minecraft);
        GetUpMusicController.start(this.minecraft);
    }

    public boolean isTransitioning() {
        return this.phase == Phase.TRANSITION;
    }

    public boolean isGetUpVisible() {
        return elapsedDeathMs() >= GetUpConfig.getUpMs();
    }

    @Override
    protected void init() {
        // The buttons are handled directly so their visuals stay text-only while the hitboxes
        // can be slightly larger than the glyphs.
    }

    @Override
    public void tick() {
        updateJitter();

        if (this.phase == Phase.DEATH_SEQUENCE) {
            // The SoundEngine mixin applies this multiplier to every newly-calculated sound, so
            // looping/ticking sounds stay silent without changing the user's saved volume sliders.
            DeathAudioController.setGameAudioMultiplier(this.minecraft, 0.0f);
            return;
        }

        if (this.phase != Phase.TRANSITION) {
            return;
        }

        long elapsed = transitionElapsedMs();
        DeathAudioController.setGameAudioMultiplier(this.minecraft, 0.0f);

        if (elapsed >= GetUpConfig.whiteFadeMs()) {
            // Install the native vanilla overlay before respawning. Vanilla is then free to
            // close the death screen normally; the overlay remains above the live world.
            this.phase = Phase.FINISHED;
            DeathAudioController.setGameAudioMultiplier(this.minecraft, 0.0f);
            this.minecraft.gui.setOverlay(new DeathTransitionOverlay());

            if (this.minecraft.player != null) {
                this.minecraft.player.respawn();
                GetUpTime.handleRespawn(this.minecraft);
            } else {
                this.minecraft.gui.setOverlay(null);
                DeathAudioController.restore(this.minecraft);
            }
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        // Do not call super: this presentation completely replaces the vanilla death screen.
        this.hoveredGetUp = isGetUpVisible() && isInsideGetUp(mouseX, mouseY);
        this.hoveredQuit = isQuitVisible() && isInsideQuit(mouseX, mouseY);

        if (this.phase == Phase.TRANSITION) {
            renderTransition(graphics);
            return;
        }

        graphics.fill(0, 0, this.width, this.height, 0xFF000000);

        if (isGetUpVisible()) {
            drawCenteredJittered(
                    graphics,
                    GET_UP,
                    this.width / 2,
                    this.height / 2 - (this.font.lineHeight / 2),
                    this.getUpActivated || this.hoveredGetUp ? WHITE : GRAY
            );
        } else if (elapsedDeathMs() >= GetUpConfig.youDiedMs()) {
            drawCenteredJittered(
                    graphics,
                    YOU_DIED,
                    this.width / 2,
                    this.height / 2 - (this.font.lineHeight / 2),
                    GRAY
            );
        }

        if (isQuitVisible()) {
            renderQuitMark(graphics, quitOpacity(), true);
        }
    }

    private void renderTransition(GuiGraphicsExtractor graphics) {
        // Keep the death screen's black background underneath the entire transition.
        // The activated Get Up text is intentionally drawn BEFORE the white layer.
        // This makes the transition wash it out naturally instead of leaving its
        // shadow visible above the white frame.
        graphics.fill(0, 0, this.width, this.height, 0xFF000000);

        if (this.getUpActivated) {
            drawCenteredJittered(
                    graphics,
                    GET_UP,
                    this.width / 2,
                    this.height / 2 - (this.font.lineHeight / 2),
                    WHITE
            );
            renderQuitMark(graphics, 1.0f, false);
        }

        long elapsed = transitionElapsedMs();
        float whiteAlpha = clamp01(elapsed / (float) GetUpConfig.whiteFadeMs());
        if (whiteAlpha > 0.0f) {
            graphics.fill(0, 0, this.width, this.height, argbFromAlpha(whiteAlpha, 0xFFFFFF));
        }
    }

    private void renderQuitMark(GuiGraphicsExtractor graphics, float opacity, boolean hoverAware) {
        int x = QUIT_MARGIN + QUIT_BOX_SIZE / 2;
        int y = QUIT_MARGIN + QUIT_BOX_SIZE / 2 - this.font.lineHeight / 2;
        int rgb = hoverAware && this.hoveredQuit ? WHITE : GRAY;
        graphics.centeredText(this.font, QUIT_MARK, x, y, argbFromAlpha(opacity, rgb));
    }

    private boolean isQuitVisible() {
        return elapsedDeathMs() >= GetUpConfig.getUpMs();
    }

    private float quitOpacity() {
        long sinceGetUp = elapsedDeathMs() - GetUpConfig.getUpMs();
        return clamp01(sinceGetUp / (float) QUIT_FADE_MS);
    }

    private void drawCenteredJittered(GuiGraphicsExtractor graphics, Component text, int centerX, int y, int color) {
        graphics.centeredText(this.font, text, centerX + this.jitterX, y + this.jitterY, color);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() != GLFW.GLFW_MOUSE_BUTTON_1) {
            return false;
        }

        int mouseX = (int) event.x();
        int mouseY = (int) event.y();

        if (this.phase == Phase.DEATH_SEQUENCE && isQuitVisible() && isInsideQuit(mouseX, mouseY)) {
            exitToTitle();
            return true;
        }

        if (this.phase == Phase.DEATH_SEQUENCE && isGetUpVisible() && isInsideGetUp(mouseX, mouseY)) {
            this.getUpActivated = true;
            GetUpMusicController.beginPitchTransition(GetUpConfig.whiteFadeMs());
            beginRespawnTransition();
            return true;
        }

        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int key = event.key();

        if (key == GLFW.GLFW_KEY_ESCAPE) {
            exitToTitle();
            return true;
        }

        if (this.phase == Phase.DEATH_SEQUENCE
                && isGetUpVisible()
                && (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER)) {
            this.getUpActivated = true;
            GetUpMusicController.beginPitchTransition(GetUpConfig.whiteFadeMs());
            beginRespawnTransition();
            return true;
        }

        return super.keyPressed(event);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        // The integrated server and client world must be allowed to continue, especially after
        // respawn, while the custom white transition remains above the world.
        return false;
    }

    @Override
    public void onClose() {
        if (this.phase != Phase.TRANSITION && this.phase != Phase.FINISHED) {
            exitToTitle();
        }
    }

    private void beginRespawnTransition() {
        if (this.phase != Phase.DEATH_SEQUENCE) {
            return;
        }

        // Stay inside the death screen for the configured black-to-white phase. Respawn only
        // once the screen is completely white; the native Overlay takes over at that exact point.
        this.phase = Phase.TRANSITION;
        this.transitionStartMs = System.currentTimeMillis();
        DeathAudioController.setGameAudioMultiplier(this.minecraft, 0.0f);
    }

    private void exitToTitle() {
        if (this.phase == Phase.FINISHED) {
            return;
        }

        GetUpMusicController.stop(this.minecraft);
        DeathAudioController.restore(this.minecraft);
        this.phase = Phase.FINISHED;
        this.minecraft.disconnect(new TitleScreen(), false, true);
    }

    private long elapsedDeathMs() {
        return Math.max(0L, System.currentTimeMillis() - this.deathStartMs);
    }

    private long transitionElapsedMs() {
        if (this.transitionStartMs < 0L) {
            return 0L;
        }
        return Math.max(0L, System.currentTimeMillis() - this.transitionStartMs);
    }

    private void updateJitter() {
        if (this.phase == Phase.DEATH_SEQUENCE) {
            this.jitterX = ThreadLocalRandom.current().nextInt(-JITTER_PIXELS, JITTER_PIXELS + 1);
            this.jitterY = ThreadLocalRandom.current().nextInt(-JITTER_PIXELS, JITTER_PIXELS + 1);
        } else {
            this.jitterX = 0;
            this.jitterY = 0;
        }
    }

    private boolean isInsideGetUp(int mouseX, int mouseY) {
        int textWidth = this.font.width(GET_UP);
        int boxWidth = textWidth + GET_UP_PAD_X * 2;
        int boxHeight = this.font.lineHeight + GET_UP_PAD_Y * 2;

        int left = (this.width - boxWidth) / 2;
        int top = (this.height - boxHeight) / 2;
        return mouseX >= left && mouseX <= left + boxWidth
                && mouseY >= top && mouseY <= top + boxHeight;
    }

    private boolean isInsideQuit(int mouseX, int mouseY) {
        return mouseX >= QUIT_MARGIN
                && mouseX <= QUIT_MARGIN + QUIT_BOX_SIZE
                && mouseY >= QUIT_MARGIN
                && mouseY <= QUIT_MARGIN + QUIT_BOX_SIZE;
    }

    private static int argbFromAlpha(float alpha, int rgb) {
        int a = Math.round(clamp01(alpha) * 255.0f);
        return (a << 24) | (rgb & 0x00FFFFFF);
    }

    private static float clamp01(float value) {
        return Math.clamp(value, 0.0f, 1.0f);
    }
}
