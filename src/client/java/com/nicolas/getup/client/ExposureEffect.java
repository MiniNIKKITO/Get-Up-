package com.nicolas.getup.client;

import net.minecraft.resources.Identifier;
import org.ladysnake.satin.api.managed.ManagedShaderEffect;
import org.ladysnake.satin.api.managed.ShaderEffectManager;
import org.ladysnake.satin.api.managed.uniform.Uniform1f;

/** Full-screen exposure effect used during the post-respawn recovery. */
public final class ExposureEffect {
    private static final ManagedShaderEffect EFFECT = ShaderEffectManager.getInstance()
            .manage(Identifier.fromNamespaceAndPath("getup", "exposure"));

    private static Uniform1f exposureUniform;
    private static boolean active;

    private ExposureEffect() {}

    public static void initialize() {
        exposureUniform = EFFECT.findUniform1f("Exposure");
    }

    public static void start() {
        active = true;
        setExposure(32.0f);
    }

    public static boolean isActive() {
        return active;
    }

    public static void setExposure(float value) {
        if (exposureUniform != null) {
            exposureUniform.set(value);
        }
    }

    /** Applies the effect to the current main framebuffer. */
    public static void render() {
        if (active) {
            EFFECT.render(0.0f);
        }
    }

    public static void stop() {
        active = false;
        setExposure(0.0f);
    }
}
