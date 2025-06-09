package com.amberclient.modules.movement;

import com.amberclient.utils.KeybindsManager;
import com.amberclient.utils.module.ConfigurableModule;
import com.amberclient.utils.module.Module;
import com.amberclient.utils.module.ModuleSetting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SnowBlock;
import net.minecraft.client.MinecraftClient;
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
    private final ModuleSetting cpsLimit;
    private final ModuleSetting holdMode;
    private final ModuleSetting rotationSpeed;
    private final ModuleSetting smartRotation;

    protected MinecraftClient mc = MinecraftClient.getInstance();
    private boolean wasKeyPressed = false;
    private long lastPlaceTime = 0;
    private long placeDelay = 50; // Dynamic delay based on CPS setting

    private boolean hasTarget = false;

    public AutoClutch() {
        super("AutoClutch", "R", "Movement");

        // Initialize settings
        range = new ModuleSetting("Range", "Distance to search for block placement (blocks)", 4.0, 1.0, 6.0, 1.0);
        cpsCap = new ModuleSetting("Uncap CPS", "Disable CPS limitation", true);
        cpsLimit = new ModuleSetting("CPS Limit", "Maximum clicks per second", 20.0, 1.0, 50.0, 1.0);
        holdMode = new ModuleSetting("Hold Mode", "Deactivate module when key is released", false);
        rotationSpeed = new ModuleSetting("Rotation Speed", "Speed of rotation smoothing", 15.0, 1.0, 50.0, 1.0);
        smartRotation = new ModuleSetting("Smart Rotation", "Intelligently rotate to closest placement", true);

        settings = new ArrayList<>();
        settings.add(range);
        settings.add(cpsCap);
        settings.add(cpsLimit);
        settings.add(holdMode);
        settings.add(rotationSpeed);
        settings.add(smartRotation);

        // Calculate initial delay
        updatePlaceDelay();
    }

    @Override
    public void onEnable() {
        if (mc.player != null) {
            float startYaw = mc.player.getYaw();
            float startPitch = mc.player.getPitch();
            hasTarget = false;
            mc.player.sendMessage(
                    Text.literal("§4[§cAmberClient§4] §c§l" + getName() + " §r§cactivated"), true);
        }
        LOGGER.info(getName() + " module enabled");
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.player.sendMessage(
                    Text.literal("§4[§cAmberClient§4] §c§l" + getName() + " §r§cdeactivated"), true);
        }
        hasTarget = false;
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
            // Check if we need to place blocks
            if (shouldPlaceBlock()) {
                // Find the best placement position
                PlacementInfo bestPlacement = findBestPlacement();

                if (bestPlacement != null) {
                    if (smartRotation.getBooleanValue()) {
                        // Smooth rotation to target
                        updateRotation(bestPlacement.yaw, bestPlacement.pitch);
                    }

                    // Place block if CPS allows or CPS cap is disabled
                    if (canPlaceBlock()) {
                        if (placeBlockAt(bestPlacement)) {
                            lastPlaceTime = System.currentTimeMillis();
                        }
                    }
                }
            }
        }
    }

    private boolean shouldPlaceBlock() {
        if (mc.player == null || mc.world == null) return false;

        Vec3d playerPos = mc.player.getPos();
        BlockPos playerBlockPos = new BlockPos((int) playerPos.x, (int) playerPos.y, (int) playerPos.z);

        // Check if player is falling and there's no block below
        return mc.player.getVelocity().y < 0 && isAirBlock(getBlock(playerBlockPos.down()));
    }

    private PlacementInfo findBestPlacement() {
        if (mc.player == null || mc.world == null) return null;

        Vec3d playerPos = mc.player.getPos();
        BlockPos playerBlockPos = new BlockPos((int) playerPos.x, (int) playerPos.y, (int) playerPos.z);

        PlacementInfo bestPlacement = null;
        double closestDistance = Double.MAX_VALUE;

        // Search in a circular pattern around the player, ONLY BELOW
        int searchRange = (int) range.getDoubleValue();

        // Commence par chercher directement sous le joueur
        for (int y = -1; y >= -searchRange; y--) {
            // Cherche d'abord directement en dessous
            BlockPos directBelow = playerBlockPos.add(0, y, 0);
            PlacementInfo placement = canPlaceAt(directBelow);
            if (placement != null) {
                return placement; // Priorité au placement direct en dessous
            }

            // Ensuite cherche dans un cercle autour du joueur à cette hauteur
            for (int radius = 1; radius <= searchRange; radius++) {
                for (int angle = 0; angle < 360; angle += 15) { // Vérifie tous les 15 degrés pour un bon coverage
                    double radians = Math.toRadians(angle);
                    int x = (int) Math.round(radius * Math.cos(radians));
                    int z = (int) Math.round(radius * Math.sin(radians));

                    BlockPos targetPos = playerBlockPos.add(x, y, z);

                    // Vérifie la distance réelle
                    double distance = playerPos.distanceTo(Vec3d.ofCenter(targetPos));
                    if (distance > searchRange) continue;

                    // Check if this position is good for placement
                    placement = canPlaceAt(targetPos);
                    if (placement != null && distance < closestDistance) {
                        closestDistance = distance;
                        bestPlacement = placement;
                    }
                }
            }

            // Si on a trouvé un placement à cette hauteur, on le retourne
            if (bestPlacement != null) {
                return bestPlacement;
            }
        }

        return bestPlacement;
    }

    private boolean canPlaceBlock() {
        if (cpsCap.getBooleanValue()) {
            return true; // No CPS limitation
        }

        long currentTime = System.currentTimeMillis();
        return (currentTime - lastPlaceTime) >= placeDelay;
    }

    private void updatePlaceDelay() {
        if (!cpsCap.getBooleanValue()) {
            double cps = cpsLimit.getDoubleValue();
            placeDelay = (long) (1000.0 / cps); // Convert CPS to milliseconds delay
        } else {
            placeDelay = 0; // No delay when CPS cap is disabled
        }
    }

    private PlacementInfo canPlaceAt(BlockPos pos) {
        if (mc.player == null || mc.world == null) return null;

        // Vérifier que la position est bien en dessous du joueur
        Vec3d playerPos = mc.player.getPos();
        if (pos.getY() >= (int) playerPos.y) return null;

        // Check if position is air
        if (!isAirBlock(getBlock(pos))) return null;

        // Check if we have blocks to place
        if (!hasBlocks()) return null;

        Vec3d eyesPos = new Vec3d(mc.player.getX(),
                mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()),
                mc.player.getZ());

        // Find the best face to place against
        for (Direction side : Direction.values()) {
            BlockPos neighbor = pos.offset(side);
            Direction placementSide = side.getOpposite();

            // Check if neighbor is solid
            BlockState neighborState = mc.world.getBlockState(neighbor);
            if (!neighborState.isSolidBlock(mc.world, neighbor)) continue;

            // Calculate hit vector
            Vec3d hitVec = Vec3d.ofCenter(neighbor)
                    .add(Vec3d.of(placementSide.getVector()).multiply(0.5));

            // Check if within reach
            if (eyesPos.squaredDistanceTo(hitVec) > 36.0) continue;

            // Calculate rotation needed
            float[] angles = calculateRotation(hitVec);

            return new PlacementInfo(pos, neighbor, placementSide, hitVec, angles[0], angles[1]);
        }

        return null;
    }

    private boolean placeBlockAt(PlacementInfo placement) {
        if (mc.player == null || !hasBlocks()) return false;

        // Select block in hotbar
        selectBlockInHotbar();

        // Set rotation
        mc.player.setYaw(placement.yaw);
        mc.player.setPitch(placement.pitch);

        // Place block
        BlockHitResult hitResult = new BlockHitResult(placement.hitVec, placement.side, placement.neighbor, false);
        assert mc.interactionManager != null;
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult);
        mc.player.swingHand(Hand.MAIN_HAND);

        return true;
    }

    private void updateRotation(float newYaw, float newPitch) {
        if (mc.player == null) return;

        // Rotation smoothing
        float targetYaw = 0;
        float targetPitch = 0;
        if (!hasTarget) {
            targetYaw = newYaw;
            targetPitch = newPitch;
            hasTarget = true;
        } else {
            // Smooth rotation
            float speed = (float) rotationSpeed.getDoubleValue();

            float yawDiff = MathHelper.wrapDegrees(newYaw - mc.player.getYaw());
            float pitchDiff = newPitch - mc.player.getPitch();

            targetYaw = mc.player.getYaw() + Math.signum(yawDiff) * Math.min(Math.abs(yawDiff), speed);
            targetPitch = mc.player.getPitch() + Math.signum(pitchDiff) * Math.min(Math.abs(pitchDiff), speed);
        }

        mc.player.setYaw(targetYaw);
        mc.player.setPitch(MathHelper.clamp(targetPitch, -90.0F, 90.0F));
    }

    private float[] calculateRotation(Vec3d target) {
        if (mc.player == null) return new float[]{0, 0};

        Vec3d eyesPos = new Vec3d(mc.player.getX(),
                mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()),
                mc.player.getZ());

        Vec3d diff = target.subtract(eyesPos);

        double horizontalDistance = Math.sqrt(diff.x * diff.x + diff.z * diff.z);

        float yaw = (float) Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90.0F;
        float pitch = (float) -Math.toDegrees(Math.atan2(diff.y, horizontalDistance));

        return new float[]{MathHelper.wrapDegrees(yaw), MathHelper.clamp(pitch, -90.0F, 90.0F)};
    }

    private boolean hasBlocks() {
        if (mc.player == null) return false;
        return getFirstHotBarSlotWithBlocks() != -1;
    }

    private void selectBlockInHotbar() {
        if (mc.player == null) return;

        if (!doesSlotHaveBlocks(mc.player.getInventory().selectedSlot)) {
            int blockSlot = getFirstHotBarSlotWithBlocks();
            if (blockSlot != -1) {
                mc.player.getInventory().selectedSlot = blockSlot;
            }
        }
    }

    // Utility classes
    private static class PlacementInfo {
        final BlockPos pos;
        final BlockPos neighbor;
        final Direction side;
        final Vec3d hitVec;
        final float yaw;
        final float pitch;

        PlacementInfo(BlockPos pos, BlockPos neighbor, Direction side, Vec3d hitVec, float yaw, float pitch) {
            this.pos = pos;
            this.neighbor = neighbor;
            this.side = side;
            this.hitVec = hitVec;
            this.yaw = yaw;
            this.pitch = pitch;
        }
    }

    // Keep existing utility methods
    public boolean isAirBlock(Block block) {
        if (block.getDefaultState().isAir()) {
            if (block instanceof SnowBlock && block.getDefaultState().get(SnowBlock.LAYERS) > 1) {
                return false;
            }
            return true;
        }
        return false;
    }

    public int getFirstHotBarSlotWithBlocks() {
        if (mc.player == null) return -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getItem() instanceof net.minecraft.item.BlockItem) {
                return i;
            }
        }
        return -1;
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
            updatePlaceDelay();
            mc.player.sendMessage(
                    Text.literal("§6AutoClutch settings updated: CPS Cap=" + status),
                    true);
            LOGGER.info("AutoClutch settings updated: CPS Cap={}", status);
        } else if (setting == cpsLimit) {
            updatePlaceDelay();
            mc.player.sendMessage(
                    Text.literal("§6AutoClutch settings updated: CPS Limit=" + cpsLimit.getDoubleValue()),
                    true);
            LOGGER.info("AutoClutch settings updated: CPS Limit={}", cpsLimit.getDoubleValue());
        } else if (setting == holdMode) {
            String status = holdMode.getBooleanValue() ? "enabled" : "disabled";
            mc.player.sendMessage(
                    Text.literal("§6AutoClutch settings updated: Hold Mode=" + status),
                    true);
            LOGGER.info("AutoClutch settings updated: Hold Mode={}", status);
        } else if (setting == rotationSpeed) {
            mc.player.sendMessage(
                    Text.literal("§6AutoClutch settings updated: Rotation Speed=" + rotationSpeed.getDoubleValue()),
                    true);
            LOGGER.info("AutoClutch settings updated: Rotation Speed={}", rotationSpeed.getDoubleValue());
        } else if (setting == smartRotation) {
            String status = smartRotation.getBooleanValue() ? "enabled" : "disabled";
            mc.player.sendMessage(
                    Text.literal("§6AutoClutch settings updated: Smart Rotation=" + status),
                    true);
            LOGGER.info("AutoClutch settings updated: Smart Rotation={}", status);
        }
    }
}