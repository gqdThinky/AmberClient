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
    private final ModuleSetting rangeSetting;
    private final ModuleSetting showNametagsSetting;
    private final ModuleSetting showHealthSetting;
    private final List<ModuleSetting> settings;

    private static EntityESP INSTANCE;

    public EntityESP() {
        super("Entity ESP", "Display outlines around entity models (players & mobs)", "Render");
        INSTANCE = this;

        renderPlayersSetting = new ModuleSetting("Render Players", "Displays outlines for players", true);
        renderMobsSetting = new ModuleSetting("Render Mobs", "Displays outlines for mobs", true);
        rangeSetting = new ModuleSetting("Render Range", "X (in chunks)", 4, 1, 8, 1);
        showNametagsSetting = new ModuleSetting("Show Nametags", "Shows entity names above them", true);
        showHealthSetting = new ModuleSetting("Show Health", "Shows entity health above them", true);

        settings = new ArrayList<>();
        settings.add(renderPlayersSetting);
        settings.add(renderMobsSetting);
        settings.add(rangeSetting);
        settings.add(showNametagsSetting);
        settings.add(showHealthSetting);
    }

    public static EntityESP getInstance() {
        return INSTANCE;
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
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
    }

    // Getters for settings
    public ModuleSetting getRenderPlayersSetting() {
        return renderPlayersSetting;
    }

    public ModuleSetting getRenderMobsSetting() {
        return renderMobsSetting;
    }

    public ModuleSetting getShowNametagsSetting() {
        return showNametagsSetting;
    }

    public ModuleSetting getShowHealthSetting() {
        return showHealthSetting;
    }
}