package com.amberclient.mixins.murdererfinder;

import com.amberclient.utils.murdererfinder.MurdererFinder;
import com.amberclient.utils.murdererfinder.MinecraftUtils;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Inject(at = @At("RETURN"), method = "setTitle")
    private void onSetTitle(Text title, CallbackInfo info) {
        if (MurdererFinder.isEnabled()) {
            String s = title.getString().split("\n")[0].toLowerCase();
            if (s.startsWith("you win") || s.startsWith("you lose")) {
                MurdererFinder.roundEnded = true;
                MinecraftUtils.sendChatMessage("§4[AmberClient] §cRound ended");
            }
        }
    }
}
