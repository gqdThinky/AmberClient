package com.amberclient.mixins.client;

import com.amberclient.modules.player.FastBreak;
import com.amberclient.utils.general.BlockUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {
    @Shadow private int blockBreakingCooldown;
    @Shadow private BlockPos currentBreakingPos;
    @Shadow private float currentBreakingProgress;

    @Unique private final Random random = new Random();
    @Unique private BlockPos lastBlockPos;
    @Unique private boolean fastBreakBlock;

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (!FastBreak.isFastBreakEnabled) return;

        FastBreak fastBreak = FastBreak.getInstance();
        MinecraftClient client = MinecraftClient.getInstance();

        // Reset block breaking cooldown
        this.blockBreakingCooldown = 0;

        // Skip if in legit mode or block is fully broken
        if (fastBreak.isLegitMode() || this.currentBreakingProgress >= 1.0F) return;

        // Update block tracking for activation chance
        if (!this.currentBreakingPos.equals(lastBlockPos)) {
            lastBlockPos = this.currentBreakingPos;
            fastBreakBlock = random.nextDouble() <= fastBreak.getActivationChance();
        }

        // Skip if block is unbreakable or not selected for fast breaking
        if (BlockUtils.isUnbreakable(this.currentBreakingPos) || !fastBreakBlock) return;

        // Send block breaking packet to speed up breaking
        Direction direction = client.player.getHorizontalFacing();
        PlayerActionC2SPacket packet = new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK,
                this.currentBreakingPos,
                direction
        );
        client.getNetworkHandler().sendPacket(packet);
    }
}