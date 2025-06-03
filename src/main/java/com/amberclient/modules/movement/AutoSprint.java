package com.amberclient.modules.movement;

import com.amberclient.api.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;

public class AutoSprint extends Module {

    public AutoSprint() {
        super("AutoSprint", "Automatically makes you sprint", Category.MOVEMENT);
    }

    @Override
    public void onUpdate() {
        if (this.isToggled()) {
            Minecraft mc = Minecraft.getMinecraft();
            EntityPlayerSP player = mc.player;
            if (player != null && player.moveForward > 0 && !player.isSneaking() && !player.isCollidedHorizontally) {
                player.setSprinting(true);
            }
        }
    }

    @Override
    public void onDisable() {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc.player;
        if (player != null) {
            player.setSprinting(false);
        }
    }
}