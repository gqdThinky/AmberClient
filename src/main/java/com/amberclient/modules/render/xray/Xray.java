package com.amberclient.modules.render.xray;

import com.amberclient.utils.module.Module;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.ChunkPos;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class Xray extends Module {
    public static final String MOD_ID = "amberclient-xray";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    private ChunkPos lastPlayerChunk;


    public Xray() {
        super("X-Ray", "Shows the outlines of the selected ores in the chunk", "Render");
        // Register events
        WorldRenderEvents.AFTER_TRANSLUCENT.register(RenderOutlines::render);
        LOGGER.info("XRay module initialized");
    }

    @Override
    public void onEnable() {
        SettingsStore.getInstance().get().setActive(true);
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
}