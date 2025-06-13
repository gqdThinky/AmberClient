package com.amberclient.modules.combat;

import com.amberclient.utils.module.ConfigurableModule;
import com.amberclient.utils.module.Module;
import com.amberclient.utils.module.ModuleSettings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Velocity extends Module implements ConfigurableModule {

    private final ModuleSettings horizontalScale =
            new ModuleSettings("Horizontal Scale", "X/Z velocity scale", 0.8, 0.0, 1.0, 0.05);
    private final ModuleSettings verticalScale =
            new ModuleSettings("Vertical Scale", "Y velocity scale", 0.8, 0.0, 1.0, 0.05);
    private final ModuleSettings contextualReduction =
            new ModuleSettings("Contextual", "Modify reduction based on sneaking or being grounded", true);
    private final ModuleSettings randomization =
            new ModuleSettings("Randomization", "Apply randomness to avoid detection", true);

    private final List<ModuleSettings> settings = new ArrayList<>();

    private Vec3d lastVelocity = Vec3d.ZERO;
    private final double[] recentReductions = new double[5];
    private int reductionIndex = 0;

    private static Velocity instance;

    public Velocity() {
        super("Velocity", "Reduces knockback", "Combat");
        settings.add(horizontalScale);
        settings.add(verticalScale);
        settings.add(contextualReduction);
        settings.add(randomization);
        instance = this;
    }

    public static Velocity getInstance() {
        return instance;
    }

    @Override
    public void onDisable() {
        lastVelocity = Vec3d.ZERO;
        reductionIndex = 0;
        Arrays.fill(recentReductions, 1.0);
    }

    @Override
    public void onTick() {
        if (!isEnabled()) return;

        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;

        Vec3d currentVelocity = player.getVelocity();

        if (isSignificantKnockback(currentVelocity)) {
            Vec3d reducedVelocity = calculateReducedVelocity(player, currentVelocity);
            player.setVelocity(reducedVelocity);
        }

        lastVelocity = currentVelocity;
    }

    public boolean isSignificantKnockback(Vec3d velocity) {
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return false;

        boolean wasHitRecently = player.hurtTime > 0;
        if (!wasHitRecently) return false;

        boolean hasHorizontal = velocity.horizontalLength() > 0.15;
        boolean hasVertical = velocity.y > 0.1;
        boolean velocityChanged = velocity.lengthSquared() > lastVelocity.lengthSquared() * 1.3;

        return (hasHorizontal || hasVertical) && velocityChanged;
    }

    public boolean isSignificantKnockback(Vec3d velocity, Vec3d previousVelocity) {
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return false;

        boolean wasHitRecently = player.hurtTime > 0;
        if (!wasHitRecently) return false;

        boolean hasHorizontal = velocity.horizontalLength() > 0.15;
        boolean hasVertical = velocity.y > 0.1;
        boolean velocityChanged = velocity.lengthSquared() > previousVelocity.lengthSquared() * 1.3;

        return (hasHorizontal || hasVertical) && velocityChanged;
    }

    public Vec3d calculateReducedVelocity(PlayerEntity player, Vec3d originalVelocity) {
        double horizontal = horizontalScale.getDoubleValue();
        double vertical = verticalScale.getDoubleValue();

        // Apply contextual modifiers
        if (contextualReduction.getBooleanValue()) {
            if (player.isSneaking()) horizontal *= 1.5;
            if (player.isOnGround()) horizontal *= 1.1;

            float healthRatio = player.getHealth() / player.getMaxHealth();
            horizontal *= (0.8 + healthRatio * 0.2);
        }

        // Add randomness
        if (randomization.getBooleanValue()) {
            double factor = 0.95 + Math.random() * 0.1;
            horizontal *= factor;
            vertical *= factor;

            recentReductions[reductionIndex] = factor;
            reductionIndex = (reductionIndex + 1) % recentReductions.length;
        }

        horizontal = MathHelper.clamp(horizontal, 0.0, 1.0);
        vertical = MathHelper.clamp(vertical, 0.0, 1.0);

        return new Vec3d(
                originalVelocity.x * horizontal,
                originalVelocity.y * vertical,
                originalVelocity.z * horizontal
        );
    }

    @Override
    public List<ModuleSettings> getSettings() {
        return settings;
    }
}