package com.amberclient.mixin;

import com.amberclient.modules.Hitbox;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.joml.Matrix4f;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(ObjectAllocator allocator, RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, Matrix4f positionMatrix, Matrix4f projectionMatrix, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (!client.getEntityRenderDispatcher().shouldRenderHitboxes() || !Hitbox.isHitboxModuleEnabled) {
            return;
        }

        MatrixStack matrices = new MatrixStack();
        matrices.multiplyPositionMatrix(positionMatrix);

        VertexConsumerProvider.Immediate immediate = client.getBufferBuilders().getEntityVertexConsumers();
        VertexConsumer vertexConsumer = immediate.getBuffer(RenderLayer.getLines());

        assert client.world != null;
        for (Entity entity : client.world.getEntities()) {
            if (entity == client.player) continue;

            Box expandedBox = getBox(entity);

            matrices.push();
            matrices.translate(
                    entity.getX() - camera.getPos().x,
                    entity.getY() - camera.getPos().y,
                    entity.getZ() - camera.getPos().z
            );

            drawExpandedHitbox(matrices, vertexConsumer,
                    expandedBox.offset(-entity.getX(), -entity.getY(), -entity.getZ()));

            matrices.pop();
        }

        immediate.draw();
    }

    @Unique
    private static @NotNull Box getBox(Entity entity) {
        Box originalBox = entity.getBoundingBox();
        double expandX = Hitbox.getInstance().getExpandX();
        double expandYUp = Hitbox.getInstance().getExpandYUp();
        double expandZ = 0.0; // No front/back expansion
        return new Box(
                originalBox.minX - expandX,
                originalBox.minY,
                originalBox.minZ,
                originalBox.maxX + expandX,
                originalBox.maxY + expandYUp,
                originalBox.maxZ
        );
    }

    @Unique
    private void drawExpandedHitbox(MatrixStack matrices, VertexConsumer vertexConsumer, Box box) {
        MatrixStack.Entry entry = matrices.peek();
        float red = 1.0f, green = 0.5f, blue = 0.0f, alpha = 0.7f;

        float minX = (float) box.minX, minY = (float) box.minY, minZ = (float) box.minZ;
        float maxX = (float) box.maxX, maxY = (float) box.maxY, maxZ = (float) box.maxZ;

        // 12 edges with normals
        vertexConsumer.vertex(entry.getPositionMatrix(), minX, minY, minZ).color(red, green, blue, alpha).normal(0, 1, 0);
        vertexConsumer.vertex(entry.getPositionMatrix(), maxX, minY, minZ).color(red, green, blue, alpha).normal(0, 1, 0);
        vertexConsumer.vertex(entry.getPositionMatrix(), maxX, minY, minZ).color(red, green, blue, alpha).normal(0, 1, 0);
        vertexConsumer.vertex(entry.getPositionMatrix(), maxX, minY, maxZ).color(red, green, blue, alpha).normal(0, 1, 0);
        vertexConsumer.vertex(entry.getPositionMatrix(), maxX, minY, maxZ).color(red, green, blue, alpha).normal(0, 1, 0);
        vertexConsumer.vertex(entry.getPositionMatrix(), minX, minY, maxZ).color(red, green, blue, alpha).normal(0, 1, 0);
        vertexConsumer.vertex(entry.getPositionMatrix(), minX, minY, maxZ).color(red, green, blue, alpha).normal(0, 1, 0);
        vertexConsumer.vertex(entry.getPositionMatrix(), minX, minY, minZ).color(red, green, blue, alpha).normal(0, 1, 0);

        vertexConsumer.vertex(entry.getPositionMatrix(), minX, maxY, minZ).color(red, green, blue, alpha).normal(0, 1, 0);
        vertexConsumer.vertex(entry.getPositionMatrix(), maxX, maxY, minZ).color(red, green, blue, alpha).normal(0, 1, 0);
        vertexConsumer.vertex(entry.getPositionMatrix(), maxX, maxY, minZ).color(red, green, blue, alpha).normal(0, 1, 0);
        vertexConsumer.vertex(entry.getPositionMatrix(), maxX, maxY, maxZ).color(red, green, blue, alpha).normal(0, 1, 0);
        vertexConsumer.vertex(entry.getPositionMatrix(), maxX, maxY, maxZ).color(red, green, blue, alpha).normal(0, 1, 0);
        vertexConsumer.vertex(entry.getPositionMatrix(), minX, maxY, maxZ).color(red, green, blue, alpha).normal(0, 1, 0);
        vertexConsumer.vertex(entry.getPositionMatrix(), minX, maxY, maxZ).color(red, green, blue, alpha).normal(0, 1, 0);
        vertexConsumer.vertex(entry.getPositionMatrix(), minX, maxY, minZ).color(red, green, blue, alpha).normal(0, 1, 0);

        vertexConsumer.vertex(entry.getPositionMatrix(), minX, minY, minZ).color(red, green, blue, alpha).normal(0, 1, 0);
        vertexConsumer.vertex(entry.getPositionMatrix(), minX, maxY, minZ).color(red, green, blue, alpha).normal(0, 1, 0);
        vertexConsumer.vertex(entry.getPositionMatrix(), maxX, minY, minZ).color(red, green, blue, alpha).normal(0, 1, 0);
        vertexConsumer.vertex(entry.getPositionMatrix(), maxX, maxY, minZ).color(red, green, blue, alpha).normal(0, 1, 0);
        vertexConsumer.vertex(entry.getPositionMatrix(), maxX, minY, maxZ).color(red, green, blue, alpha).normal(0, 1, 0);
        vertexConsumer.vertex(entry.getPositionMatrix(), maxX, maxY, maxZ).color(red, green, blue, alpha).normal(0, 1, 0);
        vertexConsumer.vertex(entry.getPositionMatrix(), minX, minY, maxZ).color(red, green, blue, alpha).normal(0, 1, 0);
        vertexConsumer.vertex(entry.getPositionMatrix(), minX, maxY, maxZ).color(red, green, blue, alpha).normal(0, 1, 0);
    }
}