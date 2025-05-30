package com.amberclient.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class RotationUtils {
    public static Rotation getNeededRotations(Vec3d vec) {
        MinecraftClient client = MinecraftClient.getInstance();
        Vec3d playerPos = client.player.getEyePos();
        Vec3d toTarget = vec.subtract(playerPos).normalize();
        float yaw = (float) (Math.toDegrees(Math.atan2(toTarget.z, toTarget.x)) - 90);
        float pitch = (float) (Math.toDegrees(-Math.asin(toTarget.y)));
        return new Rotation(yaw, pitch);
    }

    public static float limitAngleChange(float current, float target) {
        float delta = MathHelper.wrapDegrees(target - current);
        return current + MathHelper.clamp(delta, -10.0F, 10.0F);
    }
}

class Rotation {
    private final float yaw;
    private final float pitch;

    Rotation(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = MathHelper.clamp(pitch, -90.0F, 90.0F);
    }

    public float yaw() { return yaw; }
    public float pitch() { return pitch; }
}