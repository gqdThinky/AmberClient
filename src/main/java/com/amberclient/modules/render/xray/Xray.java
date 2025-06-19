package com.amberclient.modules.render.xray;

import com.amberclient.utils.module.Module;
import com.amberclient.utils.module.ConfigurableModule;
import com.amberclient.utils.module.ModuleSettings;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.ChunkPos;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.List;
import java.util.ArrayList;

public class Xray extends Module implements ConfigurableModule {
    public static final String MOD_ID = "amberclient-xray";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    private ChunkPos lastPlayerChunk;

    // Settings
    private final ModuleSettings exposedOnly;
    private final List<ModuleSettings> settings;

    public Xray() {
        super("X-Ray", "Shows the outlines of the selected ores in the chunk", "Render");

        // Initialize settings
        exposedOnly = new ModuleSettings("Exposed Only", "Show only ores exposed to air/surface", false);

        settings = new ArrayList<>();
        settings.add(exposedOnly);

        // Register events
        WorldRenderEvents.AFTER_TRANSLUCENT.register(RenderOutlines::render);
        LOGGER.info("XRay module initialized");
    }

    @Override
    public void onEnable() {
        SettingsStore.getInstance().get().setActive(true);
        // Update the exposed only setting in the settings store
        SettingsStore.getInstance().get().setExposedOnly(exposedOnly.getBooleanValue());
        // Perform a full scan of the area on activation
        ScanTask.runTask(true);
    }

    @Override
    public void onDisable() {
        SettingsStore.getInstance().get().setActive(false);
        ScanTask.renderQueue.clear();
    }

    @Override
    public void onTick() {
        if (SettingsStore.getInstance().get().isActive()) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null) return;

            ChunkPos currentChunk = client.player.getChunkPos();

            // Check if the player has changed chunk
            if (!currentChunk.equals(lastPlayerChunk)) {
                ScanTask.runTaskForSingleChunk(currentChunk);
                lastPlayerChunk = currentChunk;
            }
        }
    }

    @Override
    public List<ModuleSettings> getSettings() {
        return settings;
    }

    @Override
    public void onSettingChanged(ModuleSettings setting) {
        if (setting == exposedOnly) {
            // Update the settings store with the new value
            SettingsStore.getInstance().get().setExposedOnly(exposedOnly.getBooleanValue());

            // Send feedback to player
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                client.player.sendMessage(
                        Text.literal("§6Exposed Only: §l" + (exposedOnly.getBooleanValue() ? "ON" : "OFF")),
                        true
                );
            }

            // Trigger a full rescan when the setting changes
            if (SettingsStore.getInstance().get().isActive()) {
                ScanTask.runTask(true);
            }
        }
    }

    // Getter method for other classes to check if exposed only is enabled
    public boolean isExposedOnly() {
        return exposedOnly.getBooleanValue();
    }
}