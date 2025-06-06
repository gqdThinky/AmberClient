package com.amberclient.mixins.amberclient;

import com.amberclient.modules.render.EntityESP;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class EntityESPMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends net.minecraft.client.render.entity.model.EntityModel<? super S>> {

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;pop()V", shift = At.Shift.BEFORE))
    private void onRenderBeforePop(S renderState, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        EntityESP esp = EntityESP.getInstance();

        if (!esp.isEnabled() || renderState == null) return;

        boolean isPlayer = renderState instanceof net.minecraft.client.render.entity.state.PlayerEntityRenderState;

        if ((isPlayer && !esp.getRenderPlayersSetting().isEnabled()) ||
                (!isPlayer && !esp.getRenderMobsSetting().isEnabled())) {
            return;
        }

        @SuppressWarnings("unchecked")
        LivingEntityRenderer<T, S, M> renderer = (LivingEntityRenderer<T, S, M>) (Object) this;
        M model = renderer.getModel();

        int outlineColor;
        if (isPlayer) {
            outlineColor = 0xFFFF8000; // Orange for players
        } else {
            outlineColor = 0xFF708090; // Gray for mobs
        }

        matrices.push();
        model.render(matrices,
                vertexConsumers.getBuffer(net.minecraft.client.render.RenderLayer.getOutline(renderer.getTexture(renderState))),
                light,
                OverlayTexture.DEFAULT_UV,
                outlineColor
        );
        matrices.pop();
    }

    private Identifier getEntityTexture(LivingEntityRenderer<T, S, M> renderer, S renderState) {
        try {
            return renderer.getTexture(renderState);
        } catch (Exception e) {
            return Identifier.of("minecraft", "textures/misc/unknown_entity.png");
        }
    }
}