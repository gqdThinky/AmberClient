package com.amberclient.mixin;

import com.amberclient.modules.combat.Hitbox;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(Entity.class)
public class EntityHitboxMixin {
    @Unique
    private static final Random RANDOM = new Random();

    @Inject(method = "getBoundingBox", at = @At("RETURN"), cancellable = true)
    private void onGetBoundingBoxForTargeting(CallbackInfoReturnable<Box> cir) {
        if (!Hitbox.isHitboxModuleEnabled || (MinecraftClient.getInstance() == null || MinecraftClient.getInstance().world == null)) return;

        Entity entity = (Entity)(Object)this;
        MinecraftClient client = MinecraftClient.getInstance();
        if (entity == client.player || (client.player != null && entity.getUuid().equals(client.player.getUuid()))) return;

        // Only apply local hitbox expansion for rendering and targeting, not server packets
        if (Hitbox.isCalculatingTarget()) {
            Box originalBox = cir.getReturnValue();
            if (originalBox != null) {
                double distance = client.player.getPos().distanceTo(entity.getPos());
                if (distance > 4.0) return; // Limit to close range to avoid detection

                Vec3d playerPos = client.player.getEyePos();
                Vec3d entityPos = entity.getPos().add(0, entity.getHeight() / 2, 0);
                Vec3d lookVec = client.player.getRotationVector();
                Vec3d toEntity = entityPos.subtract(playerPos).normalize();
                double dot = lookVec.dotProduct(toEntity);
                double angle = Math.acos(dot) * (180.0 / Math.PI);

                double baseExpandX = Hitbox.getInstance().getExpandX();
                double baseExpandYUp = Hitbox.getInstance().getExpandYUp();
                double baseExpandZ = Hitbox.getInstance().getExpandZ();

                double angleFactor = Math.min(1.0, angle / 45.0);
                double expandX = baseExpandX * (0.5 + angleFactor * 0.5);
                double expandYUp = baseExpandYUp * (0.5 + angleFactor * 0.5);
                double expandZ = baseExpandZ * (0.5 + angleFactor * 0.5);

                double randomX = (RANDOM.nextDouble() - 0.5) * 0.1;
                double randomY = (RANDOM.nextDouble() - 0.5) * 0.1;
                double randomZ = (RANDOM.nextDouble() - 0.5) * 0.1;
                expandX += randomX;
                expandYUp += randomY;
                expandZ += randomZ;

                expandX = MathHelper.clamp(expandX, 0.0, 2.0);
                expandYUp = MathHelper.clamp(expandYUp, 0.0, 2.0);
                expandZ = MathHelper.clamp(expandZ, 0.0, 2.0);

                Box expandedBox = new Box(
                        originalBox.minX - expandX,
                        originalBox.minY,
                        originalBox.minZ - expandZ,
                        originalBox.maxX + expandX,
                        originalBox.maxY + expandYUp,
                        originalBox.maxZ + expandZ
                );
                cir.setReturnValue(expandedBox);
            }
        }
    }
}
