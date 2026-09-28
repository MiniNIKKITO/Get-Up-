package com.nicolas.getup.client;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Properties;

/** Persistent client configuration for the Get Up!. */
public final class GetUpConfig {
    public static boolean enabled = true;

    private static final float DEFAULT_YOU_DIED_SECONDS = 2.0f;
    private static final float DEFAULT_GET_UP_SECONDS = 5.0f;
    private static final float DEFAULT_WHITE_FADE_SECONDS = 2.0f;
    private static final float DEFAULT_EXPOSURE_FADE_SECONDS = 2.0f;

    private static final float MIN_SECONDS = 0.05f;
    private static final float MAX_SECONDS = 3600.0f;

    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("getup.properties");

    public static float youDiedSeconds = DEFAULT_YOU_DIED_SECONDS;
    public static float getUpSeconds = DEFAULT_GET_UP_SECONDS;
    public static float whiteFadeSeconds = DEFAULT_WHITE_FADE_SECONDS;
    public static float exposureFadeSeconds = DEFAULT_EXPOSURE_FADE_SECONDS;
    public static boolean advanceToNextDay = true;

    private GetUpConfig() {}

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            save();
            return;
        }

        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            properties.load(reader);
            enabled = Boolean.parseBoolean(properties.getProperty("enabled", "true"));
            youDiedSeconds = readFloat(properties, "you_died", DEFAULT_YOU_DIED_SECONDS);
            getUpSeconds = readFloat(properties, "get_up", DEFAULT_GET_UP_SECONDS);
            whiteFadeSeconds = readFloat(properties, "white", DEFAULT_WHITE_FADE_SECONDS);
            exposureFadeSeconds = readFloat(properties, "exposure", DEFAULT_EXPOSURE_FADE_SECONDS);
            advanceToNextDay = Boolean.parseBoolean(properties.getProperty("next_day", "true"));
        } catch (IOException ignored) {
            reset();
        }
    }

    public static void save() {
        Properties properties = new Properties();
        properties.setProperty("enabled", Boolean.toString(enabled));
        properties.setProperty("you_died", Float.toString(clamp(youDiedSeconds)));
        properties.setProperty("get_up", Float.toString(clamp(getUpSeconds)));
        properties.setProperty("white", Float.toString(clamp(whiteFadeSeconds)));
        properties.setProperty("exposure", Float.toString(clamp(exposureFadeSeconds)));
        properties.setProperty("next_day", Boolean.toString(advanceToNextDay));

        youDiedSeconds = clamp(youDiedSeconds);
        getUpSeconds = clamp(getUpSeconds);
        whiteFadeSeconds = clamp(whiteFadeSeconds);
        exposureFadeSeconds = clamp(exposureFadeSeconds);

        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                properties.store(writer, "Get Up! configuration");
            }
        } catch (IOException ignored) {
            // Runtime values still apply even if the file cannot be written.
        }
    }

    public static void reset() {
        enabled = true;
        youDiedSeconds = DEFAULT_YOU_DIED_SECONDS;
        getUpSeconds = DEFAULT_GET_UP_SECONDS;
        whiteFadeSeconds = DEFAULT_WHITE_FADE_SECONDS;
        exposureFadeSeconds = DEFAULT_EXPOSURE_FADE_SECONDS;
        advanceToNextDay = true;
    }

    public static long youDiedMs() { return secondsToMs(youDiedSeconds); }
    public static long getUpMs() { return secondsToMs(getUpSeconds); }
    public static long whiteFadeMs() { return secondsToMs(whiteFadeSeconds); }
    public static long exposureFadeMs() { return secondsToMs(exposureFadeSeconds); }
    private static float readFloat(Properties properties, String key, float fallback) {
        try {
            return clamp(Float.parseFloat(properties.getProperty(key, Float.toString(fallback))));
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static long secondsToMs(float seconds) {
        return Math.max(1L, Math.round(clamp(seconds) * 1000.0f));
    }

    private static float clamp(float seconds) {
        if (!Float.isFinite(seconds)) {
            return MIN_SECONDS;
        }
        return Math.clamp(seconds, MIN_SECONDS, MAX_SECONDS);
    }
}
