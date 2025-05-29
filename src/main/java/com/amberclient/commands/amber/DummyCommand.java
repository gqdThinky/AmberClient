package com.amberclient.commands.amber;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.world.World;

public class DummyCommand {

    public static int spawnDummy(ServerCommandSource source) {
        World world = source.getWorld();
        if (!world.isClient && source.getServer().isSingleplayer()) {
            try {
                source.getServer().getCommandManager().executeWithPrefix(
                        source,
                        "summon zombie ~ ~ ~ {CustomName:'[{\"text\":\"Zombie Dummy \",\"color\":\"red\"},{\"text\":\"ll\",\"color\":\"dark_red\",\"obfuscated\":true}]',CustomNameVisible:1b,Health:9999,Glowing:1b,NoAI:1b,OnGround:1b,PersistenceRequired:1b,HandItems:[{id:bedrock},{}],HandDropChances:[0f,0f],ArmorItems:[{},{},{},{id:golden_helmet}],attributes:[{id:max_health,base:9999f}]}"
                );

                source.getPlayer().sendMessage(
                        Text.literal("§4[§cAmberClient§4] §cDummy spawned successfully."),
                        true
                );

                return 1;
            } catch (Exception e) {
                source.sendError(Text.literal("Failed to spawn dummy: " + e.getMessage()));
                return 0;
            }
        } else {
            source.sendError(Text.literal("'amber' command can only be used solo."));
            return 0;
        }
    }
}
