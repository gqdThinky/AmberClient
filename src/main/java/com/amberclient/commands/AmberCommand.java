package com.amberclient.commands;

import com.amberclient.commands.impl.DummyCmd;
import com.amberclient.commands.impl.TopCmd;
import com.mojang.brigadier.arguments.IntegerArgumentType;
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
                                            return TopCmd.teleportToTop(source);
                                        }
                                        else if (action.equals("dummy")) {
                                            return DummyCmd.spawnDummy(source);
                                        }
                                        source.sendError(Text.literal("Unknown action: " + action));
                                        return 0;
                                    })
                            )
                            .then(CommandManager.literal("data-item")
                                    .then(CommandManager.argument("slot", IntegerArgumentType.integer(0, 8))
                                            .executes(context -> {
                                                ServerCommandSource source = context.getSource();
                                                int slot = IntegerArgumentType.getInteger(context, "slot");
                                                // Exécute la commande /data get entity @p Inventory[slot]
                                                source.getServer().getCommandManager().executeWithPrefix(
                                                        source, "data get entity @p Inventory[" + slot + "]"
                                                );
                                                return 1;
                                            })
                                    )
                            )
            );
        });
    }
}
