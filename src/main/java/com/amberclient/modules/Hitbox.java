package com.amberclient.modules;

import com.amberclient.utils.Module;

import net.minecraft.text.Text;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class Hitbox extends Module {
    public static final String MOD_ID = "amberclient-hitbox";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static boolean isHitboxModuleEnabled;
    private static boolean calculatingTarget = false;

    public Hitbox() {
        super("Hitbox", "Increases hitboxes' size", "Combat");
    }

    @Override
    public void onEnable() {
        getClient().player.sendMessage(
                Text.literal("§4[§cAmberClient§4] §c§l" + getName() + " §r§cenabled"), true);

        isHitboxModuleEnabled = true;
    }

    @Override
    public void onDisable() {
        getClient().player.sendMessage(
                Text.literal("§4[§cAmberClient§4] §c§l" + getName() + " §r§cdisabled"), true);

        isHitboxModuleEnabled = false;
    }

    public static boolean isCalculatingTarget() {
        return calculatingTarget;
    }

    public static void setCalculatingTarget(boolean state) {
        calculatingTarget = state;
    }

    // Méthode de sécurité pour reset complet
    public static void securityReset() {
        isHitboxModuleEnabled = false;
        calculatingTarget = false; // Reset de sécurité
    }
}
