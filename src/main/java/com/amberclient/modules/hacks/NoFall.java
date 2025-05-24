package com.amberclient.modules.hacks;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.text.Text;
import com.amberclient.modules.Module;
import net.minecraft.util.Formatting;

public class NoFall extends Module {
    private final MinecraftClient mc = MinecraftClient.getInstance();

    private int tickCounter = 0;

    public NoFall() {
        super("NoFall", "Prevents fall damage", "Movement");
    }

    @Override
    protected void onEnable() {
        //mc.player.sendMessage(Text.literal("§4[§cAmberClient§4] §c§l" + getName() + " §r§cactivé"), false);
        MinecraftClient.getInstance().player.sendMessage(
                Text.literal("§4[§cAmberClient§4] §c§l" + getName() + " §r§cactivated").formatted(Formatting.RED), true);
    }

    @Override
    protected void onDisable() {
        MinecraftClient.getInstance().player.sendMessage(
                Text.literal("§4[§cAmberClient§4] §c§l" + getName() + " §r§deactivated").formatted(Formatting.RED), true);
    }

    @Override
    public void onTick() {
        ClientPlayerEntity player = mc.player;
        if (player == null || player.isSpectator() || player.isCreative()) return;

        player.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true, false));
    }
}