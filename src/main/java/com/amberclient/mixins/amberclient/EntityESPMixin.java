package com.amberclient.mixins.amberclient;

import com.amberclient.modules.render.EntityESP;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class EntityESPMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends net.minecraft.client.render.entity.model.EntityModel<? super S>> {

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;pop()V", shift = At.Shift.BEFORE))
    private void onRenderBeforePop(S renderState, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        EntityESP esp = EntityESP.getInstance();

        if (!esp.isEnabled() || renderState == null) return;

        boolean isPlayer = renderState instanceof PlayerEntityRenderState;

        if ((isPlayer && !esp.getRenderPlayersSetting().isEnabled()) ||
                (!isPlayer && !esp.getRenderMobsSetting().isEnabled())) {
            return;
        }

        @SuppressWarnings("unchecked")
        LivingEntityRenderer<T, S, M> renderer = (LivingEntityRenderer<T, S, M>) (Object) this;
        M model = renderer.getModel();

        // Render outline
        int outlineColor;
        if (isPlayer) {
            outlineColor = 0xFFFF8000; // Orange for players
        } else {
            outlineColor = 0xFF708090; // Gray for mobs
        }

        matrices.push();
        model.render(matrices,
                vertexConsumers.getBuffer(RenderLayer.getOutline(renderer.getTexture(renderState))),
                light,
                OverlayTexture.DEFAULT_UV,
                outlineColor
        );
        matrices.pop();

        // Render nametag if enabled
        if (esp.getShowNametagsSetting().isEnabled() || esp.getShowHealthSetting().isEnabled()) {
            renderNametag(renderState, matrices, vertexConsumers, light, esp);
        }
    }

    private void renderNametag(S renderState, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, EntityESP esp) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        String name = "";
        String health = "";

        // TODO: Get entity information
        if (renderState instanceof PlayerEntityRenderState playerState) {
            if (esp.getShowNametagsSetting().isEnabled()) {
                name = playerState.name;
            }
            if (esp.getShowHealthSetting().isEnabled()) {
                health = "HP: 20/20";
            }
        } else {
            if (esp.getShowNametagsSetting().isEnabled()) {
                name = "Mob";
            }
            if (esp.getShowHealthSetting().isEnabled()) {
                health = "HP: Unknown";
            }
        }

        if (name.isEmpty() && health.isEmpty()) return;

        TextRenderer textRenderer = client.textRenderer;

        matrices.push();

        float heightOffset = renderState instanceof PlayerEntityRenderState ? -1.2F : -0.6f; // TODO: above the entity, probably not the best way to do so
        matrices.translate(0.0, heightOffset, 0.0);

        matrices.multiply(client.getEntityRenderDispatcher().getRotation());
        matrices.scale(-0.025f, 0.025f, 0.025f);

        Matrix4f matrix = matrices.peek().getPositionMatrix();

        // Calculate dimensions for each line
        int nameWidth = name.isEmpty() ? 0 : textRenderer.getWidth(name);
        int healthWidth = health.isEmpty() ? 0 : textRenderer.getWidth(health);
        int maxWidth = Math.max(nameWidth, healthWidth);

        int padding = 2;
        int lineHeight = 10;
        int totalHeight = 0;

        if (!name.isEmpty()) totalHeight += lineHeight;
        if (!health.isEmpty()) totalHeight += lineHeight;

        VertexConsumer backgroundBuffer = vertexConsumers.getBuffer(RenderLayer.getGuiOverlay());

        int bgLeft = -maxWidth / 2 - padding;
        int bgRight = maxWidth / 2 + padding;
        int bgTop = -padding;
        int bgBottom = totalHeight + padding;

        drawQuad(backgroundBuffer, matrix, bgLeft, bgTop, bgRight, bgBottom, 0xC0000000);

        int currentY = 0;

        if (!name.isEmpty()) {
            float nameX = -nameWidth / 2.0f;
            textRenderer.draw(name, nameX, currentY, 0xFFFFFFFF, false, matrix, vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, light);
            currentY += lineHeight;
        }

        if (!health.isEmpty()) {
            float healthX = -healthWidth / 2.0f;
            textRenderer.draw(health, healthX, currentY, 0xFFFFFFFF, false, matrix, vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, light);
        }

        matrices.pop();
    }

    @Unique
    private void drawQuad(VertexConsumer buffer, Matrix4f matrix, int left, int top, int right, int bottom, int color) {
        float a = (color >> 24 & 0xFF) / 255.0f;
        float r = (color >> 16 & 0xFF) / 255.0f;
        float g = (color >> 8 & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;

        buffer.vertex(matrix, left, bottom, 0).color(r, g, b, a);
        buffer.vertex(matrix, right, bottom, 0).color(r, g, b, a);
        buffer.vertex(matrix, right, top, 0).color(r, g, b, a);
        buffer.vertex(matrix, left, top, 0).color(r, g, b, a);
    }
}