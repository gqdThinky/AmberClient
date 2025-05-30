package com.amberclient.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;

public class RotationFaker {
    private boolean fakeRotation;
    private float serverYaw;
    private float serverPitch;
    private float realYaw;
    private float realPitch;

    public void onPreMotion() {
        if (fakeRotation) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null) return;
            this.realYaw = client.player.getYaw();
            this.realPitch = client.player.getPitch();
            client.player.setYaw(serverYaw);
            client.player.setPitch(serverPitch);
        }
    }

    public void onPostMotion() {
        if (fakeRotation) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null) return;
            client.player.setYaw(realYaw);
            client.player.setPitch(realPitch);
            this.fakeRotation = false;
        }
    }

    public void faceVectorPacket(Vec3d vec) {
        Rotation needed = RotationUtils.getNeededRotations(vec);
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        this.fakeRotation = true;
        this.serverYaw = RotationUtils.limitAngleChange(client.player.getYaw(), needed.yaw());
        this.serverPitch = needed.pitch();
        // Envoyer un paquet réseau pour simuler la rotation
        client.player.networkHandler.sendPacket(
                new PlayerMoveC2SPacket.LookAndOnGround(
                        serverYaw,
                        serverPitch,
                        client.player.isOnGround(),
                        client.player.horizontalCollision // Ajout du paramètre horizontalCollision
                )
        );
    }

    public float getServerYaw() {
        MinecraftClient client = MinecraftClient.getInstance();
        return fakeRotation ? serverYaw : (client.player != null ? client.player.getYaw() : 0);
    }

    public float getServerPitch() {
        MinecraftClient client = MinecraftClient.getInstance();
        return fakeRotation ? serverPitch : (client.player != null ? client.player.getPitch() : 0);
    }
}