package com.amberclient.mixins.amberclient;

import com.amberclient.modules.render.EntityESP;
import com.amberclient.utils.module.ModuleManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class EntityESPMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void onRenderWorld(ObjectAllocator allocator, RenderTickCounter tickCounter, boolean renderBlockOutline,
                               Camera camera, GameRenderer gameRenderer, Matrix4f positionMatrix,
                               Matrix4f projectionMatrix, CallbackInfo ci) {
        EntityESP module = (EntityESP) ModuleManager.getInstance().getModule(EntityESP.class);
        if (module == null || !module.isEnabled()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return;

        // Configuration OpenGL pour le rendu ESP
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderSystem.lineWidth(module.getLineWidth());
        RenderSystem.disableDepthTest();

        // Obtenir le VertexConsumer pour les lignes
        VertexConsumerProvider.Immediate vertexConsumers = client.getBufferBuilders().getEntityVertexConsumers();
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getLines());

        // Créer une MatrixStack pour gérer les transformations
        MatrixStack matrices = new MatrixStack();
        matrices.multiplyPositionMatrix(positionMatrix);

        // Parcourir les entités
        for (Entity entity : client.world.getEntities()) {
            if ((module.getRenderPlayersSetting().getBooleanValue() && entity instanceof PlayerEntity && entity != client.player) ||
                    (module.getRenderMobsSetting().getBooleanValue() && entity instanceof MobEntity)) {
                // Récupérer la boîte englobante de l’entité
                Box box = entity.getBoundingBox();

                // Définir la couleur selon le type d’entité
                float[] color = entity instanceof PlayerEntity ? module.getPlayerColor() : module.getMobColor();

                // Pousser une nouvelle transformation sur la pile
                matrices.push();
                // Appliquer une translation pour centrer le rendu sur l’entité par rapport à la caméra
                matrices.translate(
                        entity.getX() - camera.getPos().x,
                        entity.getY() - camera.getPos().y,
                        entity.getZ() - camera.getPos().z
                );

                // Dessiner les contours avec les coordonnées ajustées
                drawBox(matrices, vertexConsumer, box.offset(-entity.getX(), -entity.getY(), -entity.getZ()),
                        color[0], color[1], color[2], color[3]);

                // Restaurer l’état précédent de la pile
                matrices.pop();
            }
        }

        // Rendre les lignes
        vertexConsumers.draw();

        // Restaurer l’état OpenGL
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    // Méthode pour dessiner la boîte en utilisant MatrixStack
    private void drawBox(MatrixStack matrices, VertexConsumer vertexConsumer, Box box,
                         float red, float green, float blue, float alpha) {
        MatrixStack.Entry entry = matrices.peek();

        float minX = (float) box.minX;
        float minY = (float) box.minY;
        float minZ = (float) box.minZ;
        float maxX = (float) box.maxX;
        float maxY = (float) box.maxY;
        float maxZ = (float) box.maxZ;

        // Vecteur normal par défaut pour les lignes
        float nx = 0.0f;
        float ny = 0.0f;
        float nz = 1.0f;

        // Dessiner les 12 arêtes de la boîte en utilisant la matrice de transformation
        // Face inférieure
        vertexConsumer.vertex(entry.getPositionMatrix(), minX, minY, minZ).color(red, green, blue, alpha).normal(nx, ny, nz);
        vertexConsumer.vertex(entry.getPositionMatrix(), maxX, minY, minZ).color(red, green, blue, alpha).normal(nx, ny, nz);

        vertexConsumer.vertex(entry.getPositionMatrix(), maxX, minY, minZ).color(red, green, blue, alpha).normal(nx, ny, nz);
        vertexConsumer.vertex(entry.getPositionMatrix(), maxX, minY, maxZ).color(red, green, blue, alpha).normal(nx, ny, nz);

        vertexConsumer.vertex(entry.getPositionMatrix(), maxX, minY, maxZ).color(red, green, blue, alpha).normal(nx, ny, nz);
        vertexConsumer.vertex(entry.getPositionMatrix(), minX, minY, maxZ).color(red, green, blue, alpha).normal(nx, ny, nz);

        vertexConsumer.vertex(entry.getPositionMatrix(), minX, minY, maxZ).color(red, green, blue, alpha).normal(nx, ny, nz);
        vertexConsumer.vertex(entry.getPositionMatrix(), minX, minY, minZ).color(red, green, blue, alpha).normal(nx, ny, nz);

        // Face supérieure
        vertexConsumer.vertex(entry.getPositionMatrix(), minX, maxY, minZ).color(red, green, blue, alpha).normal(nx, ny, nz);
        vertexConsumer.vertex(entry.getPositionMatrix(), maxX, maxY, minZ).color(red, green, blue, alpha).normal(nx, ny, nz);

        vertexConsumer.vertex(entry.getPositionMatrix(), maxX, maxY, minZ).color(red, green, blue, alpha).normal(nx, ny, nz);
        vertexConsumer.vertex(entry.getPositionMatrix(), maxX, maxY, maxZ).color(red, green, blue, alpha).normal(nx, ny, nz);

        vertexConsumer.vertex(entry.getPositionMatrix(), maxX, maxY, maxZ).color(red, green, blue, alpha).normal(nx, ny, nz);
        vertexConsumer.vertex(entry.getPositionMatrix(), minX, maxY, maxZ).color(red, green, blue, alpha).normal(nx, ny, nz);

        vertexConsumer.vertex(entry.getPositionMatrix(), minX, maxY, maxZ).color(red, green, blue, alpha).normal(nx, ny, nz);
        vertexConsumer.vertex(entry.getPositionMatrix(), minX, maxY, minZ).color(red, green, blue, alpha).normal(nx, ny, nz);

        // Arêtes verticales
        vertexConsumer.vertex(entry.getPositionMatrix(), minX, minY, minZ).color(red, green, blue, alpha).normal(nx, ny, nz);
        vertexConsumer.vertex(entry.getPositionMatrix(), minX, maxY, minZ).color(red, green, blue, alpha).normal(nx, ny, nz);

        vertexConsumer.vertex(entry.getPositionMatrix(), maxX, minY, minZ).color(red, green, blue, alpha).normal(nx, ny, nz);
        vertexConsumer.vertex(entry.getPositionMatrix(), maxX, maxY, minZ).color(red, green, blue, alpha).normal(nx, ny, nz);

        vertexConsumer.vertex(entry.getPositionMatrix(), maxX, minY, maxZ).color(red, green, blue, alpha).normal(nx, ny, nz);
        vertexConsumer.vertex(entry.getPositionMatrix(), maxX, maxY, maxZ).color(red, green, blue, alpha).normal(nx, ny, nz);

        vertexConsumer.vertex(entry.getPositionMatrix(), minX, minY, maxZ).color(red, green, blue, alpha).normal(nx, ny, nz);
        vertexConsumer.vertex(entry.getPositionMatrix(), minX, maxY, maxZ).color(red, green, blue, alpha).normal(nx, ny, nz);
    }
}