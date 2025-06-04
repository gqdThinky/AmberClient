package com.amberclient.mixins.client;

import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {

//    @Inject(method = "move", at = @At("HEAD"), cancellable = true)
//    private void safewalk_preventMovement(MovementType movementType, Vec3d movement, CallbackInfo ci) {
//        if (SafeWalk.shouldPreventMovement(movement)) {
//            ci.cancel();
//
//            Vec3d safeMovement = new Vec3d(0, movement.y, 0);
//
//            ClientPlayerEntity self = (ClientPlayerEntity)(Object)this;
//            self.setPosition(self.getX(), self.getY() + safeMovement.y, self.getZ());
//        }
//    }
}