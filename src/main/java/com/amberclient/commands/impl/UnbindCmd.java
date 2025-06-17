package com.amberclient.commands.impl;

import com.amberclient.utils.module.Module;
import com.amberclient.utils.module.ModuleManager;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UnbindCmd {
    public static final String MOD_ID = "amberclient-unbindcmd";
    private static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static int execute(CommandContext<ServerCommandSource> ctx) {
        try {
            ServerCommandSource source = ctx.getSource();
            if (!source.isExecutedByPlayer()) {
                source.sendError(Text.literal("This command can only be executed by a player."));
                return 0;
            }

            source.getPlayerOrThrow();
            String moduleName = ctx.getArgument("module", String.class);

            ModuleManager moduleManager = ModuleManager.getInstance();
            Module module = moduleManager.findModuleByName(moduleName);

            if (module == null) {
                source.sendError(Text.literal("Module '" + moduleName + "' not found."));
                return 0;
            }

            String currentKey = moduleManager.getModuleKeyName(module);
            if (currentKey.equals("Not bound")) {
                source.sendFeedback(
                        () -> Text.literal("§4[§cAmberClient§4] §cModule §4'" + moduleName + "' §cis not bound to any key."),
                        false
                );
            } else {
                moduleManager.unbindModule(module);
                source.sendFeedback(
                        () -> Text.literal("§4[§cAmberClient§4] §cUnbound module §4'" + moduleName + "' §cfrom key §4'" + currentKey + "'."),
                        false
                );
            }
            return 1;

        } catch (Exception e) {
            LOGGER.error("Error unbinding module: {}", e.getMessage(), e);
            ctx.getSource().sendError(Text.literal("Error unbinding module: " + e.getMessage()));
            return 0;
        }
    }
}