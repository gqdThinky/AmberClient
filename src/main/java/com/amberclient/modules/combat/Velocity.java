package com.amberclient.modules.combat;

import com.amberclient.events.EventManager;
import com.amberclient.events.PreVelocityEvent;
import com.amberclient.events.PreVelocityListener;
import com.amberclient.utils.module.ConfigurableModule;
import com.amberclient.utils.module.Module;
import com.amberclient.utils.module.ModuleSettings;
import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Velocity extends Module implements ConfigurableModule, PreVelocityListener {
    private final ModuleSettings horizontalScale =
            new ModuleSettings("Horizontal Scale", "X/Z velocity scale", 0.6, 0.0, 5.0, 0.05);
    private final ModuleSettings verticalScale =
            new ModuleSettings("Vertical Scale", "Y velocity scale", 0.4, 0.0, 5.0, 0.05);
    private final ModuleSettings cancelAir =
            new ModuleSettings("Cancel Air", "Cancel velocity when in air", false);
    private final ModuleSettings chance =
            new ModuleSettings("Chance", "Chance to apply velocity reduction", 100.0, 0.0, 100.0, 5.0);

    private final List<ModuleSettings> settings = new ArrayList<>();
    private final MinecraftClient client = MinecraftClient.getInstance();
    private static Velocity instance;

    public Velocity() {
        super("Velocity", "Reduces knockback with anti-rollback", "Combat");
        settings.add(horizontalScale);
        settings.add(verticalScale);
        settings.add(cancelAir);
        settings.add(chance);
        instance = this;
    }

    public static Velocity getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        EventManager.getInstance().add(PreVelocityListener.class, this);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        EventManager.getInstance().remove(PreVelocityListener.class, this);
    }

    @Override
    public void onPreVelocity(@NotNull PreVelocityEvent event) {
        assert client.player != null;
        if (cancelAir.getBooleanValue() && !client.player.isOnGround()) {
            event.setCanceled(true);
            return;
        }

        // Get packet velocity (already scaled to blocks per tick in VelocityMixin)
        double motionX = event.getMotionX();
        double motionY = event.getMotionY();
        double motionZ = event.getMotionZ();

        // Apply velocity reduction based on chance
        if (chance.getDoubleValue() == 100.0 || Math.random() * 100 <= chance.getDoubleValue()) {
            motionX *= horizontalScale.getDoubleValue();
            motionY *= verticalScale.getDoubleValue();
            motionZ *= horizontalScale.getDoubleValue();
        }

        // Update the event with modified velocity
        event.setMotionX(motionX);
        event.setMotionY(motionY);
        event.setMotionZ(motionZ);
    }

    @Override
    public List<ModuleSettings> getSettings() {
        return settings;
    }
}