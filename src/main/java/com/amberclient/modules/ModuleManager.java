package com.amberclient.modules;

import com.amberclient.modules.hacks.NoFall;
import com.amberclient.modules.hacks.Xray;

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
        // Vérifier que le client et le joueur existent
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        for (Module module : modules) {
            if (module.isEnabled()) {
                try {
                    module.onTick();
                } catch (Exception e) {
                    // Log l'erreur pour éviter que le mod crash
                    System.err.println("Erreur dans " + module.getName() + ": " + e.getMessage());
                }
            }
        }
    }

    public void registerModule(Module module) {
        modules.add(module);
    }
}