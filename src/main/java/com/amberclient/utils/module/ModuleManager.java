package com.amberclient.utils.module;

import com.amberclient.modules.combat.AutoClicker;
import com.amberclient.modules.combat.Hitbox;
import com.amberclient.modules.combat.KillAura;
import com.amberclient.modules.hud.ActiveMods;
import com.amberclient.modules.hud.Transparency;
import com.amberclient.modules.minigames.MurdererFinder;
import com.amberclient.modules.movement.NoFall;
import com.amberclient.modules.movement.SafeWalk;
import com.amberclient.modules.player.FastBreak;
import com.amberclient.modules.player.FastPlace;
import com.amberclient.modules.render.EntityESP;
import com.amberclient.modules.render.Fullbright;
import com.amberclient.modules.render.xray.Xray;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ModuleManager {
    private static final ModuleManager INSTANCE = new ModuleManager();
    private final List<Module> modules = new ArrayList<>();

    private ModuleManager() {
        registerModule(new AutoClicker());
        registerModule(new Hitbox());
        registerModule(new KillAura());
        registerModule(new ActiveMods());
        registerModule(new Transparency());
        registerModule(new NoFall());
        registerModule(new SafeWalk());
        registerModule(new FastBreak());
        registerModule(new FastPlace());
        registerModule(new Xray());
        registerModule(new EntityESP());
        registerModule(new Fullbright());
        registerModule(new MurdererFinder());
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
        if (module != null && !modules.contains(module)) {
            modules.add(module);
        }
    }
}