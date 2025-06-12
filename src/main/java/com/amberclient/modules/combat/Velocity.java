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

    private final ModuleSettings horizontalScale = new ModuleSettings("Horizontal Scale", "X/Z velocity scale", 0.8, 0.0, 1.0, 0.05);
    private final ModuleSettings verticalScale = new ModuleSettings("Vertical Scale", "Y velocity scale", 0.8, 0.0, 1.0, 0.05);
    private final ModuleSettings contextualReduction = new ModuleSettings("Contextual", "Reduce based on game context", true);
    private final ModuleSettings randomization = new ModuleSettings("Randomization", "Add slight randomness to avoid patterns", true);

    private final List<ModuleSettings> settings = new ArrayList<>();

    private Vec3d lastVelocity = Vec3d.ZERO;
    private final double[] recentReductions = new double[5];
    private int reductionIndex = 0;

    public Velocity() {
        super("Velocity", "Natural knockback reduction", "Combat");
        settings.add(horizontalScale);
        settings.add(verticalScale);
        settings.add(contextualReduction);
        settings.add(randomization);
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

    private boolean isSignificantKnockback(Vec3d velocity) {
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return false;

        boolean isBeingDamaged = player.hurtTime > 0;
        if (!isBeingDamaged) return false;

        double horizontalThreshold = 0.15;
        double verticalThreshold = 0.1;

        boolean hasHorizontalKnockback = velocity.horizontalLength() > horizontalThreshold;
        boolean hasVerticalKnockback = velocity.y > verticalThreshold;
        boolean significantChange = velocity.lengthSquared() > lastVelocity.lengthSquared() * 1.3;

        return (hasHorizontalKnockback || hasVerticalKnockback) && significantChange;
    }

    private Vec3d calculateReducedVelocity(PlayerEntity player, Vec3d originalVelocity) {
        double horizontalReduction = horizontalScale.getDoubleValue();
        double verticalReduction = verticalScale.getDoubleValue();

        if (contextualReduction.getBooleanValue()) {
            if (player.isSneaking()) {
                horizontalReduction *= 1.2;
            }

            if (player.isOnGround()) {
                horizontalReduction *= 1.1;
            }

            float healthFactor = player.getHealth() / player.getMaxHealth();
            horizontalReduction *= (0.8 + healthFactor * 0.2);
        }

        if (randomization.getBooleanValue()) {
            double randomFactor = 0.95 + (Math.random() * 0.1);
            horizontalReduction *= randomFactor;
            verticalReduction *= randomFactor;

            recentReductions[reductionIndex] = randomFactor;
            reductionIndex = (reductionIndex + 1) % recentReductions.length;
        }

        horizontalReduction = MathHelper.clamp(horizontalReduction, 0.0, 1.0);
        verticalReduction = MathHelper.clamp(verticalReduction, 0.0, 1.0);

        return new Vec3d(
                originalVelocity.x * horizontalReduction,
                originalVelocity.y * verticalReduction,
                originalVelocity.z * horizontalReduction
        );
    }

    @Override
    public List<ModuleSettings> getSettings() {
        return settings;
    }
}