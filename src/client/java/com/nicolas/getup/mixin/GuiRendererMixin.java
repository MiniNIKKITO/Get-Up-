package com.nicolas.getup.mixin;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.nicolas.getup.client.DeathTransitionOverlay;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Applies an independent alpha fade to GUI/HUD vertices during the post-respawn recovery. */
@Mixin(GuiRenderer.class)
public abstract class GuiRendererMixin {
    @Redirect(
            method = "addElementToMesh",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/state/gui/GuiElementRenderState;buildVertices(Lcom/mojang/blaze3d/vertex/VertexConsumer;)V"
            )
    )
    private void getup$fadeHud(GuiElementRenderState elementState, VertexConsumer consumer) {
        float opacity = DeathTransitionOverlay.hudOpacity();
        if (opacity >= 0.9999f) {
            elementState.buildVertices(consumer);
            return;
        }

        elementState.buildVertices(new AlphaVertexConsumer(consumer, opacity));
    }

    private static final class AlphaVertexConsumer implements VertexConsumer {
        private final VertexConsumer delegate;
        private final float alphaMultiplier;

        private AlphaVertexConsumer(VertexConsumer delegate, float alphaMultiplier) {
            this.delegate = delegate;
            this.alphaMultiplier = alphaMultiplier;
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            delegate.addVertex(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer setColor(int color) {
            int alpha = (color >>> 24) & 0xFF;
            int fadedAlpha = Math.round(alpha * alphaMultiplier);
            delegate.setColor((color & 0x00FFFFFF) | (fadedAlpha << 24));
            return this;
        }

        @Override
        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
            delegate.setColor(red, green, blue, Math.round(alpha * alphaMultiplier));
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            delegate.setUv(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            delegate.setUv1(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            delegate.setUv2(u, v);
            return this;
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            delegate.setNormal(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer setLineWidth(float width) {
            delegate.setLineWidth(width);
            return this;
        }
    }
}
