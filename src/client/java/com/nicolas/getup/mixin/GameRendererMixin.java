package com.nicolas.getup.mixin;

import com.nicolas.getup.client.ExposureEffect;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Applies the death recovery exposure post-process at the end of frame rendering. */
@Mixin(GameRenderer.class)
public final class GameRendererMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private void getup$renderExposure(CallbackInfo callbackInfo) {
        ExposureEffect.render();
    }
}
