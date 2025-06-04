package com.amberclient.commands.impl;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

import java.util.Objects;

public class TopCommand {

    public static int teleportToTop(ServerCommandSource source) {
        World world = source.getWorld();
        if (!world.isClient && source.getServer().isSingleplayer()) {
            BlockPos pos = Objects.requireNonNull(source.getPlayer()).getBlockPos();
            BlockPos topPos = world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, pos);
            source.getPlayer().requestTeleport(topPos.getX() + 0.5, topPos.getY(), topPos.getZ() + 0.5);
            source.getPlayer().sendMessage(
                    Text.literal("§4[§cAmberClient§4] §cTeleported to the highest point."),
                    true
            );

            return 1;
        } else {
            source.sendError(Text.literal("'amber' command can only be used solo."));
            return 0;
        }
    }
}
