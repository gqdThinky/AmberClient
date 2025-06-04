package com.amberclient.utils.rotation;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

import static net.minecraft.client.MinecraftClient.getInstance;

public class Rotations {
    public static double getYaw(Entity entity) {
        double diffX = entity.getX() - getInstance().player.getX();
        double diffZ = entity.getZ() - getInstance().player.getZ();
        double yaw = Math.toDegrees(Math.atan2(diffZ, diffX)) - 90;
        return MathHelper.wrapDegrees((float) yaw);
    }

    public static double getPitch(Entity entity) {
        double y = entity.getY() + entity.getHeight() / 2;
        double diffX = entity.getX() - getInstance().player.getX();
        double diffY = y - (getInstance().player.getY() + getInstance().player.getEyeHeight(getInstance().player.getPose()));
        double diffZ = entity.getZ() - getInstance().player.getZ();
        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
        double pitch = -Math.toDegrees(Math.atan2(diffY, diffXZ));
        return MathHelper.wrapDegrees((float) pitch);
    }
}