package com.amberclient.utils.module;

import net.minecraft.client.MinecraftClient;
import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ModuleManager {
    private static final ModuleManager INSTANCE = new ModuleManager();
    private final List<Module> modules = new ArrayList<>();

    private ModuleManager() {
        Reflections reflections = new Reflections("com.amberclient.modules");

        // Find all Module subclasses
        Set<Class<? extends Module>> moduleClasses = reflections.getSubTypesOf(Module.class);

        for (Class<? extends Module> moduleClass : moduleClasses) {
            try {
                registerModule(moduleClass.getDeclaredConstructor().newInstance());
            } catch (Exception e) {
                System.err.println("Error during module instantiation " + moduleClass.getSimpleName() + ": " + e.getMessage());
            }
        }
    }

    public static ModuleManager getInstance() {
        return INSTANCE;
    }

    public List<Module> getModules() {
        return Collections.unmodifiableList(modules);
    }

    public static Optional<Module> getModule(Class<? extends Module> moduleClass) {
        return INSTANCE.modules.stream()
                .filter(module -> module.getClass() == moduleClass)
                .findFirst();
    }

    public void toggleModule(Module module) {
        if (module != null) {
            module.toggle();
        }
    }

    public void onTick() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        modules.stream()
                .filter(Module::isEnabled)
                .forEach(module -> {
                    try {
                        module.onTick();
                    } catch (Exception e) {
                        System.err.println("Error in " + module.getName() + ": " + e.getMessage());
                    }
                });
    }

    public void registerModule(Module module) {
        modules.add(module);
    }
}
