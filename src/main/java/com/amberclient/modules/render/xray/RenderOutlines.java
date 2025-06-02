package com.amberclient.modules.render.xray;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.gl.GlUsage;
import net.minecraft.client.render.*;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.util.math.Vec3d;
import org.apache.logging.log4j.LogManager;
import org.joml.Matrix4f;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.logging.log4j.Logger;

public class RenderOutlines {
    public static AtomicBoolean requestedRefresh = new AtomicBoolean(false);
    private static VertexBuffer vertexBuffer;
    private static int vertexCount;
    private static final Logger LOGGER = LogManager.getLogger("amberclient-xray");

    public static void render(WorldRenderContext context) {
        if (ScanTask.renderQueue.isEmpty() || !SettingsStore.getInstance().get().isActive()) {
            return;
        }

        renderFallback(context);
    }

    private static void renderWithVertexConsumer(WorldRenderContext context, VertexConsumerProvider.Immediate immediate) {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.lineWidth(4.0f);

        VertexConsumer vertexConsumer = immediate.getBuffer(RenderLayer.getTranslucent());

        Vec3d cameraPos = context.camera().getPos();
        MatrixStack matrixStack = context.matrixStack();

        matrixStack.push();
        matrixStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        for (BlockPosWithColor blockProps : ScanTask.renderQueue) {
            if (blockProps == null) continue;

            final float size = 1.0f;
            final float x = blockProps.pos().getX();
            final float y = blockProps.pos().getY();
            final float z = blockProps.pos().getZ();
            final float red = blockProps.color().red() / 255f;
            final float green = blockProps.color().green() / 255f;
            final float blue = blockProps.color().blue() / 255f;
            final float alpha = 1.0f;

            renderLineBoxWithConsumer(vertexConsumer, matrixStack,
                    x, y, z, x + size, y + size, z + size,
                    red, green, blue, alpha);
        }

        matrixStack.pop();

        immediate.draw();

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.lineWidth(1.0f);
    }

    private static void renderFallback(WorldRenderContext context) {
        if (!SettingsStore.getInstance().get().isActive()) return;

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.lineWidth(4.0f);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

        Vec3d cameraPos = context.camera().getPos();
        MatrixStack matrixStack = context.matrixStack();

        matrixStack.push();
        matrixStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        for (BlockPosWithColor blockProps : ScanTask.renderQueue) {
            if (blockProps == null) continue;

            final float size = 1.0f;
            final float x = blockProps.pos().getX();
            final float y = blockProps.pos().getY();
            final float z = blockProps.pos().getZ();
            final float red = blockProps.color().red() / 255f;
            final float green = blockProps.color().green() / 255f;
            final float blue = blockProps.color().blue() / 255f;
            final float alpha = 1.0f;

            Matrix4f matrix = matrixStack.peek().getPositionMatrix();

            // Bottom face
            bufferBuilder.vertex(matrix, x, y, z).color(red, green, blue, alpha);
            bufferBuilder.vertex(matrix, x + size, y, z).color(red, green, blue, alpha);

            bufferBuilder.vertex(matrix, x + size, y, z).color(red, green, blue, alpha);
            bufferBuilder.vertex(matrix, x + size, y, z + size).color(red, green, blue, alpha);

            bufferBuilder.vertex(matrix, x + size, y, z + size).color(red, green, blue, alpha);
            bufferBuilder.vertex(matrix, x, y, z + size).color(red, green, blue, alpha);

            bufferBuilder.vertex(matrix, x, y, z + size).color(red, green, blue, alpha);
            bufferBuilder.vertex(matrix, x, y, z).color(red, green, blue, alpha);

            // Top face
            bufferBuilder.vertex(matrix, x, y + size, z).color(red, green, blue, alpha);
            bufferBuilder.vertex(matrix, x + size, y + size, z).color(red, green, blue, alpha);

            bufferBuilder.vertex(matrix, x + size, y + size, z).color(red, green, blue, alpha);
            bufferBuilder.vertex(matrix, x + size, y + size, z + size).color(red, green, blue, alpha);

            bufferBuilder.vertex(matrix, x + size, y + size, z + size).color(red, green, blue, alpha);
            bufferBuilder.vertex(matrix, x, y + size, z + size).color(red, green, blue, alpha);

            bufferBuilder.vertex(matrix, x, y + size, z + size).color(red, green, blue, alpha);
            bufferBuilder.vertex(matrix, x, y + size, z).color(red, green, blue, alpha);

            // Vertical edges
            bufferBuilder.vertex(matrix, x, y, z).color(red, green, blue, alpha);
            bufferBuilder.vertex(matrix, x, y + size, z).color(red, green, blue, alpha);

            bufferBuilder.vertex(matrix, x + size, y, z).color(red, green, blue, alpha);
            bufferBuilder.vertex(matrix, x + size, y + size, z).color(red, green, blue, alpha);

            bufferBuilder.vertex(matrix, x + size, y, z + size).color(red, green, blue, alpha);
            bufferBuilder.vertex(matrix, x + size, y + size, z + size).color(red, green, blue, alpha);

            bufferBuilder.vertex(matrix, x, y, z + size).color(red, green, blue, alpha);
            bufferBuilder.vertex(matrix, x, y + size, z + size).color(red, green, blue, alpha);
        }

        matrixStack.pop();

        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.lineWidth(1.0f);
    }

