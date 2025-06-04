package com.amberclient.commands.impl;

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
                        "summon minecraft:zombie ~ ~ ~ {CustomName:'[\"\",{\"text\":\"Zombie Dummy \",\"color\":\"red\"},{\"text\":\"ll\",\"obfuscated\":true,\"color\":\"dark_red\"}]',CustomNameVisible:1,NoAI:1b,PersistenceRequired:1b,Glowing:1,Health:99999,Attributes:[{Name:\"generic.maxHealth\",Base:999}],HandItems:[{id:\"minecraft:red_mushroom\",Count:1},{}],ArmorItems:[{},{},{},{id:\"minecraft:diamond_helmet\",Count:1,tag:{Enchantments:[{id:unbreaking,lvl:3}]}}]}"
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
