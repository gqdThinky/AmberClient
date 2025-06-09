package com.amberclient.modules.movement;

import com.amberclient.utils.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NoFall extends Module {
    public static final String MOD_ID = "amberclient-nofall";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    private final MinecraftClient mc = MinecraftClient.getInstance();

    public NoFall() {
        super("NoFall", "Prevents fall damage", "Movement");
    }

    @Override
    public void onTick() {
        ClientPlayerEntity player = mc.player;
        if (player == null || player.isSpectator() || player.isCreative()) return;

        player.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true, false));
    }
}
