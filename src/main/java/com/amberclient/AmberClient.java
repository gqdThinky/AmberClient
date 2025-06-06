package com.amberclient;

import com.amberclient.screens.HudRenderer;
import com.amberclient.screens.ClickGUI;
import com.amberclient.commands.AmberCommand;
import com.amberclient.utils.module.ModuleManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AmberClient implements ModInitializer {
	public static final String MOD_ID = "amberclient";
	public static final String MOD_VERSION = "v0.5";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static boolean lastKeyPressed = false;
	private static boolean hudLayerRegistered = false; // Flag to prevent duplicate registration

	@Override
	public void onInitialize() {
		ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);

		// Register HUD layer only if not already registered
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
			hudLayerRegistered = true; // Mark as registered
		}

		AmberCommand.register();

		LOGGER.info("Amber Client started! Version: " + MOD_VERSION);
	}

	private void onClientTick(MinecraftClient client) {
		long windowHandle = client.getWindow().getHandle();
		boolean keyPressed = InputUtil.isKeyPressed(windowHandle, GLFW.GLFW_KEY_RIGHT_SHIFT);

		if (keyPressed && !lastKeyPressed) {
			if (client.currentScreen instanceof ClickGUI) {
				client.setScreen(null);
			} else {
				client.setScreen(new ClickGUI());
			}
		}
		lastKeyPressed = keyPressed;

		ModuleManager.getInstance().onTick();
	}
}