    private static void rebuildVertexBuffer(WorldRenderContext context) {
        requestedRefresh.set(false);
        if (vertexBuffer != null) {
            vertexBuffer.close();
            vertexBuffer = null;
        }

        BufferAllocator allocator = new BufferAllocator(1024 * 1024);
        BufferBuilder bufferBuilder = new BufferBuilder(allocator, VertexFormat.DrawMode.LINES, VertexFormats.POSITION_COLOR);

        // Calculate camera offset
        Vec3d cameraPos = context.camera().getPos();

        for (BlockPosWithColor blockProps : ScanTask.renderQueue) {
            if (blockProps == null) continue;

            final float size = 1.0f;
            final float x = blockProps.pos().getX() - (float)cameraPos.x;
            final float y = blockProps.pos().getY() - (float)cameraPos.y;
            final float z = blockProps.pos().getZ() - (float)cameraPos.z;
            final float red = blockProps.color().red() / 255f;
            final float green = blockProps.color().green() / 255f;
            final float blue = blockProps.color().blue() / 255f;

            renderLineBox(bufferBuilder, context.matrixStack(),
                    x, y, z, x + size, y + size, z + size,
                    red, green, blue, 0.8f);
        }

        vertexBuffer = new VertexBuffer(GlUsage.STATIC_WRITE);
        BuiltBuffer builtBuffer = bufferBuilder.end();
        vertexBuffer.bind();
        vertexBuffer.upload(builtBuffer);
        vertexCount = builtBuffer.getDrawParameters().vertexCount();
        builtBuffer.close();
        VertexBuffer.unbind();
        allocator.close();
        LOGGER.info("Vertex buffer rebuilt, vertexCount: " + vertexCount);
    }

    private static void renderLineBoxWithConsumer(VertexConsumer consumer, MatrixStack matrixStack,
                                                  float x1, float y1, float z1, float x2, float y2, float z2,
                                                  float red, float green, float blue, float alpha) {

        Matrix4f matrix = matrixStack.peek().getPositionMatrix();

        consumer.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha);
        consumer.vertex(matrix, x2, y1, z1).color(red, green, blue, alpha);

        consumer.vertex(matrix, x2, y1, z1).color(red, green, blue, alpha);
        consumer.vertex(matrix, x2, y1, z2).color(red, green, blue, alpha);

        consumer.vertex(matrix, x2, y1, z2).color(red, green, blue, alpha);
        consumer.vertex(matrix, x1, y1, z2).color(red, green, blue, alpha);

