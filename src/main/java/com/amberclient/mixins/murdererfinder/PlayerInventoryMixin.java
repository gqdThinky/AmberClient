package com.amberclient.mixins.murdererfinder;

import com.amberclient.utils.murdererfinder.MurdererFinder;
import com.amberclient.utils.murdererfinder.config.ConfigManager;
import com.amberclient.utils.murdererfinder.MinecraftUtils;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin {
    @Inject(at = @At("HEAD"), method = "setStack")
    private void onSetStack(int slot, ItemStack stack, CallbackInfo info) {
        if (MurdererFinder.isActive() && !MurdererFinder.clientIsMurder)
            if (ConfigManager.getConfig().mm.isMurderItem(stack.getItem())) {
                MurdererFinder.clientIsMurder = true;
                MinecraftUtils.sendChatMessage("§4[AmberClient] " + Text.translatable("message.mm.client_is_murder", Formatting.RED).getString());
            }
    }
}
