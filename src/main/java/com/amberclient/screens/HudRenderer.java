package com.amberclient.screens;

import com.amberclient.utils.Module;
import com.amberclient.utils.ModuleManager;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

import java.awt.Color;
import java.util.List;

public class HudRenderer implements HudRenderCallback {
    private static final int BACKGROUND_COLOR = new Color(20, 20, 25, 200).getRGB();
    private static final int TEXT_COLOR = new Color(220, 220, 220).getRGB();
    private static final int ACCENT_COLOR = new Color(255, 165, 0).getRGB();

    @Override
    public void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || client.options.hudHidden) {
            return;
        }

        // Get enabled modules
        List<Module> enabledModules = ModuleManager.getInstance().getModules()
                .stream()
                .filter(Module::isEnabled)
                .toList();

        if (enabledModules.isEmpty()) {
            return;
        }

        // Calculate dimensions
        int padding = 5;
        int spacing = 2;
        int textHeight = client.textRenderer.fontHeight;
        int moduleHeight = textHeight + spacing;
        int width = enabledModules.stream()
                .map(module -> client.textRenderer.getWidth(module.getName()))
                .max(Integer::compare)
                .orElse(100) + padding * 2;
        int height = enabledModules.size() * moduleHeight + padding * 2 - spacing;

        // Position in top-left corner
        int x = 5;
        int y = 5;

        // Draw background
        context.fill(x, y, x + width, y + height, BACKGROUND_COLOR);

        // Draw accent border (top edge)
        context.fill(x, y, x + width, y + 1, ACCENT_COLOR);

        // Draw module names
        for (int i = 0; i < enabledModules.size(); i++) {
            Module module = enabledModules.get(i);
            String name = module.getName();
            int textX = x + padding;
            int textY = y + padding + i * moduleHeight;
            context.drawTextWithShadow(client.textRenderer, name, textX, textY, TEXT_COLOR);
        }
    }
}
