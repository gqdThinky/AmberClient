package com.amberclient.mixins.amberclient;

import com.amberclient.events.EventManager;
import com.amberclient.events.PostVelocityEvent;
import com.amberclient.events.PreVelocityEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class VelocityMixin {

    @Inject(method = "onEntityVelocityUpdate", at = @At("HEAD"), cancellable = true)
    public void onPreEntityVelocityUpdate(EntityVelocityUpdateS2CPacket packet, CallbackInfo ci) {
        assert MinecraftClient.getInstance().player != null;
        if (packet.getEntityId() == MinecraftClient.getInstance().player.getId()) {
            PreVelocityEvent event = new PreVelocityEvent();
            event.setMotionX(packet.getVelocityX());
            event.setMotionY(packet.getVelocityY());
            event.setMotionZ(packet.getVelocityZ());

            EventManager.getInstance().firePreVelocity(event);

            if (event.isCanceled()) {
                ci.cancel();
                return;
            }

            ci.cancel();

            MinecraftClient client = MinecraftClient.getInstance();
            double motionX = event.getMotionX();
            double motionY = event.getMotionY();
            double motionZ = event.getMotionZ();

            client.player.setVelocity(motionX, motionY, motionZ);
        }
    }

    @Inject(method = "onEntityVelocityUpdate", at = @At("RETURN"))
    public void onPostEntityVelocityUpdate(EntityVelocityUpdateS2CPacket packet, CallbackInfo ci) {
        if (MinecraftClient.getInstance().player == null) return;

        if (packet.getEntityId() == MinecraftClient.getInstance().player.getId()) {
            EventManager.getInstance().firePostVelocity(new PostVelocityEvent());
        }
    }
}