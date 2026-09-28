package com.nicolas.getup.mixin;

import com.nicolas.getup.client.GetUpScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.DeathScreen;
import com.nicolas.getup.client.GetUpConfig;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {
    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void getup$replaceDeathScreen(@Nullable Screen screen, CallbackInfo callbackInfo) {
        Minecraft minecraft = Minecraft.getInstance();

        if (GetUpConfig.enabled
                && screen instanceof DeathScreen
                && minecraft.hasSingleplayerServer()
                && minecraft.player != null) {
            minecraft.gui.setScreen(new GetUpScreen(minecraft.player));
            callbackInfo.cancel();
        }
    }
}
