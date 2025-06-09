package com.amberclient.utils.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public abstract class Module {
    private final String name;
    private final String description;
    private final String category;
    protected boolean enabled;

    public Module(String name, String description, String category) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.enabled = false;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void toggle() {
        if (enabled) {
            disable();
        } else {
            enable();
        }
    }

    protected void enable() {
        enabled = true;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            mc.player.sendMessage(Text.literal("§4[§cAmberClient§4] §c§l" + getName() + " §r§cmodule enabled"), true);
        }

        onEnable();
    }

    protected void disable() {
        enabled = false;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            mc.player.sendMessage(Text.literal("§4[§cAmberClient§4] §c§l" + getName() + " §r§cmodule disabled"), true);
        }

        onDisable();
    }

    // Called when the module is enabled
    protected void onEnable() {
        // Override in subclasses to implement enable logic
    }

    // Called when the module is disabled
    protected void onDisable() {
        // Override in subclasses to implement disable logic
    }

    // Called every client tick when the module is enabled
    public void onTick() {
        // Override in subclasses to implement per-tick logic
    }

    // Called every client tick to handle key inputs (even if module is disabled)
    public void handleKeyInput() {
        // Override in subclasses if needed
    }

    protected MinecraftClient getClient() {
        return MinecraftClient.getInstance();
    }
}