package com.nicolas.getup.client;

import net.fabricmc.api.ClientModInitializer;

public final class GetUpClient implements ClientModInitializer {
    public static final String MOD_ID = "getup";

    @Override
    public void onInitializeClient() {
        GetUpConfig.load();
        ExposureEffect.initialize();
        GetUpCommands.register();
    }
}
