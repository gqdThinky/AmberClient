package com.amberclient.modules.hacks;

import com.amberclient.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class Fullbright extends Module {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private double originalGamma;
    private boolean wasEnabled = false;

    public Fullbright() {
        super("Fullbright", "Increases brightness to maximum level", "Render");
    }

    @Override
    protected void onEnable() {
        if (mc.options != null) {
            // Sauvegarder la valeur gamma originale
            originalGamma = mc.options.getGamma().getValue();

            // Définir le gamma au maximum (16.0 comme dans gamma-utils)
            mc.options.getGamma().setValue(16.0);

            wasEnabled = true;
        }

        if (mc.player != null) {
            mc.player.sendMessage(
                    Text.literal("§4[§cAmberClient§4] §c§l" + getName() + " §r§cactivated").formatted(Formatting.RED), true);
        }
    }

    @Override
    protected void onDisable() {
        if (mc.options != null && wasEnabled) {
            // Restaurer la valeur gamma originale
            mc.options.getGamma().setValue(originalGamma);
            wasEnabled = false;
        }

        if (mc.player != null) {
            mc.player.sendMessage(
                    Text.literal("§4[§cAmberClient§4] §c§l" + getName() + " §r§cdeactivated").formatted(Formatting.RED), true);
        }
    }
}