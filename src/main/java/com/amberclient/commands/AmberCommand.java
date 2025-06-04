package com.amberclient.commands;

import com.amberclient.commands.impl.DummyCommand;
import com.amberclient.commands.impl.TopCommand;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class AmberCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                    CommandManager.literal("amber")
                            .then(CommandManager.argument("action", StringArgumentType.word())
                                    .suggests((context, builder) -> {
                                        builder.suggest("top");
                                        builder.suggest("dummy");
                                        return builder.buildFuture();
                                    })
                                    .executes(context -> {
                                        ServerCommandSource source = context.getSource();
                                        String action = StringArgumentType.getString(context, "action");
                                        if (action.equals("top")) {
                                            return TopCommand.teleportToTop(source);
                                        }
                                        else if (action.equals("dummy")) {
                                            return DummyCommand.spawnDummy(source);
                                        }
                                        source.sendError(Text.literal("Unknown action : " + action));
                                        return 0;
                                    }))
            );
        });
    }
}
