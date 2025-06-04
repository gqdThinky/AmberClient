package com.amberclient.modules.hud;

import com.amberclient.utils.module.Module;
import com.amberclient.utils.module.ConfigurableModule;
import com.amberclient.utils.module.ModuleSetting;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class Transparency extends Module implements ConfigurableModule {
    public static final String MOD_ID = "amberclient-transparency";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    private final ModuleSetting transparencyLevel;
    private final List<ModuleSetting> settings;

    public Transparency() {
        super("Transparency", "Change the transparency of the click gui", "HUD");
        this.enabled = true;

        this.transparencyLevel = new ModuleSetting("Transparency Level", "Adjusts GUI transparency", 0.69, 0.01, 1.0, 0.01);
        this.settings = new ArrayList<>();
        this.settings.add(transparencyLevel);
    }

    @Override
    public List<ModuleSetting> getSettings() {
        return settings;
    }

    @Override
    public void onSettingChanged(ModuleSetting setting) {
        if (setting == transparencyLevel) {
            if (getClient().player != null) {
                getClient().player.sendMessage(
                        Text.literal("§6Transparency Level set to: §l" + String.format("%.2f", transparencyLevel.getDoubleValue())),
                        true
                );
            }
        }
    }

    public double getTransparencyLevel() {
        return transparencyLevel.getDoubleValue();
    }

    @Override
    public void onEnable() {
        getClient().player.sendMessage(
                Text.literal("§4[§cAmberClient§4] §c§l" + getName() + " §r§cenabled"), true);
        LOGGER.info("Transparency module enabled");
    }

    @Override
    public void onDisable() {
        getClient().player.sendMessage(
                Text.literal("§4[§cAmberClient§4] §c§l" + getName() + " §r§cdisabled"), true);
        LOGGER.info("Transparency module disabled");
    }
}
