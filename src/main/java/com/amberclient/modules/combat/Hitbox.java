package com.amberclient.modules.combat;

import com.amberclient.events.EventManager;
import com.amberclient.events.PreMotionListener;
import com.amberclient.events.PostMotionListener;
import com.amberclient.utils.module.ConfigurableModule;
import com.amberclient.utils.module.Module;
import com.amberclient.utils.module.ModuleSettings;
import com.amberclient.utils.rotation.RotationFaker;
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

    private final List<ModuleSettings> settings;
    private final ModuleSettings expandX;
    private final ModuleSettings expandYUp;
    private final ModuleSettings expandZ;
    private final RotationFaker rotationFaker = new RotationFaker();

    public Hitbox() {
        super("Hitbox", "(DETECTABLE) Increases hitboxes' size", "Combat");
        instance = this;

        expandX = new ModuleSettings("Expand X", "Horizontal hitbox expansion", 0.25, 0.0, 2.0, 0.05);
        expandYUp = new ModuleSettings("Expand Y Up", "Upward hitbox expansion", 0.6, 0.0, 2.0, 0.05);
        expandZ = new ModuleSettings("Expand Z", "Depth hitbox expansion", 0.25, 0.0, 2.0, 0.05);

        settings = new ArrayList<>();
        settings.add(expandX);
        settings.add(expandYUp);
        settings.add(expandZ);
    }

    @Override
    public void onEnable() {
        isHitboxModuleEnabled = true;
        EventManager.getInstance().add(PreMotionListener.class, rotationFaker);
        EventManager.getInstance().add(PostMotionListener.class, rotationFaker);
    }

    @Override
    public void onDisable() {
        isHitboxModuleEnabled = false;
        EventManager.getInstance().remove(PreMotionListener.class, rotationFaker);
        EventManager.getInstance().remove(PostMotionListener.class, rotationFaker);
    }

    @Override
    public void onTick() {
        if (!isHitboxModuleEnabled) return;
    }

    @Override
    public List<ModuleSettings> getSettings() {
        return settings;
    }

    @Override
    public void onSettingChanged(ModuleSettings setting) {
        if (setting == expandX || setting == expandYUp || setting == expandZ) {
            if (getClient().player != null) {
                getClient().player.sendMessage(
                        Text.literal("§6Hitbox expansion updated: X=" + expandX.getDoubleValue() +
                                ", YUp=" + expandYUp.getDoubleValue() +
                                ", Z=" + expandZ.getDoubleValue()),
                        true);
            }
        }
    }

    public static void securityReset() {
        isHitboxModuleEnabled = false;
        calculatingTarget = false;
    }

    public static boolean isCalculatingTarget() { return calculatingTarget; }
    public static void setCalculatingTarget(boolean state) { calculatingTarget = state; }

    public static Hitbox getInstance() { return instance; }

    public double getExpandX() { return expandX.getDoubleValue(); }
    public double getExpandYUp() { return expandYUp.getDoubleValue(); }
    public double getExpandZ() { return expandZ.getDoubleValue(); }

    // Méthode pour accéder à RotationFaker depuis les mixins
    public RotationFaker getRotationFaker() { return rotationFaker; }
}