package com.amberclient.modules.minigames;

import com.amberclient.utils.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/*
     NBT tags for Murder Mystery's items (found by myself 🤓):
     DetectiveBow:1b (for the detective's bow)
     KNIFE:1b (for the murderer's weapon)
 */

public class MurdererFinder extends Module {
    public static final String MOD_ID = "amberclient-murderfinder";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static Boolean isMurdererFound;

    public MurdererFinder() {
        super("Murderer Finder", "Find the murderer in Hypixel's Murder Mystery", "Mini-games");
    }

    @Override
    public void onEnable() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(
                    Text.literal("§4[§cAmberClient§4] §c§l" + getName() + " §r§cactivated"), true);
        }
        isMurdererFound = false;
        LOGGER.info(getName() + " module enabled");
    }

    @Override
    public void onDisable() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(
                    Text.literal("§4[§cAmberClient§4] §c§l" + getName() + " §r§cdeactivated"), true);
        }
        LOGGER.info(getName() + " module disabled");
    }

    @Override
    public void onTick() {
        // find the murderer here
    }
}