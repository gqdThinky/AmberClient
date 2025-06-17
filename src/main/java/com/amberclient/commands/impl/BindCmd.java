package com.amberclient.commands.impl;

import com.amberclient.utils.module.Module;
import com.amberclient.utils.module.ModuleManager;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BindCmd {
    public static final String MOD_ID = "amberclient-bindcmd";
    private static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static int execute(CommandContext<ServerCommandSource> ctx) {
        try {
            ServerCommandSource source = ctx.getSource();
            if (!source.isExecutedByPlayer()) {
                source.sendError(Text.literal("This command can only be executed by a player."));
                return 0;
            }

            String moduleName = ctx.getArgument("module", String.class);
            String keyName = ctx.getArgument("key", String.class).toUpperCase();

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
            LOGGER.error("Error binding module: {}", e.getMessage(), e);
            ctx.getSource().sendError(Text.literal("Error binding module: " + e.getMessage()));
            return 0;
        }
    }
}