package com.amberclient.commands;

import com.amberclient.commands.impl.DummyCmd;
import com.amberclient.commands.impl.TopCmd;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class AmberCommand {
    public static void register() {

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                    CommandManager.literal("amber")
                            .then(CommandManager.literal("top")
                                    .executes(context -> {
                                        ServerCommandSource source = context.getSource();
                                        return TopCmd.teleportToTop(source);
                                    })
                            )
                            .then(CommandManager.literal("dummy")
                                    .executes(context -> {
                                        ServerCommandSource source = context.getSource();
                                        return DummyCmd.spawnDummy(source);
                                    })
                            )
            );
        });
    }
}