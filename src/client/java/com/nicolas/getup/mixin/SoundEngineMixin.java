package com.nicolas.getup.mixin;

import com.nicolas.getup.client.DeathAudioController;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.resources.sounds.SoundInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Applies the death-screen's runtime audio multiplier to every sound calculated by Minecraft's
 * client sound engine, without touching the player's persistent volume options.
 */
@Mixin(SoundEngine.class)
public abstract class SoundEngineMixin {
    @Inject(
            method = "calculateVolume(Lnet/minecraft/client/resources/sounds/SoundInstance;)F",
            at = @At("RETURN"),
            cancellable = true
    )
    private void getup$applyDeathAudioMultiplier(
            SoundInstance sound,
            CallbackInfoReturnable<Float> callbackInfo
    ) {
        callbackInfo.setReturnValue(DeathAudioController.applyMultiplier(sound, callbackInfo.getReturnValueF()));
    }
}
