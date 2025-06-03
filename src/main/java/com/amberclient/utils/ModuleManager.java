package com.amberclient.utils;

import com.amberclient.modules.combat.AutoClicker;
import com.amberclient.modules.combat.Hitbox;
import com.amberclient.modules.combat.KillAura;
import com.amberclient.modules.misc.ActiveMods;
import com.amberclient.modules.movement.NoFall;
import com.amberclient.modules.movement.SafeWalk;
import com.amberclient.modules.player.FastBreak;
import com.amberclient.modules.player.FastPlace;
import com.amberclient.modules.render.Fullbright;
import com.amberclient.modules.render.xray.Xray;

import net.minecraft.client.MinecraftClient;
import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    private static final ModuleManager INSTANCE = new ModuleManager();
    public final List<Module> modules = new ArrayList<>();

    private ModuleManager() {
        // Register modules
        registerModule(new NoFall());
        registerModule(new Xray());
        registerModule(new Fullbright());
        registerModule(new ActiveMods());
        registerModule(new Hitbox());
        registerModule(new KillAura());
        registerModule(new AutoClicker());
        registerModule(new FastPlace());
        registerModule(new FastBreak());
        registerModule(new SafeWalk());
    }

    public static ModuleManager getInstance() {
        return INSTANCE;
    }

    public List<Module> getModules() {
        return new ArrayList<>(modules);
    }

    public void toggleModule(Module module) {
        if (module != null) {
            module.toggle();
        }
    }

    public void onTick() {
        // Check that the client and player exist
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        for (Module module : modules) {
            if (module.isEnabled()) {
                try {
                    module.onTick();
                } catch (Exception e) {
                    System.err.println("Error in " + module.getName() + ": " + e.getMessage());
                }
            }
        }
    }

    public void registerModule(Module module) {
        modules.add(module);
    }
}