        consumer.vertex(matrix, x1, y1, z2).color(red, green, blue, alpha);
        consumer.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha);

        // Top face (y2) - 4 lines
        consumer.vertex(matrix, x1, y2, z1).color(red, green, blue, alpha);
        consumer.vertex(matrix, x2, y2, z1).color(red, green, blue, alpha);

        consumer.vertex(matrix, x2, y2, z1).color(red, green, blue, alpha);
        consumer.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha);

        consumer.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha);
        consumer.vertex(matrix, x1, y2, z2).color(red, green, blue, alpha);

        consumer.vertex(matrix, x1, y2, z2).color(red, green, blue, alpha);
        consumer.vertex(matrix, x1, y2, z1).color(red, green, blue, alpha);

        // Vertical edges - 4 lines
        consumer.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha);
        consumer.vertex(matrix, x1, y2, z1).color(red, green, blue, alpha);

        consumer.vertex(matrix, x2, y1, z1).color(red, green, blue, alpha);
        consumer.vertex(matrix, x2, y2, z1).color(red, green, blue, alpha);

        consumer.vertex(matrix, x2, y1, z2).color(red, green, blue, alpha);
        consumer.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha);

        consumer.vertex(matrix, x1, y1, z2).color(red, green, blue, alpha);
        consumer.vertex(matrix, x1, y2, z2).color(red, green, blue, alpha);
    }

    private static void renderLineBox(BufferBuilder buffer, MatrixStack matrixStack,
                                      float x1, float y1, float z1, float x2, float y2, float z2,
                                      float red, float green, float blue, float alpha) {

        Matrix4f matrix = matrixStack.peek().getPositionMatrix();

        int r = (int)(red * 255);
        int g = (int)(green * 255);
        int b = (int)(blue * 255);
        int a = (int)(alpha * 255);

        // Bottom face (y1)
        buffer.vertex(matrix, x1, y1, z1).color(r, g, b, a).normal(0, 0, 1);
        buffer.vertex(matrix, x2, y1, z1).color(r, g, b, a).normal(0, 0, 1);

        buffer.vertex(matrix, x2, y1, z1).color(r, g, b, a).normal(0, 0, 1);
        buffer.vertex(matrix, x2, y1, z2).color(r, g, b, a).normal(0, 0, 1);

        buffer.vertex(matrix, x2, y1, z2).color(r, g, b, a).normal(0, 0, 1);
        buffer.vertex(matrix, x1, y1, z2).color(r, g, b, a).normal(0, 0, 1);

        buffer.vertex(matrix, x1, y1, z2).color(r, g, b, a).normal(0, 0, 1);
        buffer.vertex(matrix, x1, y1, z1).color(r, g, b, a).normal(0, 0, 1);

        // Top face (y2)
        buffer.vertex(matrix, x1, y2, z1).color(r, g, b, a).normal(0, 0, 1);
        buffer.vertex(matrix, x2, y2, z1).color(r, g, b, a).normal(0, 0, 1);

        buffer.vertex(matrix, x2, y2, z1).color(r, g, b, a).normal(0, 0, 1);
        buffer.vertex(matrix, x2, y2, z2).color(r, g, b, a).normal(0, 0, 1);

        buffer.vertex(matrix, x2, y2, z2).color(r, g, b, a).normal(0, 0, 1);
        buffer.vertex(matrix, x1, y2, z2).color(r, g, b, a).normal(0, 0, 1);

        buffer.vertex(matrix, x1, y2, z2).color(r, g, b, a).normal(0, 0, 1);
        buffer.vertex(matrix, x1, y2, z1).color(r, g, b, a).normal(0, 0, 1);

        // Vertical edges
        buffer.vertex(matrix, x1, y1, z1).color(r, g, b, a).normal(0, 0, 1);
        buffer.vertex(matrix, x1, y2, z1).color(r, g, b, a).normal(0, 0, 1);

        buffer.vertex(matrix, x2, y1, z1).color(r, g, b, a).normal(0, 0, 1);
        buffer.vertex(matrix, x2, y2, z1).color(r, g, b, a).normal(0, 0, 1);

        buffer.vertex(matrix, x2, y1, z2).color(r, g, b, a).normal(0, 0, 1);
        buffer.vertex(matrix, x2, y2, z2).color(r, g, b, a).normal(0, 0, 1);

        buffer.vertex(matrix, x1, y1, z2).color(r, g, b, a).normal(0, 0, 1);
        buffer.vertex(matrix, x1, y2, z2).color(r, g, b, a).normal(0, 0, 1);
    }
}
