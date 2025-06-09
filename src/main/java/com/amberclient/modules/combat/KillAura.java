package com.amberclient.modules.combat;

import com.amberclient.utils.module.ConfigurableModule;
import com.amberclient.utils.module.Module;
import com.amberclient.utils.module.ModuleSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Tameable;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

import static com.amberclient.utils.general.TickRate.getTickRate;
import static com.amberclient.utils.general.TickRate.getTimeSinceLastTick;

/*
    Thanks to https://github.com/enzzzh for the KillAura module base!
    Implemented by https://github.com/gqdThinky.
 */

public class KillAura extends Module implements ConfigurableModule {
    public static final String MOD_ID = "amberclient-killaura";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    private final List<ModuleSetting> settings;
    private long lastClickTime = 0;
    private int scanTick = 0;
    private boolean attacking = false;

    // Settings
    private final ModuleSetting range = new ModuleSetting("Range", "Maximum attack range", 3.5, 0.5, 6, 0.25);
    private final ModuleSetting clickDelay = new ModuleSetting("Click Delay", "Delay between clicks in ms", 100.0, 25.0, 500.0, 10.0);
    private final ModuleSetting useWeaponSpeed = new ModuleSetting("Use Weapon Speed", "Use the weapon's attack speed", true);
    private final ModuleSetting onlyOnClick = new ModuleSetting("Only on Click", "Attacks only when left-clicking", false);
    private final ModuleSetting onlyOnLook = new ModuleSetting("Only on Look", "Attacks only when looking at an entity", false);
    private final ModuleSetting wallsRange = new ModuleSetting("Walls Range", "Range through walls", 3.5, 0, 6, 0.1);
    private final ModuleSetting ignoreTamed = new ModuleSetting("Ignore Tamed", "Ignore tamed mobs", false);
    private final ModuleSetting pauseOnLag = new ModuleSetting("Pause on Lag", "Pause when server lags", true);
    private final ModuleSetting tpsSync = new ModuleSetting("TPS Sync", "Sync attack speed with server TPS", true);

    public KillAura() {
        super("KillAura", "Automatically attacks nearby entities", "Combat");
        settings = List.of(range, clickDelay, useWeaponSpeed, onlyOnClick, onlyOnLook, wallsRange, ignoreTamed, pauseOnLag, tpsSync);
    }

    @Override
    public void onEnable() {
        MinecraftClient client = MinecraftClient.getInstance();
        LOGGER.info("{} module activated", getName());
    }

    @Override
    public void onDisable() {
        stopAttacking();
        MinecraftClient client = MinecraftClient.getInstance();
        LOGGER.info("{} module disabled", getName());
    }

    private GameMode getGameMode(PlayerEntity player) {
        if (player == null || MinecraftClient.getInstance().getNetworkHandler() == null) return null;
        PlayerListEntry entry = MinecraftClient.getInstance().getNetworkHandler().getPlayerListEntry(player.getUuid());
        return entry != null ? entry.getGameMode() : null;
    }

    @Override
    public void onTick() {
        if (!isEnabled()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        if (player == null || client.world == null || !player.isAlive() || getGameMode(player) == GameMode.SPECTATOR) {
            stopAttacking();
            return;
        }

        if (onlyOnClick.getBooleanValue() && !client.options.attackKey.isPressed()) {
            stopAttacking();
            return;
        }
        if (pauseOnLag.getBooleanValue() && getTimeSinceLastTick() >= 1f) {
            stopAttacking();
            return;
        }

        if (scanTick++ % 5 != 0) return; // Scan only every 5 ticks

        // Collect target
        LivingEntity target = null;
        if (onlyOnLook.getBooleanValue()) {
            Entity targeted = client.targetedEntity;
            if (targeted instanceof LivingEntity living && entityCheck(living)) {
                target = living;
            }
        } else {
            double closestDist = Double.MAX_VALUE;
            for (Entity entity : client.world.getEntities()) {
                if (entity instanceof LivingEntity living && entityCheck(living)) {
                    double dist = player.distanceTo(living);
                    if (dist < closestDist) {
                        closestDist = dist;
                        target = living;
                    }
                }
            }
        }

        if (target == null) {
            stopAttacking();
            return;
        }

        // Simulate human hesitation with a 5% chance to skip attack
        if (Math.random() < 0.05) return;

        double delayToUse;
        if (useWeaponSpeed.getBooleanValue()) {
            delayToUse = getWeaponAttackDelay();
        } else {
            delayToUse = clickDelay.getDoubleValue();
        }
        if (tpsSync.getBooleanValue()) {
            float tps = getTickRate();
            if (tps > 0) {
                delayToUse *= (20.0 / tps);
            }
        }
        double randomDelay = delayToUse * (0.8 + Math.random() * 0.4);

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClickTime >= randomDelay) {
            client.interactionManager.attackEntity(player, target);
            player.swingHand(Hand.MAIN_HAND);
            lastClickTime = currentTime;
            attacking = true;
        } else {
            attacking = false;
        }
    }

