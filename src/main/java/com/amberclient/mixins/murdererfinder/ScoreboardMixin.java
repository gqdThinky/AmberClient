package com.amberclient.mixins.murdererfinder;

import com.amberclient.utils.murdererfinder.MurdererFinder;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.number.NumberFormat;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Scoreboard.class)
public class ScoreboardMixin {
    @Inject(at = @At("HEAD"), method = "addObjective")
    private void onAddObjective(String name, ScoreboardCriterion criterion, Text displayName, ScoreboardCriterion.RenderType renderType, boolean displayAutoUpdate, NumberFormat numberFormat, CallbackInfoReturnable<ScoreboardObjective> info) {
        // Used for detecting active mini-game
        if (MurdererFinder.onHypixel) {
            String displayNameString = displayName.getString();
            if (displayNameString.equalsIgnoreCase("murder mystery")) {
                if (name.equalsIgnoreCase("prescoreboard") || name.equalsIgnoreCase("mmlobby"))
                    MurdererFinder.setCurrentLobby(MurdererFinder.HypixelLobbies.MurderMysteryLobby);
                else if (MurdererFinder.lobby != MurdererFinder.HypixelLobbies.MurderMystery && name.equalsIgnoreCase("murdermystery"))
                    MurdererFinder.setCurrentLobby(MurdererFinder.HypixelLobbies.MurderMystery);
            }
        }
    }
}
