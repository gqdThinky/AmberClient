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

        if (!FastBreak.isFastBreakEnabled()) return;
        FastBreak fastBreak = FastBreak.getInstance();

        if (fastBreak == null) return;
        MinecraftClient client = MinecraftClient.getInstance();

        this.blockBreakingCooldown = 0;

        if (fastBreak.isLegitMode() || this.currentBreakingProgress >= 1.0F) return;

        if (!this.currentBreakingPos.equals(lastBlockPos)) {
            lastBlockPos = this.currentBreakingPos;
            fastBreakBlock = random.nextDouble() <= fastBreak.getActivationChance();
        }

        if (BlockUtils.INSTANCE.isUnbreakable(this.currentBreakingPos) || !fastBreakBlock) return;

        Direction direction = client.player.getHorizontalFacing();
        PlayerActionC2SPacket packet = new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK,
                this.currentBreakingPos,
                direction
        );
        client.getNetworkHandler().sendPacket(packet);
    }

}