package com.amberclient.events;

import com.amberclient.modules.render.EntityESP;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

import java.awt.Color;

public class EntityRenderHandler {

//    public static void register() {
//        WorldRenderEvents.AFTER_ENTITIES.register((context) -> {
//            if (!EntityESP.isModuleEnabled()) return;
//            renderEntityOutlines(context.matrixStack(), context.tickCounter().getTickDelta(true));
//        });
//    }
//
//    private static void renderEntityOutlines(MatrixStack matrices, float tickDelta) {
//        MinecraftClient client = MinecraftClient.getInstance();
//        if (client.world == null || client.player == null) return;
//
//        EntityESP esp = EntityESP.getInstance();
//        if (esp == null) return;
//
//        EntityRenderDispatcher dispatcher = client.getEntityRenderDispatcher();
//        Vec3d cameraPos = client.gameRenderer.getCamera().getPos();
//        OutlineVertexConsumerProvider outlineProvider = client.getBufferBuilders().getOutlineVertexConsumers();
//
//        for (Entity entity : client.world.getEntities()) {
//            if (entity == client.player || !(entity instanceof LivingEntity)) continue;
//            if (!esp.shouldRenderEntity(entity)) continue;
//
//            Color outlineColor = getEntityColor(esp, entity);
//            if (outlineColor == null) continue;
//
//            outlineProvider.setColor(
//                    outlineColor.getRed(),
//                    outlineColor.getGreen(),
//                    outlineColor.getBlue(),
//                    255
//            );
//
//            Vec3d entityPos = entity.getPos();
//            double x = entityPos.x - cameraPos.x;
//            double y = entityPos.y - cameraPos.y;
//            double z = entityPos.z - cameraPos.z;
//
//            matrices.push();
//            matrices.translate(x, y, z);
//
//            try {
//                @SuppressWarnings("unchecked")
//                EntityRenderer<? super Entity, ?> renderer = dispatcher.getRenderer(entity);
//                if (renderer != null) {
//                    int light = renderer.getLight(entity, tickDelta);
//                    renderer.render(
//                            entity,
//                            matrices,
//                            outlineProvider, // Use outlineProvider for outlines
//                            light
//                    );
//                }
//            } catch (Exception e) {
//                EntityESP.LOGGER.debug("Erreur rendu contour entité: " + entity.getType(), e);
//            }
//
//            matrices.pop();
//        }
//    }
//
//    private static Color getEntityColor(EntityESP esp, Entity entity) {
//        if (entity instanceof net.minecraft.entity.player.PlayerEntity && esp.player.getBooleanValue()) {
//            return hexToColor(esp.playerColor.getStringValue());
//        } else if (entity instanceof net.minecraft.entity.mob.HostileEntity && esp.mob.getBooleanValue()) {
//            return hexToColor(esp.mobColor.getStringValue());
//        } else if (entity instanceof net.minecraft.entity.passive.PassiveEntity && esp.animal.getBooleanValue()) {
//            return hexToColor(esp.animalColor.getStringValue());
//        }
//        return null;
//    }
//
//    private static Color hexToColor(String hex) {
//        try {
//            if (hex.startsWith("#")) {
//                hex = hex.substring(1);
//            }
//            return new Color(Integer.parseInt(hex, 16));
//        } catch (NumberFormatException e) {
//            return Color.WHITE;
//        }
//    }
}