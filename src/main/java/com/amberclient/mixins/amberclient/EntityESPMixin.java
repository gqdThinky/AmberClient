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
        EntityESP esp = EntityESP.Companion.getInstance(); // Access via Companion
        LivingEntity entity = EntityESP.Companion.getStateToEntity().get(renderState); // Access via Companion

        if (entity != null && entity == MinecraftClient.getInstance().player) { return; }

        if (esp == null || !esp.isEnabled() || renderState == null) return;

        boolean isPlayer = renderState instanceof PlayerEntityRenderState;

        if ((isPlayer && !esp.getRenderPlayersSetting().isEnabled()) ||
                (!isPlayer && !esp.getRenderMobsSetting().isEnabled())) {
            return;
        }

        @SuppressWarnings("unchecked")
        LivingEntityRenderer<T, S, M> renderer = (LivingEntityRenderer<T, S, M>) (Object) this;
        M model = renderer.getModel();

        // Render outline
        int outlineColor = isPlayer ? 0xFFFF8000 : 0xFF708090;

        matrices.push();
        model.render(matrices,
                vertexConsumers.getBuffer(RenderLayer.getOutline(renderer.getTexture(renderState))),
                light,
                OverlayTexture.DEFAULT_UV,
                outlineColor
        );
        matrices.pop();

        // Render name/health info
        if (esp.getEntityInfosSetting().isEnabled()) {
            renderNametag(renderState, matrices, vertexConsumers, light, esp);
        }
    }

    @Unique
    private void renderNametag(S renderState, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, EntityESP esp) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        String name = "";
        String health = "";

        LivingEntity entity = EntityESP.Companion.getStateToEntity().get(renderState); // Access via Companion
        if (entity != null) {
            if (esp.getEntityInfosSetting().isEnabled()) {
                if (entity.hasCustomName()) {
                    name = entity.getCustomName().getString();
                } else if (renderState instanceof PlayerEntityRenderState playerState) {
                    name = playerState.name;
                } else {
                    name = entity.getType().getName().getString();
                }

                int healthValue = (int) entity.getHealth();
                int maxHealth = (int) entity.getMaxHealth();
                health = "HP: " + healthValue + "/" + maxHealth;
            }
        } else {
            if (esp.getEntityInfosSetting().isEnabled()) {
                if (renderState instanceof PlayerEntityRenderState playerState) {
                    name = playerState.name;
                } else {
                    name = "Mob";
                }
                health = "HP: Unknown";
            }
        }

        if (name.isEmpty() && health.isEmpty()) return;

        TextRenderer textRenderer = client.textRenderer;

        matrices.push();

        float heightOffset = renderState instanceof PlayerEntityRenderState ? -1.2F : -1.0f;
        matrices.translate(0.0, heightOffset, 0.0);
        matrices.multiply(client.getEntityRenderDispatcher().camera.getRotation());
        matrices.scale(-0.025f, 0.025f, 0.025f);

        Matrix4f matrix = matrices.peek().getPositionMatrix();

        int nameWidth = name.isEmpty() ? 0 : textRenderer.getWidth(name);
        int healthWidth = health.isEmpty() ? 0 : textRenderer.getWidth(health);
        int maxWidth = Math.max(nameWidth, healthWidth);

        int padding = 2;
        int lineHeight = 10;
        int totalHeight = 0;

        if (!name.isEmpty()) totalHeight += lineHeight;
        if (!health.isEmpty()) totalHeight += lineHeight;

        VertexConsumer backgroundBuffer = vertexConsumers.getBuffer(RenderLayer.getGui());

        int bgLeft = -maxWidth / 2 - padding;
        int bgRight = maxWidth / 2 + padding;
        int bgTop = -padding;
        int bgBottom = totalHeight + padding;

        drawBothSides(backgroundBuffer, matrix, bgLeft, bgTop, bgRight, bgBottom, 0xC0000000);
        drawTextBothSides(textRenderer, name, health, nameWidth, healthWidth, lineHeight, matrix, vertexConsumers, light);

        matrices.pop();
    }

    @Unique
    private void drawTextBothSides(TextRenderer textRenderer, String name, String health, int nameWidth, int healthWidth, int lineHeight, Matrix4f matrix, VertexConsumerProvider vertexConsumers, int light) {
        int currentY = 0;
        float nameX = -nameWidth / 2.0f;
        float healthX = -healthWidth / 2.0f;

        if (!name.isEmpty()) {
            textRenderer.draw(name, nameX, currentY, 0xFFFFFFFF, false, matrix, vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, 0, light);
            currentY += lineHeight;
        }

        if (!health.isEmpty()) {
            textRenderer.draw(health, healthX, currentY, 0xFFFFFFFF, false, matrix, vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, 0, light);
        }

        Matrix4f backMatrix = new Matrix4f(matrix);
        backMatrix.scale(-1.0f, 1.0f, 1.0f);
        currentY = 0;

        if (!name.isEmpty()) {
            textRenderer.draw(name, nameX, currentY, 0xFFFFFFFF, false, backMatrix, vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, 0, light);
            currentY += lineHeight;
        }

        if (!health.isEmpty()) {
            textRenderer.draw(health, healthX, currentY, 0xFFFFFFFF, false, backMatrix, vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, 0, light);
        }
    }

    @Unique
    private void drawBothSides(VertexConsumer buffer, Matrix4f matrix, int left, int top, int right, int bottom, int color) {
        float a = (color >> 24 & 0xFF) / 255.0f;
        float r = (color >> 16 & 0xFF) / 255.0f;
        float g = (color >> 8 & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;

        // Front face
        buffer.vertex(matrix, left, bottom, 0).color(r, g, b, a);
        buffer.vertex(matrix, right, bottom, 0).color(r, g, b, a);
        buffer.vertex(matrix, right, top, 0).color(r, g, b, a);
        buffer.vertex(matrix, left, top, 0).color(r, g, b, a);

        // Back face
        buffer.vertex(matrix, left, top, 0).color(r, g, b, a);
        buffer.vertex(matrix, right, top, 0).color(r, g, b, a);
        buffer.vertex(matrix, right, bottom, 0).color(r, g, b, a);
        buffer.vertex(matrix, left, bottom, 0).color(r, g, b, a);
    }

    @Inject(method = "updateRenderState*", at = @At("TAIL"))
    private void onUpdateRenderState(T entity, S state, float tickDelta, CallbackInfo ci) {
        EntityESP.Companion.getStateToEntity().put(state, entity); // Access via Companion
    }
}