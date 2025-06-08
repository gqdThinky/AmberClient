package com.amberclient.modules.minigames;

import com.amberclient.utils.murdererfinder.MurdererFinder;
import com.amberclient.utils.module.ConfigurableModule;
import com.amberclient.utils.module.Module;
import com.amberclient.utils.module.ModuleSetting;
import com.amberclient.utils.murdererfinder.config.Config;
import com.amberclient.utils.murdererfinder.config.ConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/*
      Mod originally created by https://github.com/thatDudo for 1.18.1
      Remastered in 1.21.4 and improved by https://github.com/gqdThinky
      All credits go to thatDudo.
 */

public class MMFinder extends Module implements ConfigurableModule {
    public static final String MOD_ID = "amberclient-murderfinder";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    private static final MMFinder INSTANCE = new MMFinder();

    private final List<ModuleSetting> settings;
    private final ModuleSetting highlightMurders;
    private final ModuleSetting highlightGold;
    private final ModuleSetting highlightBows;
    private final ModuleSetting showNameTags;
    private final ModuleSetting highlightSpectators;

    public MMFinder() {
        super("Murderer Finder", "Find the murderer in Hypixel's Murder Mystery", "Mini-games");

        // Initialize settings with values from Config
        Config config = ConfigManager.getConfig();
        highlightMurders = new ModuleSetting("Highlight Murderers", "Highlight players identified as murderers", config.mm.shouldHighlightMurders());
        highlightGold = new ModuleSetting("Highlight Gold", "Highlight gold items in the game", config.mm.shouldHighlightGold());
        highlightBows = new ModuleSetting("Highlight Bows", "Highlight bows in the game", config.mm.shouldHighlightBows());
        showNameTags = new ModuleSetting("Show Name Tags", "Display name tags for players", config.mm.shouldShowNameTags());
        highlightSpectators = new ModuleSetting("Highlight Spectators", "Highlight spectator players", config.mm.shouldHighlightSpectators());

        settings = new ArrayList<>();
        settings.add(highlightMurders);
        settings.add(highlightGold);
        settings.add(highlightBows);
        settings.add(showNameTags);
        settings.add(highlightSpectators);
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

    @Override
    public List<ModuleSetting> getSettings() {
        return settings;
    }

    @Override
    public void onSettingChanged(ModuleSetting setting) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        Config config = ConfigManager.getConfig();
        String status;

        if (setting == highlightMurders) {
            status = highlightMurders.getBooleanValue() ? "enabled" : "disabled";
            config.mm.highlightMurders = highlightMurders.getBooleanValue();
            client.player.sendMessage(
                    Text.literal("§6MMFinder settings updated: Highlight Murderers=" + status),
                    true);
            LOGGER.info("MMFinder settings updated: Highlight Murderers={}", status);
        } else if (setting == highlightGold) {
            status = highlightGold.getBooleanValue() ? "enabled" : "disabled";
            config.mm.highlightGold = highlightGold.getBooleanValue();
            client.player.sendMessage(
                    Text.literal("§6MMFinder settings updated: Highlight Gold=" + status),
                    true);
            LOGGER.info("MMFinder settings updated: Highlight Gold={}", status);
        } else if (setting == highlightBows) {
            status = highlightBows.getBooleanValue() ? "enabled" : "disabled";
            config.mm.highlightBows = highlightBows.getBooleanValue();
            client.player.sendMessage(
                    Text.literal("§6MMFinder settings updated: Highlight Bows=" + status),
                    true);
            LOGGER.info("MMFinder settings updated: Highlight Bows={}", status);
        } else if (setting == showNameTags) {
            status = showNameTags.getBooleanValue() ? "enabled" : "disabled";
            config.mm.showNameTags = showNameTags.getBooleanValue();
            client.player.sendMessage(
                    Text.literal("§6MMFinder settings updated: Show Name Tags=" + status),
                    true);
            LOGGER.info("MMFinder settings updated: Show Name Tags={}", status);
        } else if (setting == highlightSpectators) {
            status = highlightSpectators.getBooleanValue() ? "enabled" : "disabled";
            config.mm.highlightSpectators = highlightSpectators.getBooleanValue();
            client.player.sendMessage(
                    Text.literal("§6MMFinder settings updated: Highlight Spectators=" + status),
                    true);
            LOGGER.info("MMFinder settings updated: Highlight Spectators={}", status);
        }

        // Save the updated configuration
        ConfigManager.writeConfig();
    }
}