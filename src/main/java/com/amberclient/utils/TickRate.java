package com.amberclient.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.minecraft.util.math.MathHelper;

public class TickRate {
    private static final TickRate INSTANCE = new TickRate();
    private final float[] tickRates = new float[20];
    private int nextIndex = 0;
    private long timeLastTimeUpdate = -1;
    private final long timeGameJoined;

    private TickRate() {
        timeGameJoined = System.currentTimeMillis();
    }

    public static void onPacket(WorldTimeUpdateS2CPacket packet) {
        long now = System.currentTimeMillis();
        float timeElapsed = (now - INSTANCE.timeLastTimeUpdate) / 1000.0F;
        INSTANCE.tickRates[INSTANCE.nextIndex] = MathHelper.clamp(20.0f / timeElapsed, 0.0f, 20.0f);
        INSTANCE.nextIndex = (INSTANCE.nextIndex + 1) % INSTANCE.tickRates.length;
        INSTANCE.timeLastTimeUpdate = now;
    }

    public static float getTickRate() {
        if (MinecraftClient.getInstance().world == null) return 0;
        if (System.currentTimeMillis() - INSTANCE.timeGameJoined < 4000) return 20;

        int numTicks = 0;
        float sumTickRates = 0.0f;
        for (float tickRate : INSTANCE.tickRates) {
            if (tickRate > 0) {
                sumTickRates += tickRate;
                numTicks++;
            }
        }
        return numTicks > 0 ? sumTickRates / numTicks : 20.0f;
    }

    public static float getTimeSinceLastTick() {
        long now = System.currentTimeMillis();
        if (now - INSTANCE.timeGameJoined < 4000) return 0;
        return (now - INSTANCE.timeLastTimeUpdate) / 1000f;
    }
}