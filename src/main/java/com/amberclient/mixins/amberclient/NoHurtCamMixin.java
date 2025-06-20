package com.amberclient.mixins.amberclient;

import com.amberclient.modules.render.NoHurtCam;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class NoHurtCamMixin {

    @Inject(
            at = {@At("HEAD")},
            method = "tiltViewWhenHurt(Lnet/minecraft/client/util/math/MatrixStack;F)V",
            cancellable = true
    )
    private void onTiltViewWhenHurt(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        if (NoHurtCam.getInstance() != null && NoHurtCam.getInstance().isEnabled()) {
            ci.cancel();
        }
    }
}