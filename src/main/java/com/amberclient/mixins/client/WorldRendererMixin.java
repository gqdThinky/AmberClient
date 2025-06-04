package com.amberclient.mixins.client;

import com.amberclient.modules.combat.Hitbox;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.joml.Matrix4f;

import java.util.Random;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    @Unique
    private static final Random RANDOM = new Random();

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
        MinecraftClient client = MinecraftClient.getInstance();
        Box originalBox = entity.getBoundingBox();

        // Calculate distance to entity
        double distance = client.player.getPos().distanceTo(entity.getPos());
        if (distance > 4.0) return originalBox; // Avoid expansion at long distances

        // Calculate angle between player's look direction and entity
        Vec3d playerPos = client.player.getEyePos();
        Vec3d entityPos = entity.getPos().add(0, entity.getHeight() / 2, 0);
        Vec3d lookVec = client.player.getRotationVector();
        Vec3d toEntity = entityPos.subtract(playerPos).normalize();
        double dot = lookVec.dotProduct(toEntity);
        double angle = Math.acos(dot) * (180.0 / Math.PI);

        // Base expansion values
        double baseExpandX = Hitbox.getInstance().getExpandX();
        double baseExpandYUp = Hitbox.getInstance().getExpandYUp();
        double baseExpandZ = Hitbox.getInstance().getExpandZ();

        // Dynamic adjustment based on angle
        double angleFactor = Math.min(1.0, angle / 45.0);
        double expandX = baseExpandX * (0.5 + angleFactor * 0.5);
        double expandYUp = baseExpandYUp * (0.5 + angleFactor * 0.5);
        double expandZ = baseExpandZ * (0.5 + angleFactor * 0.5);

        // Randomization to avoid consistent patterns
        double randomX = (RANDOM.nextDouble() - 0.5) * 0.1;
        double randomY = (RANDOM.nextDouble() - 0.5) * 0.1;
        double randomZ = (RANDOM.nextDouble() - 0.5) * 0.1;
        expandX += randomX;
        expandYUp += randomY;
        expandZ += randomZ;

        // Ensure values stay within reasonable bounds
        expandX = MathHelper.clamp(expandX, 0.0, 2.0);
        expandYUp = MathHelper.clamp(expandYUp, 0.0, 2.0);
        expandZ = MathHelper.clamp(expandZ, 0.0, 2.0);

        return new Box(
                originalBox.minX - expandX,
                originalBox.minY,
                originalBox.minZ - expandZ,
                originalBox.maxX + expandX,
                originalBox.maxY + expandYUp,
                originalBox.maxZ + expandZ
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