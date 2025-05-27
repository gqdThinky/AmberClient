package com.amberclient.mixin;

import com.amberclient.modules.Hitbox;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityHitboxMixin {

    @Inject(method = "getBoundingBox", at = @At("RETURN"), cancellable = true)
    private void onGetBoundingBoxForTargeting(CallbackInfoReturnable<Box> cir) {
        if (!Hitbox.isHitboxModuleEnabled) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.world == null) {
            return;
        }

        Entity entity = (Entity)(Object)this;
        if (entity == client.player || (client.player != null && entity.getUuid().equals(client.player.getUuid()))) {
            return;
        }

        // Appliquer l'expansion pour le ciblage/attaque OU pour le rendu des hitboxes en mode debug (F3+B)
        if (Hitbox.isCalculatingTarget() || client.getEntityRenderDispatcher().shouldRenderHitboxes()) {
            Box originalBox = cir.getReturnValue();
            if (originalBox != null) {
                double expandX = 0.25;
                double expandY = 0.6;
                double expandZ = 0.25;

                Box expandedBox = originalBox.expand(expandX, expandY, expandZ);
                cir.setReturnValue(expandedBox);
            }
        }
    }
}

// corriger la taille et l'emplacement de la nouvelle hitbox
// ajouter une hitbox dans le f3+b en orange qui affiche la nouvelle (en conservant celle de base de minecraft qui est blanche)