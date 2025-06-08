package com.amberclient.modules.minigames;

import com.amberclient.utils.murdererfinder.MurdererFinder;

import com.amberclient.utils.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/*
      Mod originally created by https://github.com/thatDudo for 1.18.1
      Remastered in 1.21.4 and improved by https://github.com/gqdThinky
      All credits go to thatDudo.
 */

public class MMFinder extends Module {
    public static final String MOD_ID = "amberclient-murderfinder";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    private static final MMFinder INSTANCE = new MMFinder();

    public MMFinder() {
        super("Murderer Finder", "Find the murderer in Hypixel's Murder Mystery", "Mini-games");
    }

    public static MMFinder getInstance() {
        return INSTANCE;
    }

    @Override
    public void onEnable() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(
                    Text.literal("§4[§cAmberClient§4] §c§l" + getName() + " §r§cactivated"), true);
        }

        try {
            MurdererFinder.setModEnabled(true);
            System.out.println("MurdererFinder mod has been activated!");
        } catch (Exception e) {
            System.err.println("Failed to activate MurdererFinder mod: " + e.getMessage());
        }
        LOGGER.info(getName() + " module enabled");
    }

    @Override
    public void onDisable() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(
                    Text.literal("§4[§cAmberClient§4] §c§l" + getName() + " §r§cdeactivated"), true);
        }

        MurdererFinder.setModEnabled(false);
        LOGGER.info(getName() + " module disabled");
    }
}