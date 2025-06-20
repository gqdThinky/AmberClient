package com.amberclient.modules.minigames;

import com.amberclient.utils.module.ModuleSettings;
import com.amberclient.utils.murdererfinder.MurdererFinder;
import com.amberclient.utils.module.ConfigurableModule;
import com.amberclient.utils.module.Module;
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

    private final List<ModuleSettings> settings;
    private final ModuleSettings highlightMurders;
    private final ModuleSettings highlightGold;
    private final ModuleSettings highlightBows;
    private final ModuleSettings showNameTags;
    private final ModuleSettings highlightSpectators;

    public MMFinder() {
        super("Murderer Finder", "Find the murderer in Hypixel's Murder Mystery", "Mini-games");

        // Initialize settings with values from Config
        Config config = ConfigManager.getConfig();
        highlightMurders = new ModuleSettings("Highlight Murderers", "Highlight players identified as murderers", config.mm.shouldHighlightMurders());
        highlightGold = new ModuleSettings("Highlight Gold", "Highlight gold items in the game", config.mm.shouldHighlightGold());
        highlightBows = new ModuleSettings("Highlight Bows", "Highlight bows in the game", config.mm.shouldHighlightBows());
        showNameTags = new ModuleSettings("Show Name Tags", "Display name tags for players", config.mm.shouldShowNameTags());
        highlightSpectators = new ModuleSettings("Highlight Spectators", "Highlight spectator players", config.mm.shouldHighlightSpectators());

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
        try {
            MurdererFinder.setModEnabled(true);
            System.out.println("MurdererFinder mod has been activated!");
        } catch (Exception e) {
            System.err.println("Failed to activate MurdererFinder mod: " + e.getMessage());
        }
    }

    @Override
    public void onDisable() {
        try {
            MurdererFinder.setModEnabled(false);
            System.out.println("MurdererFinder mod has been deactivated!");
        } catch (Exception e) {
            System.err.println("Failed to deactivate MurdererFinder mod: " + e.getMessage());
        }
    }

    @Override
    public List<ModuleSettings> getSettings() {
        return settings;
    }

    @Override
    public void onSettingChanged(ModuleSettings setting) {
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