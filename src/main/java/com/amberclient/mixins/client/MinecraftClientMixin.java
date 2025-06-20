package com.amberclient.mixins.client;

import com.amberclient.modules.player.FastPlace;
import com.amberclient.modules.combat.Hitbox;
import com.amberclient.modules.render.EntityESP;
import com.amberclient.utils.murdererfinder.access.EntityMixinAccess;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Shadow private int itemUseCooldown;

    @Inject(method = "doAttack", at = @At("HEAD"))
    private void beforeDoAttack(CallbackInfoReturnable<Boolean> cir) {
        if (!Hitbox.isHitboxModuleEnabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        Hitbox hitboxModule = Hitbox.getInstance();
        if (client.crosshairTarget != null && client.crosshairTarget.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityHitResult = (EntityHitResult) client.crosshairTarget;
            Entity target = entityHitResult.getEntity();
            Vec3d targetPos = target.getPos().add(0, target.getHeight() / 2, 0);
            hitboxModule.getRotationFaker().faceVectorPacket(targetPos);
        }

        Hitbox.setCalculatingTarget(true);
    }

    @Inject(method = "doAttack", at = @At("TAIL"))
    private void afterDoAttack(CallbackInfoReturnable<Boolean> cir) {
        Hitbox.setCalculatingTarget(false);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (FastPlace.isFastPlaceEnabled && ((MinecraftClient) (Object) this).options.useKey.isPressed()) {
            int randomDelay = FastPlace.getRandomDelay();

            if (this.itemUseCooldown > randomDelay) {
                this.itemUseCooldown = randomDelay;
            }
        }
    }

    @Inject(method = "hasOutline", at = @At("HEAD"), cancellable = true)
    private void onHasOutline(Entity entity, CallbackInfoReturnable<Boolean> info) {
        EntityESP espModule = EntityESP.getInstance();
        if (espModule == null || !espModule.isEnabled()) return;

        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) entity;
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null) return;

            double distance = client.player.getPos().distanceTo(livingEntity.getPos());
            double range = espModule.getRangeSetting().getDoubleValue() * 16.0; // Convert chunks to blocks

            if (distance > range) return;

            // Apply outline and set glow color based on settings
            if (espModule.getRenderPlayersSetting().getBooleanValue() && livingEntity instanceof net.minecraft.entity.player.PlayerEntity) {
                ((EntityMixinAccess) entity).setGlowColor(0xFFF126);
                info.setReturnValue(true);
            } else if (espModule.getRenderMobsSetting().getBooleanValue() && !(livingEntity instanceof net.minecraft.entity.player.PlayerEntity)) {
                ((EntityMixinAccess) entity).setGlowColor(0x15BFD6);
                info.setReturnValue(true);
            }
        }
    }
}