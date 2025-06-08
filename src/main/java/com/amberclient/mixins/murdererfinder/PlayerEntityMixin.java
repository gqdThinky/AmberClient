package com.amberclient.mixins.murdererfinder;

import com.amberclient.utils.murdererfinder.MurdererFinder;
import com.amberclient.utils.murdererfinder.access.PlayerEntityMixinAccess;
import com.amberclient.utils.murdererfinder.config.ConfigManager;
import com.amberclient.utils.murdererfinder.MinecraftUtils;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin implements PlayerEntityMixinAccess {
    @Unique private boolean _isMurder = false;
    @Unique private boolean _isRealPlayer = false;
    @Unique private boolean _hasBow = false;
    @Unique private boolean _isDeadSpectator = false;

    @Unique @Override
    public boolean isMurder() {
        return _isMurder;
    }

    @Unique @Override
    public boolean hasBow() {
        return _hasBow;
    }

    @Unique @Override
    public boolean isRealPlayer() {
        return _isRealPlayer;
    }

    @Unique @Override
    public boolean isDeadSpectator() {
        return _isDeadSpectator;
    }

    @Inject(at = @At("RETURN"), method = "<init>")
    private void onInit(World world, BlockPos pos, float yaw, GameProfile profile, CallbackInfo info) {
        if (MurdererFinder.isEnabled()) {
            _isMurder = MurdererFinder.markedMurders.contains(profile.getId());
            _hasBow = MurdererFinder.markedDetectives.contains(profile.getId());
        }
    }

    @Inject(at = @At("RETURN"), method = "tick")
    private void onTick(CallbackInfo info) {
        if (MurdererFinder.isActive()) {
            PlayerEntity player = (PlayerEntity)(Object)this;
            this._isRealPlayer = !player.isSleeping() && !player.isMainPlayer() && MinecraftUtils.isPlayerInTabList(player);

            if ((MurdererFinder.clientIsDead || MurdererFinder.roundEnded) && isRealPlayer() && !isDeadSpectator()) {
                StatusEffectInstance activeInvisibilityEffect = player.getStatusEffect(StatusEffects.INVISIBILITY);
//                if (player.isInvisible() && activeInvisibilityEffect != null) {
//                    MMHelper.printChatMsg(Text.of(activeInvisibilityEffect.getDuration()+""));
//                }
                _isDeadSpectator = player.getAbilities().allowFlying || player.getAbilities().flying || (activeInvisibilityEffect != null && activeInvisibilityEffect.getDuration() > 30000);
            }
        }
    }

    @Inject(at = @At("RETURN"), method = "getEquippedStack")
    private void onEquip(EquipmentSlot slot, CallbackInfoReturnable<ItemStack> info) {
        if (MurdererFinder.isActive() && !MurdererFinder.roundEnded) {
            if (!isMurder() && isRealPlayer()) {
                Item heldItem = info.getReturnValue().getItem();
                if (!hasBow() && (heldItem == Items.BOW || heldItem == Items.ARROW)) {
                    _hasBow = true;
                    MurdererFinder.markedDetectives.add(((PlayerEntity)(Object)this).getGameProfile().getId());
                } else if (ConfigManager.getConfig().mm.isMurderItem(heldItem)) {
                    if (!MurdererFinder.clientIsMurder) {
                        MinecraftUtils.sendChatMessage("§4[AmberClient] " + Text.translatable("message.mm.murder_marked", Formatting.RED+((PlayerEntity)(Object)this).getGameProfile().getName()).getString());
                    }
                    _isMurder = true;
                    MurdererFinder.markedMurders.add(((PlayerEntity)(Object)this).getGameProfile().getId());
                }
            }

        }
    }
}
