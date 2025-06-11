package com.amberclient.utils.module;

import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

public class ModuleManager {
    private static final ModuleManager INSTANCE = new ModuleManager();
    private final List<Module> modules = new ArrayList<>();

    private ModuleManager() {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage("com.amberclient.modules"))
                .setScanners(Scanners.SubTypes));

        Set<Class<? extends Module>> moduleClasses = reflections.getSubTypesOf(Module.class);

        for (Class<? extends Module> moduleClass : moduleClasses) {
            try {
                Module module = moduleClass.getDeclaredConstructor().newInstance();
                registerModule(module);
            } catch (Exception e) {
                System.err.println("Error during module instantiation of " + moduleClass.getSimpleName() + ": " + e.getMessage());
            }
        }
    }

    public static ModuleManager getInstance() { return INSTANCE; }

    public void onTick() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        modules.stream()
                .filter(Module::isEnabled)
                .forEach(module -> {
                    try {
                        module.onTick();
                    } catch (Exception e) {
                        System.err.println("Erreur dans " + module.getName() + ": " + e.getMessage());
                    }
                });
    }

    public List<Module> getModules() { return Collections.unmodifiableList(modules); }

    public void toggleModule(Module module) { if (module != null) module.toggle(); }

    public void registerModule(Module module) { if (module != null && !modules.contains(module)) { modules.add(module); } }

    public void handleKeyInputs() { for (Module module : modules) { module.handleKeyInput(); } }
}