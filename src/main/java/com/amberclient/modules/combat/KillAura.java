package com.amberclient.modules.combat;

import com.amberclient.api.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;

public class KillAura extends Module {

    private final float range = 4.5f; // You can make this configurable

    public KillAura() {
        super("KillAura", "Automatically attacks nearby players and hostile mobs", Category.COMBAT);
    }

    @Override
    public void onUpdate() {
        if (!this.isToggled()) return;

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc.player;
        if (player == null || mc.world == null) return;

        EntityLivingBase closest = null;
        double closestDist = Double.MAX_VALUE;

        for (Entity entity : mc.world.loadedEntityList) {
            if (entity == player) continue;
            if (!(entity instanceof EntityLivingBase)) continue;
            EntityLivingBase target = (EntityLivingBase) entity;
            if (!target.isEntityAlive() || target.isInvisible()) continue;

            // Only attack players and hostile mobs
            if (!(target instanceof EntityPlayer) && !(target instanceof IMob)) continue;

            double dist = player.getDistance(target);
            if (dist <= range && dist < closestDist) {
                closestDist = dist;
                closest = target;
            }
        }

        if (closest != null) {
            mc.playerController.attackEntity(player, closest);
            player.swingArm(player.getActiveHand());
        }
    }
}