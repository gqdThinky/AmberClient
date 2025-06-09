package com.amberclient.modules.player;

import com.amberclient.utils.module.ConfigurableModule;
import com.amberclient.utils.module.Module;
import com.amberclient.utils.module.ModuleSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class FastBreak extends Module implements ConfigurableModule {
    public static final String MOD_ID = "amberclient-fastbreak";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    private static FastBreak instance;
    public static boolean isFastBreakEnabled;

    private final List<ModuleSetting> settings;
    private final ModuleSetting activationChance;
    private final ModuleSetting legitMode;

    public FastBreak() {
        super("FastBreak", "Breaks blocks way faster", "Player");
        instance = this;
        isFastBreakEnabled = false;

        this.activationChance = new ModuleSetting("Activation Chance", "Probability of FastBreak activating for a block. Lower values reduce anti-cheat detection.",1.0, 0.0, 1.0, 0.01);
        this.legitMode = new ModuleSetting("Legit Mode", "Only removes block-breaking delay without speeding up the process. Safer for anti-cheat.",false);

        this.settings = new ArrayList<>();
        this.settings.add(activationChance);
        this.settings.add(legitMode);
    }

    public static FastBreak getInstance() {
        return instance;
    }

    @Override
    public void onEnable() { isFastBreakEnabled = true; }

    @Override
    public void onDisable() { isFastBreakEnabled = false; }

    @Override
    public List<ModuleSetting> getSettings() {
        List<ModuleSetting> settings = new ArrayList<>();
        settings.add(activationChance);
        settings.add(legitMode);
        return settings;
    }

    public double getActivationChance() {
        return activationChance.getDoubleValue();
    }

    public boolean isLegitMode() {
        return legitMode.getBooleanValue();
    }
}