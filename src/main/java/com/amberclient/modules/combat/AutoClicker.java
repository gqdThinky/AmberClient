package com.amberclient.modules.combat;

import com.amberclient.utils.module.ConfigurableModule;
import com.amberclient.utils.module.Module;
import com.amberclient.utils.module.ModuleSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class AutoClicker extends Module implements ConfigurableModule {
    public static final String MOD_ID = "amberclient-autoclicker";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    private final List<ModuleSetting> settings;
    private final ModuleSetting clickDelay;
    private final ModuleSetting useWeaponSpeed;
    private long lastClickTime;

    public AutoClicker() {
        super("AutoClicker", "Auto clicks when aiming at an entity", "Combat");

        useWeaponSpeed = new ModuleSetting("Use Weapon Speed", "Use the attack delay of the held item", true);
        clickDelay = new ModuleSetting("Default Click Delay", "Delay between clicks in ms (when weapon speed is disabled)", 100.0, 50.0, 500.0, 10.0);
        lastClickTime = 0;

        settings = new ArrayList<>();
        settings.add(useWeaponSpeed);
        settings.add(clickDelay);
    }

    @Override
    protected void onEnable() {
        LOGGER.info("AutoClicker module enabled");
    }

    @Override
    protected void onDisable() {
        LOGGER.info("AutoClicker module disabled");
    }

    private double getWeaponAttackDelay() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return 100.0;

        ItemStack heldItem = client.player.getMainHandStack();
        if (heldItem.isEmpty()) {
            // No weapon = bare-handed attack, player's basic attack speed
            double baseAttackSpeed = client.player.getAttributeValue(EntityAttributes.ATTACK_SPEED);
            return 1000.0 / baseAttackSpeed; // Convert in ms
        }

        double attackSpeed = client.player.getAttributeValue(EntityAttributes.ATTACK_SPEED);

        return 1000.0 / attackSpeed;
    }

    @Override
    public void onTick() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        HitResult hitResult = client.crosshairTarget;
        if (hitResult != null && hitResult.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityHitResult = (EntityHitResult) hitResult;
            Entity target = entityHitResult.getEntity();
            long currentTime = System.currentTimeMillis();

            // Determining the time frame to be used
            double delayToUse;
            if (useWeaponSpeed.getBooleanValue()) {
                delayToUse = getWeaponAttackDelay();
            } else {
                delayToUse = clickDelay.getDoubleValue();
            }

            if (currentTime - lastClickTime >= delayToUse) {
                client.interactionManager.attackEntity(client.player, target);
                client.player.swingHand(Hand.MAIN_HAND);
                lastClickTime = currentTime;
            }
        }
    }

    @Override
    public List<ModuleSetting> getSettings() { return settings; }

    @Override
    public void onSettingChanged(ModuleSetting setting) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        if (setting == clickDelay) {
            client.player.sendMessage(
                    Text.literal("§6AutoClicker settings updated: Click Delay=" + clickDelay.getDoubleValue() + " ms"),
                    true);
            LOGGER.info("AutoClicker settings updated: Click Delay={} ms", clickDelay.getDoubleValue());
        } else if (setting == useWeaponSpeed) {
            String status = useWeaponSpeed.getBooleanValue() ? "enabled" : "disabled";
            client.player.sendMessage(
                    Text.literal("§6AutoClicker settings updated: Use Weapon Speed=" + status),
                    true);
            LOGGER.info("AutoClicker settings updated: Use Weapon Speed={}", status);
        }
    }
}