package com.nicolas.getup.client;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

/** Chat commands for changing the death-screen timings at runtime. */
public final class GetUpCommands {
    private GetUpCommands() {}

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
                ClientCommands.literal("getup")
                        .then(ClientCommands.literal("set")
                                .then(duration("you_died", value -> GetUpConfig.youDiedSeconds = value))
                                .then(duration("get_up", value -> GetUpConfig.getUpSeconds = value))
                                .then(duration("white", value -> GetUpConfig.whiteFadeSeconds = value))
                                .then(duration("exposure", value -> GetUpConfig.exposureFadeSeconds = value)))
                        .then(ClientCommands.literal("get")
                                .executes(context -> {
                                    context.getSource().sendFeedback(Component.literal(formatConfig()));
                                    return 1;
                                }))
                        .then(ClientCommands.literal("reset")
                                .executes(context -> {
                                    GetUpConfig.reset();
                                    GetUpConfig.save();
                                    context.getSource().sendFeedback(Component.literal("Get Up!: configuración restablecida."));
                                    return 1;
                                }))
        ));
    }

    private static RequiredArgumentBuilder<FabricClientCommandSource, Float> duration(
            String name,
            java.util.function.Consumer<Float> setter
    ) {
        return ClientCommands.argument("seconds", FloatArgumentType.floatArg(0.05f, 3600.0f))
                .executes(context -> {
                    float value = FloatArgumentType.getFloat(context, "seconds");
                    setter.accept(value);
                    GetUpConfig.save();
                    context.getSource().sendFeedback(Component.literal(
                            "Get Up!: " + name + " = " + formatSeconds(value) + " s"
                    ));
                    return 1;
                });
    }

    private static String formatConfig() {
        return "Get Up! | you_died=" + formatSeconds(GetUpConfig.youDiedSeconds)
                + "s, get_up=" + formatSeconds(GetUpConfig.getUpSeconds)
                + "s, white=" + formatSeconds(GetUpConfig.whiteFadeSeconds)
                + "s, exposure=" + formatSeconds(GetUpConfig.exposureFadeSeconds)
                + "s, next_day=" + GetUpConfig.advanceToNextDay;
    }

    private static String formatSeconds(float value) {
        if (value == Math.round(value)) {
            return Integer.toString(Math.round(value));
        }
        return String.format(java.util.Locale.ROOT, "%.2f", value);
    }
}
