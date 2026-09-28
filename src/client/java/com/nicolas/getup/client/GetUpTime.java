package com.nicolas.getup.client;

import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.clock.WorldClocks;

/** Handles the world-state changes that happen immediately after a singleplayer respawn. */
public final class GetUpTime {
    private static final long DAY_LENGTH = 24_000L;

    private GetUpTime() {}

    public static void handleRespawn(Minecraft minecraft) {
        MinecraftServer server = minecraft.getSingleplayerServer();
        if (server == null) {
            return;
        }

        server.execute(() -> {
            // Remove every currently loaded hostile mob in every loaded dimension.
            // Monster is Minecraft's hostile-mob marker, so peaceful/passive mobs are untouched.
            for (ServerLevel level : server.getAllLevels()) {
                for (Entity entity : level.getAllEntities()) {
                    if (entity instanceof Monster) {
                        entity.discard();
                    }
                }
            }

            if (!GetUpConfig.advanceToNextDay) {
                return;
            }

            ServerLevel level = minecraft.level == null
                    ? null
                    : server.getLevel(minecraft.level.dimension());
            if (level == null) {
                return;
            }

            // 26.2 uses WorldClock rather than the old Level getDayTime/setDayTime API.
            // Vanilla's overworld clock is the clock that defines the normal day/night cycle.
            var clock = level.registryAccess().getOrThrow(WorldClocks.OVERWORLD);
            var clockManager = level.clockManager();
            long current = clockManager.getTotalTicks(clock);
            long remainder = Math.floorMod(current, DAY_LENGTH);
            long delta = DAY_LENGTH - remainder;
            if (delta == 0L) {
                delta = DAY_LENGTH;
            }
            clockManager.setTotalTicks(clock, current + delta);
        });
    }
}
