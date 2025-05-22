package com.amberclient.modules.hacks;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.text.Text;
import com.amberclient.modules.Module;

public class NoFall extends Module {
    private final MinecraftClient mc = MinecraftClient.getInstance();

    private int tickCounter = 0;

    public NoFall() {
        super("NoFall", "Prevents fall damage", "Movement");
    }

    @Override
    protected void onEnable() {
        mc.player.sendMessage(Text.literal("§4[§cAmberClient§4] §c§l" + getName() + " §r§cactivé"), false);
    }

    @Override
    protected void onDisable() {
        mc.player.sendMessage(Text.literal("§4[§cAmberClient§4] §c§l" + getName() + " §r§cdésactivé"), false);
    }

    @Override
    public void onTick() {
        ClientPlayerEntity player = mc.player;
        if (player == null || player.isSpectator() || player.isCreative()) return;

        player.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true, false));
    }
}