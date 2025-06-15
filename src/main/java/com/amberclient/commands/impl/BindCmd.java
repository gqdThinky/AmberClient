package com.amberclient.commands.impl;

import com.amberclient.utils.module.Module;
import com.amberclient.utils.module.ModuleManager;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class BindCmd {
    public static int execute(CommandContext<ServerCommandSource> context) {
        try {
            ServerCommandSource source = context.getSource();
            if (!source.isExecutedByPlayer()) {
                source.sendError(Text.literal("This command can only be executed by a player."));
                return 0;
            }

            ServerPlayerEntity player = source.getPlayerOrThrow();
            String moduleName = context.getArgument("module", String.class);
            String keyName = context.getArgument("key", String.class).toUpperCase();

            ModuleManager moduleManager = ModuleManager.getInstance();

            Module module = moduleManager.getModules()
                    .stream()
                    .filter(m -> m.getName().equalsIgnoreCase(moduleName))
                    .findFirst()
                    .orElse(null);

            if (module == null) {
                source.sendError(Text.literal("Module '" + moduleName + "' not found."));
                return 0;
            }

            moduleManager.bindKeyToModule(module, keyName);

            source.sendFeedback(
                    () -> Text.literal("§4[§cAmberClient§4] §cKey §4'" + keyName + "' §cbound to module §4'" + moduleName + "'."),
                    false
            );
            return 1;

        } catch (Exception e) {
            System.err.println("Error in BindCmd: " + e.getMessage());
            e.printStackTrace();

            context.getSource().sendError(Text.literal("Error binding key: " + e.getMessage()));
            return 0;
        }
    }
}