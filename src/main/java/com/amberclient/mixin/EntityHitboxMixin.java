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
        if (!Hitbox.isHitboxModuleEnabled || (MinecraftClient.getInstance() == null || MinecraftClient.getInstance().world == null)) return;

        Entity entity = (Entity)(Object)this;
        if (entity == MinecraftClient.getInstance().player || (MinecraftClient.getInstance().player != null && entity.getUuid().equals(MinecraftClient.getInstance().player.getUuid()))) return;

        if (Hitbox.isCalculatingTarget()) {
            Box originalBox = cir.getReturnValue();
            if (originalBox != null) {
                double expandX = Hitbox.getInstance().getExpandX();
                double expandYUp = Hitbox.getInstance().getExpandYUp();
                double expandZ = 0.0; // No front/back expansion

                Box expandedBox = new Box(
                        originalBox.minX - expandX,
                        originalBox.minY,
                        originalBox.minZ,
                        originalBox.maxX + expandX,
                        originalBox.maxY + expandYUp,
                        originalBox.maxZ
                );
                cir.setReturnValue(expandedBox);
            }
        }
    }
}