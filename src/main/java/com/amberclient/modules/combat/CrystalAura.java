package AmberClient.src.main.java.com.amberclient.modules.combat;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C0APacketAnimation;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CrystalAura {
    private final Minecraft mc = Minecraft.getMinecraft();
    private final float breakRange = 5.5F;
    private final float placeRange = 4.5F;
    private final int delay = 1; // ticks between actions
    private int tickCounter = 0;

    public void onUpdate() {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        tickCounter++;
        if (tickCounter < delay) return;
        tickCounter = 0;

        EntityPlayer target = getClosestEnemy(8.0F);
        if (target != null) {
            EntityEnderCrystal crystal = getClosestCrystalTo(target, breakRange);
            if (crystal != null) {
                attackCrystal(crystal);
                return; // Only one action per tick
            }
        }

        if (mc.thePlayer.getHeldItem() != null &&
            mc.thePlayer.getHeldItem().getItem() == Items.end_crystal &&
            target != null) {
            BlockPos pos = findBestPlacePos(target);
            if (pos != null) {
                placeCrystal(pos);
            }
        }
    }

    private EntityPlayer getClosestEnemy(float range) {
        return mc.theWorld.playerEntities.stream()
                .filter(p -> p != mc.thePlayer && !p.isDead && mc.thePlayer.getDistanceToEntity(p) <= range)
                .min(Comparator.comparingDouble(p -> mc.thePlayer.getDistanceToEntity(p)))
                .orElse(null);
    }

    private EntityEnderCrystal getClosestCrystalTo(EntityPlayer target, float range) {
        return mc.theWorld.loadedEntityList.stream()
                .filter(e -> e instanceof EntityEnderCrystal)
                .map(e -> (EntityEnderCrystal) e)
                .filter(c -> c.getDistanceToEntity(target) <= range)
                .min(Comparator.comparingDouble(c -> c.getDistanceToEntity(target)))
                .orElse(null);
    }

    private BlockPos findBestPlacePos(EntityPlayer target) {
        BlockPos base = new BlockPos(target.posX, target.posY - 1, target.posZ);
        int r = (int) Math.ceil(placeRange);
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (int x = -r; x <= r; x++) {
            for (int z = -r; z <= r; z++) {
                BlockPos pos = base.add(x, 0, z);
                if (isValidCrystalPos(pos)) {
                    double dist = mc.thePlayer.getDistanceSq(pos);
                    if (dist < bestDist) {
                        bestDist = dist;
                        best = pos;
                    }
                }
            }
        }
        return best;
    }

    private boolean isValidCrystalPos(BlockPos pos) {
        if (!(mc.theWorld.getBlockState(pos).getBlock() == Blocks.obsidian ||
              mc.theWorld.getBlockState(pos).getBlock() == Blocks.bedrock)) return false;

        if (!mc.theWorld.isAirBlock(pos.up())) return false;
        if (!mc.theWorld.isAirBlock(pos.up(2))) return false;

        List<Entity> entities = mc.theWorld.getEntitiesWithinAABBExcludingEntity(null,
            mc.theWorld.getBlockState(pos.up()).getBlock().getCollisionBoundingBox(mc.theWorld, pos.up(), mc.theWorld.getBlockState(pos.up())));
        return entities == null || entities.isEmpty();
    }

    private void attackCrystal(EntityEnderCrystal crystal) {
        mc.thePlayer.swingItem();
        mc.thePlayer.sendQueue.addToSendQueue(new C02PacketUseEntity(crystal, C02PacketUseEntity.Action.ATTACK));
        mc.thePlayer.sendQueue.addToSendQueue(new C0APacketAnimation());
    }

    private void placeCrystal(BlockPos pos) {
        mc.thePlayer.sendQueue.addToSendQueue(
            new C08PacketPlayerBlockPlacement(
                pos, EnumFacing.UP.getIndex(), mc.thePlayer.getHeldItem(), 0.5F, 1.0F, 0.5F
            )
        );
        mc.thePlayer.swingItem();
    }
}
