package com.amberclient.mixins.amberclient;

import com.amberclient.modules.combat.Velocity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityVelocityUpdateS2CPacket.class)
public class VelocityMixin {

    @Inject(method = "apply(Lnet/minecraft/network/listener/ClientPlayPacketListener;)V", at = @At("HEAD"), cancellable = true)
    private void onApplyVelocity(ClientPlayPacketListener clientPlayPacketListener, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        Velocity velocityModule = Velocity.getInstance();
        if (velocityModule == null || !velocityModule.isEnabled()) return;

        EntityVelocityUpdateS2CPacket packet = (EntityVelocityUpdateS2CPacket)(Object)this;

        if (packet.getEntityId() != client.player.getId()) return;

        Vec3d originalVelocity = new Vec3d(
                packet.getVelocityX() / 8000.0,
                packet.getVelocityY() / 8000.0,
                packet.getVelocityZ() / 8000.0
        );

        Vec3d currentVelocity = client.player.getVelocity();
        client.player.setVelocity(originalVelocity);

        boolean isKnockback = isSignificantKnockback(velocityModule, client.player, originalVelocity);

        client.player.setVelocity(currentVelocity);

        if (isKnockback) {
            Vec3d reducedVelocity = velocityModule.calculateReducedVelocity(client.player, originalVelocity);
            client.player.setVelocity(reducedVelocity);
            ci.cancel();
        }
    }

    @Unique
    private boolean isSignificantKnockback(Velocity velocityModule, net.minecraft.entity.player.PlayerEntity player, Vec3d velocity) {
        boolean wasHitRecently = player.hurtTime > 0;
        if (!wasHitRecently) return false;

        boolean hasHorizontal = velocity.horizontalLength() > 0.15;
        boolean hasVertical = velocity.y > 0.1;

        boolean velocityChanged = velocity.lengthSquared() > 0.1; // Seuil minimal

        return (hasHorizontal || hasVertical) && velocityChanged;
    }
}