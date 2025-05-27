package com.amberclient.modules;

import com.amberclient.utils.Module;
import com.amberclient.utils.ConfigurableModule;
import com.amberclient.utils.ModuleSetting;
import net.minecraft.text.Text;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

public class ActiveMods extends Module implements ConfigurableModule {
    private final ModuleSetting enableRGB;

    private final List<ModuleSetting> settings;

    public ActiveMods() {
        super("Active mods", "Toggles display of active modules", "Miscellaneous");
        this.enabled = true;

        // Initialize settings
        this.enableRGB = new ModuleSetting("Enable RGB", "Use animated RGB text color", true);

        // Create settings list
        this.settings = new ArrayList<>();
        this.settings.add(enableRGB);
    }

    @Override
    protected void onEnable() {
        getClient().player.sendMessage(
                Text.literal("§4[§cAmberClient§4] §c§l" + getName() + " §r§cenabled"), true);
    }

    @Override
    protected void onDisable() {
        getClient().player.sendMessage(
                Text.literal("§4[§cAmberClient§4] §c§l" + getName() + " §r§cdisabled"), true);
    }

    @Override
    public List<ModuleSetting> getSettings() {
        return settings;
    }

    @Override
    public void onSettingChanged(ModuleSetting setting) {
        if (setting == enableRGB) { updateRGBSetting(); }
    }

    // Method to use parameters
    public boolean isRGBEnabled() {
        return enableRGB.getBooleanValue();
    }

    public void renderHUD(DrawContext context) {
        // Color calculation
        int textColorRGB;
        if (isRGBEnabled()) {
            long time = System.currentTimeMillis();
            float hue = (time % 5000) / 5000.0f; // Each 5 seconds
            textColorRGB = java.awt.Color.HSBtoRGB(hue, 1.0f, 1.0f);
        } else {
            textColorRGB = 0xFFFFFFFF; // Default = white
        }
    }

    private void updateRGBSetting() {
        if (getClient().player != null) {
            getClient().player.sendMessage(
                    Text.literal("§6RGB enabled: §l" + isRGBEnabled()),
                    true
            );
        }
    }
}
