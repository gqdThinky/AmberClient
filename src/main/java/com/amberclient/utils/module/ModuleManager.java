package com.amberclient.utils.module;

import com.amberclient.modules.combat.AutoClicker;
import com.amberclient.modules.combat.Hitbox;
import com.amberclient.modules.combat.KillAura;
import com.amberclient.modules.combat.Velocity;
import com.amberclient.modules.hud.ActiveMods;
import com.amberclient.modules.hud.Transparency;
import com.amberclient.modules.minigames.MMFinder;
import com.amberclient.modules.movement.AutoClutch;
import com.amberclient.modules.movement.NoFall;
import com.amberclient.modules.movement.SafeWalk;
import com.amberclient.modules.player.FastBreak;
import com.amberclient.modules.player.FastPlace;
import com.amberclient.modules.render.EntityESP;
import com.amberclient.modules.render.Fullbright;
import com.amberclient.modules.render.xray.Xray;
import com.amberclient.utils.keybinds.CustomKeybindManager;
import com.amberclient.utils.keybinds.KeybindsManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ModuleManager {
    private static final ModuleManager INSTANCE = new ModuleManager();
    private final List<Module> modules = new ArrayList<>();

    private ModuleManager() {
        KeybindsManager.INSTANCE.initialize();
        CustomKeybindManager.INSTANCE.initialize();

        // Register modules
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
        registerModule(new MMFinder());
        registerModule(new AutoClutch());
        registerModule(new Velocity());
    }

    public static ModuleManager getInstance() {
        return INSTANCE;
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
                        System.err.println("Erreur dans " + module.getName() + ": " + e.getMessage());
                    }
                });

        CustomKeybindManager.INSTANCE.tick();
    }

    public List<Module> getModules() {
        return Collections.unmodifiableList(modules);
    }

    public void toggleModule(Module module) {
        if (module != null) module.toggle();
    }

    public void registerModule(Module module) {
        if (module != null && !modules.contains(module)) {
            modules.add(module);
        }
    }

    public void handleKeyInputs() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        // Handle static keybindings (original system)
        for (Map.Entry<String, KeyBinding> entry : KeybindsManager.INSTANCE.getKeyBindings().entrySet()) {
            KeyBinding keyBinding = entry.getValue();

            while (keyBinding.wasPressed()) {
                Module module = modules.stream()
                        .filter(m -> m.getKeyBinding() == keyBinding)
                        .findFirst()
                        .orElse(null);
                if (module != null) {
                    module.toggle();
                }
            }
        }
    }

    public void bindKeyToModule(Module module, String keyName) {
        int keyCode = KeybindsManager.INSTANCE.getKeyCodeFromName(keyName);

        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_UNKNOWN) {
            System.err.println("Invalid key name: " + keyName);
            return;
        }

        unbindModule(module);

        String actionId = "module_" + module.getName().toLowerCase().replace(" ", "_");

        CustomKeybindManager.INSTANCE.bindKey(
                keyCode,
                actionId,
                "Toggle " + module.getName(),
                true, // Requires player
                new Runnable() {
                    @Override
                    public void run() {
                        module.toggle();
                        System.out.println("Toggled " + module.getName() + " via custom keybind");
                    }
                }
        );

        module.setCustomKeyCode(keyCode);

        System.out.println("Successfully bound " + CustomKeybindManager.INSTANCE.getKeyName(keyCode) +
                " to " + module.getName());
    }

    public void unbindModule(Module module) {
        int currentKeyCode = module.getCustomKeyCode();
        if (currentKeyCode != -1) {
            String actionId = "module_" + module.getName().toLowerCase().replace(" ", "_");
            CustomKeybindManager.INSTANCE.unbindAction(currentKeyCode, actionId);
            module.setCustomKeyCode(-1);
            System.out.println("Unbound " + module.getName() + " from key");
        }
    }

    public String getModuleKeyName(Module module) {
        int keyCode = module.getCustomKeyCode();
        if (keyCode == -1) {
            return "Not bound";
        }
        return CustomKeybindManager.INSTANCE.getKeyName(keyCode);
    }

    public void listAllKeybinds() {
        System.out.println("=== Custom Keybinds ===");
        Map<Integer, List<CustomKeybindManager.KeybindAction>> bindings = CustomKeybindManager.INSTANCE.getAllBindings();

        if (bindings.isEmpty()) {
            System.out.println("No custom keybinds registered.");
            return;
        }

        bindings.forEach((keyCode, actions) -> {
            String keyName = CustomKeybindManager.INSTANCE.getKeyName(keyCode);
            System.out.println(keyName + ":");
            actions.forEach(action -> {
                System.out.println("  - " + action.getDescription());
            });
        });
    }

    public Module findModuleByName(String name) {
        return modules.stream()
                .filter(m -> m.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
}