    // Improved weapon attack delay respecting server cooldown
    private double getWeaponAttackDelay() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return 100.0;

        ItemStack heldItem = client.player.getMainHandStack();
        float cooldown = client.player.getAttackCooldownProgress(0.0f);
        if (cooldown < 1.0f) {
            return Double.MAX_VALUE; // Not ready to attack yet
        }
        double attackSpeed = client.player.getAttributeValue(EntityAttributes.ATTACK_SPEED);
        return 1000.0 / attackSpeed;
    }

    // Enhanced entity check with FOV and range variation
    private boolean entityCheck(LivingEntity entity) {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        if (entity == player) return false;
        if (!entity.isAlive() || entity.isDead()) return false;
        if (!(entity instanceof PlayerEntity || entity instanceof MobEntity)) return false;

        // Check if entity is within player's field of view (±90°)
        if (!isInFOV(entity)) return false;

        double distance = player.distanceTo(entity);
        double effectiveRange = range.getDoubleValue() * (0.95 + Math.random() * 0.1); // ±5% variation
        if (distance > effectiveRange) return false;
        if (!canSeeEntity(entity) && distance > wallsRange.getDoubleValue()) return false;

        if (ignoreTamed.getBooleanValue() && entity instanceof Tameable tameable && tameable.getOwner() == player) return false;

        return true;
    }

    // Check if entity is within player's field of view
    private boolean isInFOV(LivingEntity entity) {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        Vec3d lookVec = player.getRotationVector();
        Vec3d entityVec = entity.getPos().subtract(player.getPos()).normalize();
        double dot = lookVec.dotProduct(entityVec);
        return dot > Math.cos(Math.toRadians(90)); // 90° FOV
    }

    private boolean canSeeEntity(Entity entity) {
        return MinecraftClient.getInstance().player.canSee(entity);
    }

    private void stopAttacking() {
        attacking = false;
    }

    @Override
    public List<ModuleSetting> getSettings() {
        return settings;
    }

    @Override
    public void onSettingChanged(ModuleSetting setting) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        if (setting == range) {
            client.player.sendMessage(Text.literal("§6KillAura settings updated: Range=" + range.getDoubleValue()), true);
            LOGGER.info("KillAura settings updated: Range={}", range.getDoubleValue());
        } else if (setting == clickDelay) {
            client.player.sendMessage(Text.literal("§6KillAura settings updated: Click Delay=" + clickDelay.getDoubleValue() + " ms"), true);
            LOGGER.info("KillAura settings updated: Click Delay={} ms", clickDelay.getDoubleValue());
        } else if (setting == useWeaponSpeed) {
            String status = useWeaponSpeed.getBooleanValue() ? "enabled" : "disabled";
            client.player.sendMessage(Text.literal("§6KillAura settings updated: Use Weapon Speed=" + status), true);
            LOGGER.info("KillAura settings updated: Use Weapon Speed={}", status);
        } else if (setting == onlyOnClick) {
            String status = onlyOnClick.getBooleanValue() ? "enabled" : "disabled";
            client.player.sendMessage(Text.literal("§6KillAura settings updated: Only on Click=" + status), true);
            LOGGER.info("KillAura settings updated: Only on Click={}", status);
        } else if (setting == onlyOnLook) {
            String status = onlyOnLook.getBooleanValue() ? "enabled" : "disabled";
            client.player.sendMessage(Text.literal("§6KillAura settings updated: Only on Look=" + status), true);
            LOGGER.info("KillAura settings updated: Only on Look={}", status);
        } else if (setting == wallsRange) {
            client.player.sendMessage(Text.literal("§6KillAura settings updated: Walls Range=" + wallsRange.getDoubleValue()), true);
            LOGGER.info("KillAura settings updated: Walls Range={}", wallsRange.getDoubleValue());
        } else if (setting == ignoreTamed) {
            String status = ignoreTamed.getBooleanValue() ? "enabled" : "disabled";
            client.player.sendMessage(Text.literal("§6KillAura settings updated: Ignore Tamed=" + status), true);
            LOGGER.info("KillAura settings updated: Ignore Tamed={}", status);
        } else if (setting == pauseOnLag) {
            String status = pauseOnLag.getBooleanValue() ? "enabled" : "disabled";
            client.player.sendMessage(Text.literal("§6KillAura settings updated: Pause on Lag=" + status), true);
            LOGGER.info("KillAura settings updated: Pause on Lag={}", status);
        } else if (setting == tpsSync) {
            String status = tpsSync.getBooleanValue() ? "enabled" : "disabled";
            client.player.sendMessage(Text.literal("§6KillAura settings updated: TPS Sync=" + status), true);
            LOGGER.info("KillAura settings updated: TPS Sync={}", status);
        }
    }
}