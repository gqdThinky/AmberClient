package com.amberclient.utils.general;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Utility class for block operations in AmberClient.
 */
public class BlockUtils {

    /**
     * Checks whether the block at the given position is unbreakable.
     * <p>
     * Unbreakable blocks, such as bedrock, have a hardness of less than 0.
     * This method returns true if the block is unbreakable, false otherwise.
     *
     * @param pos The position of the block to be checked.
     * @return true if the block is unbreakable, false otherwise.
     */
    public static boolean isUnbreakable(BlockPos pos) {
        World world = MinecraftClient.getInstance().world;

        if (world == null) { return false; }

        // Récupérer l'état du bloc et vérifier sa dureté
        return world.getBlockState(pos).getHardness(world, pos) < 0;
    }
}