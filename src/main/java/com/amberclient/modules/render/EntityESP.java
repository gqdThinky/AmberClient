package com.amberclient.modules.render;

import com.amberclient.utils.module.ConfigurableModule;
import com.amberclient.utils.module.Module;
import com.amberclient.utils.module.ModuleSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class EntityESP extends Module implements ConfigurableModule {
    public static final String MOD_ID = "amberclient-entityesp";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    private final ModuleSetting renderPlayersSetting;
    private final ModuleSetting renderMobsSetting;
    private final List<ModuleSetting> settings;

    // Configuration options
    private float lineWidth = 2.0f;
    private float[] playerColor = {1.0f, 0.0f, 0.0f, 1.0f}; // Red for players
    private float[] mobColor = {0.0f, 1.0f, 0.0f, 1.0f};    // Green for mobs

    public EntityESP() {
        super("Entity ESP", "Affiche des contours autour des modèles d'entités", "Render");

        renderPlayersSetting = new ModuleSetting("Render Players", "Affiche les contours pour les joueurs", true);
        renderMobsSetting = new ModuleSetting("Render Mobs", "Affiche les contours pour les mobs", true);

        settings = new ArrayList<>();
        settings.add(renderPlayersSetting);
        settings.add(renderMobsSetting);
    }

    @Override
    public void onEnable() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(
                    Text.literal("§4[§cAmberClient§4] §c§l" + getName() + " §r§cactivated"), true);
        }
        LOGGER.info(getName() + " module enabled");
    }

    @Override
    public void onDisable() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(
                    Text.literal("§4[§cAmberClient§4] §c§l" + getName() + " §r§cdeactivated"), true);
        }
        LOGGER.info(getName() + " module disabled");
    }

    @Override
    public List<ModuleSetting> getSettings() {
        return settings;
    }

    @Override
    public void onSettingChanged(ModuleSetting setting) {
        // Réagir aux changements si nécessaire (optionnel)
    }

    // Getters pour les paramètres
    public ModuleSetting getRenderPlayersSetting() {
        return renderPlayersSetting;
    }

    public ModuleSetting getRenderMobsSetting() {
        return renderMobsSetting;
    }

    // Getters et setters existants
    public float getLineWidth() { return lineWidth; }
    public void setLineWidth(float lineWidth) { this.lineWidth = lineWidth; }
    public float[] getPlayerColor() { return playerColor; }
    public void setPlayerColor(float[] playerColor) { this.playerColor = playerColor; }
    public float[] getMobColor() { return mobColor; }
    public void setMobColor(float[] mobColor) { this.mobColor = mobColor; }
}