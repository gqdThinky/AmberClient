package com.amberclient.modules.hud;

import com.amberclient.utils.module.Module;
import com.amberclient.utils.module.ConfigurableModule;
import com.amberclient.utils.module.ModuleSetting;
import net.minecraft.text.Text;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

public class ActiveMods extends Module implements ConfigurableModule {
    private final ModuleSetting enableRGB;

    private final List<ModuleSetting> settings;

    public ActiveMods() {
        super("Active mods", "Toggles display of active modules", "HUD");
        this.enabled = true;

        this.enableRGB = new ModuleSetting("Enable RGB", "Use animated RGB text color", true);

        this.settings = new ArrayList<>();
        this.settings.add(enableRGB);
    }

    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {
        
    }

    @Override
    public List<ModuleSetting> getSettings() {
        return settings;
    }

    @Override
    public void onSettingChanged(ModuleSetting setting) {
        if (setting == enableRGB) { updateRGBSetting(); }
    }

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
