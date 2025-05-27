package com.amberclient.mixin;

import com.amberclient.modules.Hitbox;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public class MinecraftClientAttackMixin {

    @Inject(method = "doAttack", at = @At("HEAD"))
    private void beforeDoAttack(CallbackInfoReturnable<Boolean> cir) {
        if (!Hitbox.isHitboxModuleEnabled) return;

        // Activer temporairement les hitboxes étendues pour l'attaque
        Hitbox.setCalculatingTarget(true);
    }

    @Inject(method = "doAttack", at = @At("TAIL"))
    private void afterDoAttack(CallbackInfoReturnable<Boolean> cir) {
        // Désactiver après l'attaque
        Hitbox.setCalculatingTarget(false);
    }
}
