package com.amberclient.utils;

import com.amberclient.modules.Fullbright;
import com.amberclient.modules.NoFall;
import com.amberclient.modules.xray.Xray;

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