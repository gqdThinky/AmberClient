package com.amberclient.modules.hacks;

import com.amberclient.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class Fullbright extends Module {
    public static final String MOD_ID = "amberclient-fullbright";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public Fullbright() {
        super("Fullbright", "Allows you to see in the dark", "Render");

        LOGGER.info("Fullbright module initialized");
    }

    @Override
    public void onEnable() {
        MinecraftClient.getInstance().player.sendMessage(
                Text.literal("§4[§cAmberClient§4] §c§l" + getName() + " §r§cactivated").formatted(Formatting.RED), true);
    }

    @Override
    public void onDisable() {
        MinecraftClient.getInstance().player.sendMessage(
                Text.literal("§4[§cAmberClient§4] §c§l" + getName() + " §r§cdeactivated").formatted(Formatting.RED), true);
    }
}
