package com.amberclient.modules.movement;

import com.amberclient.utils.module.ConfigurableModule;
import com.amberclient.utils.module.Module;
import com.amberclient.utils.module.ModuleSetting;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EntityPose;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;

public class SafeWalk extends Module implements ConfigurableModule {

    public static final String MOD_ID = "amberclient-safewalk";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    // Settings
    private final ModuleSetting inAir = new ModuleSetting("InAir", "Enable SafeWalk in air", true);
    private final ModuleSetting minemen = new ModuleSetting("Minemen", "Enable Minemen mode", true);

    // Internal state
    private final MinecraftClient mc = MinecraftClient.getInstance();
    private BlockPos currentBlock;
    public static boolean safewalk = false;

    public SafeWalk() {
        super("SafeWalk", "Prevents falling off edges", "Movement");
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (this.isEnabled()) {
                onTick();
            }
        });
    }

    @Override
    public List<ModuleSetting> getSettings() {
        return Arrays.asList(inAir, minemen);
    }

    @Override
    public void onEnable() {
        LOGGER.info("SafeWalk module enabled");
        currentBlock = null;
        safewalk = true;
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.player.setPose(EntityPose.STANDING);
        }
        LOGGER.info("SafeWalk module disabled");
        safewalk = false;
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        ClientPlayerEntity player = mc.player;

        boolean shouldEnableSafeWalk = true;

        if (!(boolean) inAir.getValue() && !player.isOnGround()) {
            shouldEnableSafeWalk = false;
        }

        if ((boolean) minemen.getValue() && shouldEnableSafeWalk) {
            player.setSneaking(true);
        } else {
            player.setSneaking(player.isSneaking());
        }

        safewalk = shouldEnableSafeWalk;

        // Update currentBlock to track player position
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