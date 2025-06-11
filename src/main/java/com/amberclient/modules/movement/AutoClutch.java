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
import java.util.Objects;

public class AutoClutch extends Module implements ConfigurableModule {
    public static final String MOD_ID = "amberclient-autoclutch";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    // Settings
    private final List<ModuleSetting> settings;
    private final ModuleSetting range;
    private final ModuleSetting uncapCps;
    private final ModuleSetting cpsLimit;
    private final ModuleSetting holdMode;
    private final ModuleSetting rotationSpeed;
    private final ModuleSetting smartRotation;
    private final ModuleSetting humanizeRotations;

    protected MinecraftClient mc = getClient();
    private boolean wasKeyPressed = false;
    private long lastPlaceTime = 0;
    private long placeDelay = 50;

    private boolean hasTarget = false;

    public AutoClutch() {
        super("AutoClutch", "Automatically clutches (blocks might disappear sometimes). Can be use as diagonal scaffold", "Movement");

        // Initialize settings
        range = new ModuleSetting("Range", "Distance to search for block placement", 4.0, 1.0, 8.0, 1.0);
        rotationSpeed = new ModuleSetting("Rotation Speed", "Speed of rotation smoothing", 15.0, 1.0, 50.0, 1.0);
        cpsLimit = new ModuleSetting("CPS Limit", "Maximum clicks per second (if Uncap CPS is disabled)", 20.0, 1.0, 50.0, 1.0);
        uncapCps = new ModuleSetting("Uncap CPS", "Disable CPS limitation (for longer clutches)", true);
        holdMode = new ModuleSetting("Hold Mode", "Deactivate module when key is released", false);
        smartRotation = new ModuleSetting("Smart Rotation", "Intelligently rotate to closest placement", true);
        humanizeRotations = new ModuleSetting("Humanize Rotations", "Add human-like rotation variations", false);

        settings = new ArrayList<>();
        settings.add(range);
        settings.add(rotationSpeed);
        settings.add(cpsLimit);
        settings.add(uncapCps);
        settings.add(holdMode);
        settings.add(smartRotation);
        settings.add(humanizeRotations);

        // Calculate initial delay
        updatePlaceDelay();
    }

    @Override
    public void onEnable() {
        if (mc.player != null) { hasTarget = false; }
        LOGGER.info("{} module enabled", getName());
    }

    @Override
    public void onDisable() {
        if (mc.player != null) { hasTarget = false; }
        LOGGER.info("{} module disabled", getName());
    }

