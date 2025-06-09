package com.amberclient.modules.movement;

import com.amberclient.utils.KeybindsManager;
import com.amberclient.utils.module.ConfigurableModule;
import com.amberclient.utils.module.Module;
import com.amberclient.utils.module.ModuleSetting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SnowBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class AutoClutch extends Module implements ConfigurableModule {
    public static final String MOD_ID = "amberclient-autoclutch";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    // Settings
    private final List<ModuleSetting> settings;
    private final ModuleSetting range;
    private final ModuleSetting cpsCap;
    private final ModuleSetting holdMode;

    protected MinecraftClient mc = MinecraftClient.getInstance();
    private float startYaw;
    private float startPitch;
    private boolean wasKeyPressed = false;
    private long lastPlaceTime = 0;
    private static final long PLACE_DELAY = 50; // 50ms comme l'original

    public AutoClutch() {
        super("AutoClutch", "R", "Movement");

        // Initialize settings
        range = new ModuleSetting("Range", "Distance to search for block placement (blocks)", 4.0, 1.0, 6.0, 1.0);
        cpsCap = new ModuleSetting("CPS Cap", "Limit block placement speed", true);
        holdMode = new ModuleSetting("Hold Mode", "Deactivate module when key is released", false);

        settings = new ArrayList<>();
        settings.add(range);
        settings.add(cpsCap);
        settings.add(holdMode);
    }

    @Override
    public void onEnable() {
        if (mc.player != null) {
            startYaw = mc.player.getYaw();
            startPitch = mc.player.getPitch();
        }
        LOGGER.info(getName() + " module enabled");
    }

    @Override
    public void onDisable() {
        LOGGER.info(getName() + " module disabled");
    }

    public void handleKeyInput() {
        boolean isKeyPressed = KeybindsManager.INSTANCE.getAutoClutchKey().isPressed();

        if (isKeyPressed && !wasKeyPressed) {
            if (!holdMode.getBooleanValue()) {
                toggle();
            } else {
                if (!isEnabled()) {
                    this.enabled = true;
                    onEnable();
                }
            }
        } else if (!isKeyPressed && wasKeyPressed) {
            if (holdMode.getBooleanValue() && isEnabled()) {
                this.enabled = false;
                onDisable();
            }
        }

        wasKeyPressed = isKeyPressed;
    }

    public void onTick() {
        handleKeyInput();

        if (isEnabled() && mc.player != null && mc.world != null) {
            // CPS cap check
            if (cpsCap.getBooleanValue() && System.currentTimeMillis() - lastPlaceTime < PLACE_DELAY) {
                return;
            }

            // Place block using optimized algorithm
            if (placeBlockFast((int) range.getDoubleValue())) {
                lastPlaceTime = System.currentTimeMillis();
            }
        }
    }

    // Algorithme rapide inspiré de l'original
    public boolean placeBlockFast(int searchRange) {
        if (mc.player == null || mc.world == null) return false;

        Vec3d playerPos = mc.player.getPos();
        BlockPos playerBlockPos = new BlockPos((int) playerPos.x, (int) playerPos.y, (int) playerPos.z);

        // Vérifier d'abord directement en dessous
        if (isAirBlock(getBlock(playerBlockPos.down()))) {
            if (placeBlockAt(playerBlockPos.down())) {
                return true;
            }
        }

        // Recherche rapide en spirale
        for (int dist = 1; dist <= searchRange; dist++) {
            for (int y = 0; y >= -dist; y--) { // Priorité vers le bas
                for (int x = -dist; x <= dist; x++) {
                    for (int z = -dist; z <= dist; z++) {
                        // Optimisation: seulement les bords du carré
                        if (Math.abs(x) != dist && Math.abs(z) != dist && y != -dist) continue;

                        BlockPos targetPos = playerBlockPos.add(x, y, z);

                        // Distance check rapide
                        if (playerPos.squaredDistanceTo(Vec3d.ofCenter(targetPos)) > searchRange * searchRange) {
                            continue;
                        }

                        if (placeBlockAt(targetPos)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    // Placement de bloc optimisé et direct
    public boolean placeBlockAt(BlockPos pos) {
        if (mc.player == null || mc.world == null) return false;

        // Vérifier si la position est de l'air
        if (!isAirBlock(getBlock(pos))) return false;

        // Vérifier et sélectionner les blocs
        if (!ensureBlockSelected()) return false;

        Vec3d eyesPos = new Vec3d(mc.player.getX(),
                mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()),
                mc.player.getZ());

        // Essayer tous les côtés - ordre optimisé pour le clutch
        Direction[] priorityOrder = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.UP, Direction.DOWN};

        for (Direction side : priorityOrder) {
            BlockPos neighbor = pos.offset(side);
            Direction placementSide = side.getOpposite();

            // Vérifier si le voisin est solide
            BlockState neighborState = mc.world.getBlockState(neighbor);
            if (!neighborState.isSolidBlock(mc.world, neighbor)) continue;

            // Calculer le point de frappe
            Vec3d hitVec = Vec3d.ofCenter(neighbor)
                    .add(Vec3d.of(placementSide.getVector()).multiply(0.5));

            // Vérifier la portée (6 blocs max)
            if (eyesPos.squaredDistanceTo(hitVec) > 36.0) continue;

            // Calculer et appliquer la rotation IMMÉDIATEMENT
            float[] angles = calculateRotationFast(hitVec, eyesPos);
            mc.player.setYaw(angles[0]);
            mc.player.setPitch(angles[1]);

            // Placer le bloc IMMÉDIATEMENT
            BlockHitResult hitResult = new BlockHitResult(hitVec, placementSide, neighbor, false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult);
            mc.player.swingHand(Hand.MAIN_HAND);

            return true;
        }

        return false;
    }

    // Calcul de rotation ultra-rapide
    private float[] calculateRotationFast(Vec3d target, Vec3d eyes) {
        Vec3d diff = target.subtract(eyes);

        double horizontalDistance = Math.sqrt(diff.x * diff.x + diff.z * diff.z);

        float yaw = (float) Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90.0F;
        float pitch = (float) -Math.toDegrees(Math.atan2(diff.y, horizontalDistance));

        // Normaliser le yaw
        while (yaw > 180.0F) yaw -= 360.0F;
        while (yaw < -180.0F) yaw += 360.0F;

        // Limiter le pitch
        pitch = MathHelper.clamp(pitch, -90.0F, 90.0F);

        return new float[]{yaw, pitch};
    }

    // Sélection de bloc rapide
    private boolean ensureBlockSelected() {
        if (mc.player == null) return false;

        // Vérifier le slot actuel
        if (doesSlotHaveBlocks(mc.player.getInventory().selectedSlot)) {
            return true;
        }

        // Trouver un slot avec des blocs
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getItem() instanceof net.minecraft.item.BlockItem && stack.getCount() > 0) {
                mc.player.getInventory().selectedSlot = i;
                return true;
            }
        }

        return false;
    }

    // Méthodes utilitaires optimisées
    public boolean isAirBlock(Block block) {
        if (block == null) return true;
        if (block.getDefaultState().isAir()) {
            if (block instanceof SnowBlock && block.getDefaultState().get(SnowBlock.LAYERS) > 1) {
                return false;
            }
            return true;
        }
        return false;
    }

    public boolean doesSlotHaveBlocks(int slotToCheck) {
        if (mc.player == null) return false;
        ItemStack stack = mc.player.getInventory().getStack(slotToCheck);
        return !stack.isEmpty() && stack.getItem() instanceof net.minecraft.item.BlockItem && stack.getCount() > 0;
    }

    public static Block getBlock(BlockPos pos) {
        if (MinecraftClient.getInstance().world == null) return null;
        return MinecraftClient.getInstance().world.getBlockState(pos).getBlock();
    }

    @Override
    public List<ModuleSetting> getSettings() {
        return settings;
    }

    @Override
    public void onSettingChanged(ModuleSetting setting) {
        if (mc.player == null) return;

        if (setting == range) {
            mc.player.sendMessage(
                    Text.literal("§6AutoClutch settings updated: Range=" + range.getDoubleValue() + " blocks"),
                    true);
            LOGGER.info("AutoClutch settings updated: Range={} blocks", range.getDoubleValue());
        } else if (setting == cpsCap) {
            String status = cpsCap.getBooleanValue() ? "enabled" : "disabled";
            mc.player.sendMessage(
                    Text.literal("§6AutoClutch settings updated: CPS Cap=" + status),
                    true);
            LOGGER.info("AutoClutch settings updated: CPS Cap={}", status);
        } else if (setting == holdMode) {
            String status = holdMode.getBooleanValue() ? "enabled" : "disabled";
            mc.player.sendMessage(
                    Text.literal("§6AutoClutch settings updated: Hold Mode=" + status),
                    true);
            LOGGER.info("AutoClutch settings updated: Hold Mode={}", status);
        }
    }
}