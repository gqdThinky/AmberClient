package com.amberclient;

import com.amberclient.gui.HudRenderer;
import com.amberclient.gui.TabGUI;
import com.amberclient.modules.ModuleManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AmberClient implements ModInitializer {
	public static final String MOD_ID = "amber-client";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static boolean lastKeyPressed = false;

	@Override
	public void onInitialize() {
		// Register client-side tick event
		ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);

		// Register HUD renderer
		HudRenderCallback.EVENT.register(new HudRenderer());

		LOGGER.info("Amber Client started!");
	}

	private void onClientTick(MinecraftClient client) {
		// Check if right shift key is pressed
		long windowHandle = client.getWindow().getHandle();
		boolean keyPressed = InputUtil.isKeyPressed(windowHandle, GLFW.GLFW_KEY_RIGHT_SHIFT);

		// Toggle TabGUI on key press (not hold)
		if (keyPressed && !lastKeyPressed) {
			if (client.currentScreen instanceof TabGUI) {
				client.setScreen(null);
			} else {
				client.setScreen(new TabGUI());
			}
		}

		lastKeyPressed = keyPressed;

		// Call ModuleManager's tick
		ModuleManager.getInstance().onTick();
	}
}