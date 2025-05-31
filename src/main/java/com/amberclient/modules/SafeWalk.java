package com.amberclient.modules;

import com.amberclient.utils.ConfigurableModule;
import com.amberclient.utils.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SafeWalk extends Module {
    public static final String MOD_ID = "amberclient-safewalk";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    private static SafeWalk instance;

    public SafeWalk() {
        super("SafeWalk", "Prevents falling off edges", "Player");
        instance = this;
    }

    public static SafeWalk getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(
                    Text.literal("§4[§cAmberClient§4] §c§l" + getName() + " §r§cenabled"), true);
        }
        LOGGER.info("SafeWalk module enabled");
    }

    @Override
    public void onDisable() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(
                    Text.literal("§4[§cAmberClient§4] §c§l" + getName() + " §r§cdisabled"), true);
        }
        LOGGER.info("SafeWalk module disabled");
    }

    public static boolean shouldPreventMovement(Vec3d movement) {
        if (instance == null || !instance.isEnabled()) {
            return false;
        }

        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null || client.world == null) {
            return false;
        }

        if (client.player.isSneaking() || client.player.getAbilities().flying ||
                client.player.isSwimming() || client.player.isSubmergedInWater() ||
                !client.player.isOnGround()) {
            return false;
        }

        if (Math.abs(movement.x) < 0.001 && Math.abs(movement.z) < 0.001) {
            return false;
        }

        Vec3d currentPos = client.player.getPos();
        Vec3d newPos = new Vec3d(
                currentPos.x + movement.x,
                currentPos.y,
                currentPos.z + movement.z
        );

        return !isSafePosition(client.world, newPos);
    }

    private static boolean isSafePosition(World world, Vec3d pos) {
        double width = 0.3;

        Vec3d[] checkPositions = {
                new Vec3d(pos.x - width, pos.y, pos.z - width), // south-west corner
                new Vec3d(pos.x + width, pos.y, pos.z - width), // southeast corner
                new Vec3d(pos.x - width, pos.y, pos.z + width), // north-west corner
                new Vec3d(pos.x + width, pos.y, pos.z + width), // northeast corner
                new Vec3d(pos.x, pos.y, pos.z) // centre
        };

        for (Vec3d checkPos : checkPositions) {
            BlockPos blockPos = BlockPos.ofFloored(checkPos.x, checkPos.y - 1, checkPos.z);

            if (world.getBlockState(blockPos).isSolidBlock(world, blockPos)) {
                return true;
            }
        }

        return false;
    }
}
