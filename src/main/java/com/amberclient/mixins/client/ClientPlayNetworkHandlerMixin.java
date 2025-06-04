package com.amberclient.mixins.client;

import com.amberclient.events.EventManager;
import com.amberclient.utils.general.TickRate;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTickStart(CallbackInfo ci) {
        EventManager.getInstance().firePreMotion();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTickEnd(CallbackInfo ci) {
        EventManager.getInstance().firePostMotion();
    }

    @Inject(method = "onWorldTimeUpdate", at = @At("HEAD"))
    private void onWorldTimeUpdate(WorldTimeUpdateS2CPacket packet, CallbackInfo ci) {
        TickRate.onPacket(packet);
    }
}