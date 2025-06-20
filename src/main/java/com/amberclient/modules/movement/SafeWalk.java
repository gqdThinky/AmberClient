package com.amberclient.modules.movement;

import com.amberclient.utils.module.ConfigurableModule;
import com.amberclient.utils.module.Module;
import com.amberclient.utils.module.ModuleSettings;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class SafeWalk extends Module implements ConfigurableModule {

    public static final String MOD_ID = "amberclient-safewalk";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    // Settings
    private final ModuleSettings inAir = new ModuleSettings("InAir", "Enable SafeWalk in air", true);
    private final ModuleSettings randomEdgeDistance = new ModuleSettings("Random Edge Distance", "Randomize edge detection distance", true);

    // Internal state
    private final MinecraftClient mc = MinecraftClient.getInstance();
    private BlockPos currentBlock;
    public static boolean safewalk = false;
    public static double currentEdgeDistance = 1.0D;
    private final Random random = new Random();
    private int distanceUpdateTicks = 0;

    public SafeWalk() {
        super("SafeWalk", "Prevents falling off edges", "Movement");
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (this.isEnabled()) {
                onTick();
            }
        });
    }

    @Override
    public List<ModuleSettings> getSettings() {
        return Arrays.asList(inAir, randomEdgeDistance);
    }

    @Override
    public void onEnable() {
        currentBlock = null;
        safewalk = true;
        currentEdgeDistance = 1.0D;
        distanceUpdateTicks = 0;
    }

    @Override
    public void onDisable() {
        safewalk = false;
        currentEdgeDistance = 1.0D;
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        ClientPlayerEntity player = mc.player;

        boolean shouldEnableSafeWalk = true;

        if (!(boolean) inAir.getValue() && !player.isOnGround()) {
            shouldEnableSafeWalk = false;
        }

        if ((boolean) randomEdgeDistance.getValue() && shouldEnableSafeWalk) {
            distanceUpdateTicks++;
            int updateInterval = 40 + random.nextInt(80);
            if (distanceUpdateTicks >= updateInterval) {
                currentEdgeDistance = 0.7D + (random.nextDouble() * 0.6D);
                distanceUpdateTicks = 0;
            }
        } else {
            currentEdgeDistance = 1.0D;
        }

        safewalk = shouldEnableSafeWalk;

        BlockPos newBlock = new BlockPos(
                (int) Math.floor(player.getX()),
                (int) Math.floor(player.getY() - 0.2),
                (int) Math.floor(player.getZ())
        );
        if (!newBlock.equals(currentBlock)) {
            currentBlock = newBlock;
        }
    }
}