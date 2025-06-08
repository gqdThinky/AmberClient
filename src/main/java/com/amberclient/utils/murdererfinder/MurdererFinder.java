package com.amberclient.utils.murdererfinder;

import com.amberclient.utils.murdererfinder.config.Config;
import com.amberclient.utils.murdererfinder.config.ConfigManager;
import com.amberclient.utils.murdererfinder.MurdererFinder;

import net.fabricmc.api.ModInitializer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.UUID;

public class MurdererFinder {
	public static final Logger logger = LoggerFactory.getLogger("mm-finder");

	public static boolean onHypixel = false;
	public enum HypixelLobbies {
		None,
		MurderMystery,
		MurderMysteryLobby
	}
	public static HypixelLobbies lobby;
	public static boolean roundEnded = false;

	public static boolean isEnabled() {
		try {
			Config config = ConfigManager.getConfig();
			return config != null && config.enabled;
		} catch (Exception e) {
			logger.error("Error checking if mod is enabled: " + e.getMessage());
			return false;
		}
	}

	public static boolean isActive() {
		return isEnabled() && lobby == HypixelLobbies.MurderMystery;
	}

	public static boolean clientIsMurder = false;
	public static boolean clientIsDead = false;
	public static HashSet<UUID> markedMurders = new HashSet<>();
	public static HashSet<UUID> markedDetectives = new HashSet<>();

	public void showMarkedPlayers(MinecraftClient client) {
		if (!isActive()) return;

		String murderersList = "";
		String detectivesList = "";
		if (client.world != null)
			for (PlayerEntity player : client.world.getPlayers()) {
				UUID uuid = player.getGameProfile().getId();
				if (markedMurders.contains(uuid))
					murderersList += player.getGameProfile().getName() + " ";
				if (markedDetectives.contains(uuid))
					detectivesList += player.getGameProfile().getName() + " ";
			}
		if (murderersList.isEmpty()) murderersList = "None";
		if (detectivesList.isEmpty()) detectivesList = "None";

		MinecraftUtils.sendChatMessage("");
		MinecraftUtils.sendChatMessage("§4[AmberClient]");
		MinecraftUtils.sendChatMessage(Text.translatable("message.show_murderers").getString() + Formatting.RED + murderersList);
		MinecraftUtils.sendChatMessage(Text.translatable("message.show_detectives").getString() + Formatting.AQUA + detectivesList);
		MinecraftUtils.sendChatMessage("");
	}

	public static void setModEnabled(boolean state) {
		try {
			Config config = ConfigManager.getConfig();
			if (config == null) {
				logger.warn("Config is null, cannot set mod enabled state");
				return;
			}
			if (state != config.enabled) {
				config.enabled = state;
				ConfigManager.writeConfig();
			}
		} catch (Exception e) {
			logger.error("Error setting mod enabled state: " + e.getMessage());
		}
	}

	public static void setHighlightMurders(boolean state) {
		try {
			Config config = ConfigManager.getConfig();
			if (config == null) {
				logger.warn("Config is null, cannot set highlight murders");
				return;
			}
			if (config.mm.highlightMurders != state) {
				config.mm.highlightMurders = state;
				// remove marked murderers
				if (!state)
					markedMurders.clear();
				ConfigManager.writeConfig();
			}
		} catch (Exception e) {
			logger.error("Error setting highlight murders: " + e.getMessage());
		}
	}

	public static void setDetectiveHighlightOptions(Config.MurderMystery.DetectiveHighlightOptions state) {
		try {
			Config config = ConfigManager.getConfig();
			if (config == null) {
				logger.warn("Config is null, cannot set detective highlight options");
				return;
			}
			if (config.mm.detectiveHighlightOptions != state) {
				config.mm.detectiveHighlightOptions = state;
				if (!config.mm.shouldHighlightDetectives(clientIsMurder))
					markedDetectives.clear();
				ConfigManager.writeConfig();
			}
		} catch (Exception e) {
			logger.error("Error setting detective highlight options: " + e.getMessage());
		}
	}

	public static void setCurrentLobby(HypixelLobbies slobby) {
		resetLobby(lobby);
		lobby = slobby;
	}

	public static void resetLobby(HypixelLobbies lobby) {
		if (lobby == HypixelLobbies.MurderMystery) {
			roundEnded = false;
			clientIsMurder = false;
			clientIsDead = false;
			markedMurders.clear();
			markedDetectives.clear();
		}
	}
}
