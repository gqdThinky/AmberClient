package com.amberclient.commands.impl;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class DataItemCmd {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                    CommandManager.literal("amber")
                            .then(CommandManager.literal("dataitem")
                                    .requires(source -> source.hasPermissionLevel(2)) // Requiert OP level 2
                                    .executes(context -> {
                                        ServerCommandSource source = context.getSource();
                                        // Exécute la commande /data get entity @p Inventory[0]
                                        source.getServer().getCommandManager().executeWithPrefix(
                                                source, "data get entity @p Inventory[0]"
                                        );
                                        return 1;
                                    })
                            )
            );
        });
    }
}