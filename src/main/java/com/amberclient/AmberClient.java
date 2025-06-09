package com.amberclient;

import com.amberclient.screens.HudRenderer;
import com.amberclient.screens.ClickGUI;
import com.amberclient.commands.AmberCommand;
import com.amberclient.utils.KeybindsManager;
import com.amberclient.utils.module.ModuleManager;
import com.amberclient.utils.murdererfinder.config.ConfigManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AmberClient implements ModInitializer {
	public static final String MOD_ID = "amberclient";
	public static final String MOD_VERSION = "v0.5.2";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static boolean hudLayerRegistered = false;

	@Override
	public void onInitialize() {
		ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);

		if (!hudLayerRegistered) {
			HudLayerRegistrationCallback.EVENT.register(layeredDrawer -> {
				layeredDrawer.attachLayerAfter(
						IdentifiedLayer.EXPERIENCE_LEVEL,
						Identifier.of(MOD_ID, "hud_overlay"),
						(context, tickCounter) -> {
							HudRenderer hudRenderer = new HudRenderer();
							hudRenderer.onHudRender(context, tickCounter);
						}
				);
			});
			hudLayerRegistered = true;
		}

		AmberCommand.register();

		KeybindsManager.INSTANCE.initialize();
		ConfigManager.init();

		LOGGER.info("Amber Client started! Version: " + MOD_VERSION);
	}

	private void onClientTick(MinecraftClient client) {
		if (KeybindsManager.INSTANCE.getOpenClickGui().wasPressed() && client.currentScreen == null) {
			client.setScreen(new ClickGUI());
		}

		ModuleManager.getInstance().onTick();
	}
}