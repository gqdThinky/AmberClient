package com.amberclient.modules;

import com.amberclient.utils.Module;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ActiveMods extends Module {
    public ActiveMods() {
        super("Active mods", "Toggles display of active modules", "Miscellaneous");
        this.enabled = true;
    }

    @Override
    protected void onEnable() {
        getClient().player.sendMessage(
                Text.literal("§4[§cAmberClient§4] §c§l" + getName() + " §r§cenabled").formatted(Formatting.RED), true);
    }

    @Override
    protected void onDisable() {
        getClient().player.sendMessage(
                Text.literal("§4[§cAmberClient§4] §c§l" + getName() + " §r§cdisabled").formatted(Formatting.RED), true);
    }
}
