package com.amberclient.mixin;

import com.amberclient.modules.Hitbox;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "updateCrosshairTarget", at = @At("HEAD"))
    private void beforeUpdateCrosshairTarget(float tickDelta, CallbackInfo ci) {
        if (!Hitbox.isHitboxModuleEnabled) return;

        Hitbox.setCalculatingTarget(true);
    }

    @Inject(method = "updateCrosshairTarget", at = @At("TAIL"))
    private void afterUpdateCrosshairTarget(float tickDelta, CallbackInfo ci) {
        Hitbox.setCalculatingTarget(false);
    }
}