    @Override
    public void handleKeyInput() {
        boolean isKeyPressed = KeybindsManager.INSTANCE.getAutoClutchKey().isPressed();

        if (isKeyPressed) {
            if (!holdMode.getBooleanValue()) {
                if (!wasKeyPressed) toggle();
            } else {
                if (!isEnabled()) {
                    this.enabled = true;
                    onEnable();
                }
            }
        } else if (wasKeyPressed) {
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
            if (shouldPlaceBlock()) {
                PlacementInfo bestPlacement = findBestPlacement();

                if (bestPlacement != null) {
                    if (smartRotation.getBooleanValue()) {
                        updateRotation(bestPlacement.yaw, bestPlacement.pitch);
                    }

                    // Place block if CPS allows or uncap CPS is enabled
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
        return mc.player.getVelocity().y < 0 && isAirBlock(Objects.requireNonNull(getBlock(playerBlockPos.down())));
    }

    private PlacementInfo findBestPlacement() {
        if (mc.player == null || mc.world == null) return null;

        Vec3d playerPos = mc.player.getPos();
        BlockPos playerBlockPos = new BlockPos((int) playerPos.x, (int) playerPos.y, (int) playerPos.z);

        PlacementInfo bestPlacement = null;
        double closestDistance = Double.MAX_VALUE;

        int searchRange = (int) range.getDoubleValue();

        for (int y = -1; y >= -searchRange; y--) {
            BlockPos directBelow = playerBlockPos.add(0, y, 0);
            PlacementInfo placement = canPlaceAt(directBelow);
            if (placement != null) {
                return placement;
            }

            for (int radius = 1; radius <= searchRange; radius++) {
                for (int angle = 0; angle < 360; angle += 15) {
                    double radians = Math.toRadians(angle);
                    int x = (int) Math.round(radius * Math.cos(radians));
                    int z = (int) Math.round(radius * Math.sin(radians));

                    BlockPos targetPos = playerBlockPos.add(x, y, z);

                    double distance = playerPos.distanceTo(Vec3d.ofCenter(targetPos));
                    if (distance > searchRange) continue;

                    placement = canPlaceAt(targetPos);
                    if (placement != null && distance < closestDistance) {
                        closestDistance = distance;
                        bestPlacement = placement;
                    }
                }
            }

            if (bestPlacement != null) {
                return bestPlacement;
            }
        }

        return bestPlacement;
    }

    private boolean canPlaceBlock() {
        if (uncapCps.getBooleanValue()) {
            return true;
        }

        long currentTime = System.currentTimeMillis();
        return (currentTime - lastPlaceTime) >= placeDelay;
    }

    private void updatePlaceDelay() {
        if (!uncapCps.getBooleanValue()) {
            double cps = cpsLimit.getDoubleValue();
            placeDelay = (long) (1000.0 / cps);
        } else {
            placeDelay = 0;
        }
    }

    private PlacementInfo canPlaceAt(BlockPos pos) {
        if (mc.player == null || mc.world == null) return null;

        Vec3d playerPos = mc.player.getPos();
        if (pos.getY() >= (int) playerPos.y) return null;

        if (!isAirBlock(Objects.requireNonNull(getBlock(pos)))) return null;

        if (hasBlocks()) return null;

        Vec3d eyesPos = new Vec3d(mc.player.getX(),
                mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()),
                mc.player.getZ());

        // Find the best face to place against
        for (Direction side : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.DOWN}) {
            BlockPos neighbor = pos.offset(side);
            Direction placementSide = side.getOpposite();

            BlockState neighborState = mc.world.getBlockState(neighbor);
            if (!neighborState.isSolidBlock(mc.world, neighbor)) continue;

            // Calculate hit vector
            Vec3d hitVec = Vec3d.ofCenter(neighbor)
                    .add(Vec3d.of(placementSide.getVector()).multiply(0.5));

            if (eyesPos.squaredDistanceTo(hitVec) > 36.0) continue;

            float[] angles = calculateRotation(hitVec);

            return new PlacementInfo(pos, neighbor, placementSide, hitVec, angles[0], angles[1]);
        }

        return null;
    }

    private boolean placeBlockAt(PlacementInfo placement) {
        if (mc.player == null || hasBlocks()) return false;

        selectBlockInHotbar();

        mc.player.setYaw(placement.yaw);
        mc.player.setPitch(placement.pitch);

        BlockHitResult hitResult = new BlockHitResult(placement.hitVec, placement.side, placement.neighbor, false);
        assert mc.interactionManager != null;
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult);
        mc.player.swingHand(Hand.MAIN_HAND);

        return true;
    }

    private void updateRotation(float newYaw, float newPitch) {
        if (mc.player == null) return;

        float targetYaw = 0;
        float targetPitch = 0;
        if (!hasTarget) {
            targetYaw = newYaw;
            targetPitch = newPitch;
            hasTarget = true;
        } else {
            float speed = (float) rotationSpeed.getDoubleValue();

            // Humanize rotations if enabled
            if (humanizeRotations.getBooleanValue()) {

                float randomFactor = 0.7f + (float) (Math.random() * 0.6f);
                speed *= randomFactor;

                // Add micro-jitter to simulate human mouse movement
                float jitterYaw = (float) ((Math.random() - 0.5));
                float jitterPitch = (float) ((Math.random() - 0.5) * 0.6);

                newYaw += jitterYaw;
                newPitch += jitterPitch;
            }

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
        if (mc.player == null) return true;
        return getFirstHotBarSlotWithBlocks() == -1;
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
    private record PlacementInfo(BlockPos pos, BlockPos neighbor, Direction side, Vec3d hitVec, float yaw, float pitch) { }

    public boolean isAirBlock(Block block) {
        if (block.getDefaultState().isAir()) {
            return !(block instanceof SnowBlock) || block.getDefaultState().get(SnowBlock.LAYERS) <= 1;
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
        } else if (setting == uncapCps) {
            String status = uncapCps.getBooleanValue() ? "enabled" : "disabled";
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
        } else if (setting == humanizeRotations) {
            String status = humanizeRotations.getBooleanValue() ? "enabled" : "disabled";
            mc.player.sendMessage(
                    Text.literal("§6AutoClutch settings updated: Humanize Rotations=" + status),
                    true);
            LOGGER.info("AutoClutch settings updated: Humanize Rotations={}", status);
        }
    }
}