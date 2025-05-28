package com.amberclient.modules;

import com.amberclient.utils.ConfigurableModule;
import com.amberclient.utils.Module;
import com.amberclient.utils.ModuleSetting;
import net.minecraft.text.Text;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import java.util.ArrayList;
import java.util.List;

public class Hitbox extends Module implements ConfigurableModule {
    public static final String MOD_ID = "amberclient-hitbox";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static boolean isHitboxModuleEnabled;
    private static boolean calculatingTarget = false;
    private static Hitbox instance;

    private final List<ModuleSetting> settings;
    private final ModuleSetting expandX;
    private final ModuleSetting expandYUp;

    public Hitbox() {
        super("Hitbox", "Increases hitboxes' size", "Combat");
        instance = this;

        // Initialize settings with min, max, and step values
        expandX = new ModuleSetting("Expand X", "Horizontal hitbox expansion", 0.25, 0.0, 2.0, 0.05);
        expandYUp = new ModuleSetting("Expand Y Up", "Upward hitbox expansion", 0.6, 0.0, 2.0, 0.05);

        // Create settings list
        settings = new ArrayList<>();
        settings.add(expandX);
        settings.add(expandYUp);
    }

    @Override
    public void onEnable() {
        getClient().player.sendMessage(
                Text.literal("§4[§cAmberClient§4] §c§l" + getName() + " §r§cenabled"), true);

        isHitboxModuleEnabled = true;
        LOGGER.info("Hitbox module enabled");
    }

    @Override
    public void onDisable() {
        getClient().player.sendMessage(
                Text.literal("§4[§cAmberClient§4] §c§l" + getName() + " §r§cdisabled"), true);

        isHitboxModuleEnabled = false;
        LOGGER.info("Hitbox module disabled");
    }

    @Override
    public List<ModuleSetting> getSettings() {
        return settings;
    }

    @Override
    public void onSettingChanged(ModuleSetting setting) {
        if (setting == expandX || setting == expandYUp) {
            if (getClient().player != null) {
                getClient().player.sendMessage(
                        Text.literal("§6Hitbox expansion updated: X=" + expandX.getDoubleValue() + ", YUp=" + expandYUp.getDoubleValue()),
                        true
                );
            }
        }
    }

    // Safety method for complete reset
    public static void securityReset() {
        isHitboxModuleEnabled = false;
        calculatingTarget = false;
    }

    public static boolean isCalculatingTarget() { return calculatingTarget; }
    public static void setCalculatingTarget(boolean state) { calculatingTarget = state; }

    public static Hitbox getInstance() { return instance; }

    public double getExpandX() { return expandX.getDoubleValue(); }
    public double getExpandYUp() { return expandYUp.getDoubleValue(); }
}
