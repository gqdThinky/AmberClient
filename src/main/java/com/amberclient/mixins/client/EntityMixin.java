package com.amberclient.mixins.client;

import com.amberclient.modules.movement.SafeWalk;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Entity.class)
public abstract class EntityMixin {

    /*
       ......SAFE WALK MODULE
     */

    @Shadow private World world;
    @Shadow public abstract boolean isOnGround();
    @Shadow public abstract boolean isSneaking();
    @Shadow public abstract double getX();
    @Shadow public abstract double getY();
    @Shadow public abstract double getZ();
    @Shadow public abstract Box getBoundingBox();
    @Shadow public abstract void setBoundingBox(Box box);

    @ModifyVariable(
            method = "move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V",
            at = @At(value = "HEAD"),
            argsOnly = true
    )
    private net.minecraft.util.math.Vec3d modifyMovementVector(net.minecraft.util.math.Vec3d movement) {
        if ((Entity) (Object) this instanceof PlayerEntity player && (SafeWalk.safewalk || isSneaking()) && isOnGround()) {
            double x = movement.x;
            double z = movement.z;
            double step = 0.05D;

            while (x != 0.0D && isSafeToMove(player, x, 0.0D)) {
                if (x < step && x >= -step) {
                    x = 0.0D;
                } else if (x > 0.0D) {
                    x -= step;
                } else {
                    x += step;
                }
            }

            while (z != 0.0D && isSafeToMove(player, 0.0D, z)) {
                if (z < step && z >= -step) {
                    z = 0.0D;
                } else if (z > 0.0D) {
                    z -= step;
                } else {
                    z += step;
                }
            }

            while (x != 0.0D && z != 0.0D && isSafeToMove(player, x, z)) {
                if (x < step && x >= -step) {
                    x = 0.0D;
                } else if (x > 0.0D) {
                    x -= step;
                } else {
                    x += step;
                }
                if (z < step && z >= -step) {
                    z = 0.0D;
                } else if (z > 0.0D) {
                    z -= step;
                } else {
                    z += step;
                }
            }

            return new net.minecraft.util.math.Vec3d(x, movement.y, z);
        }
        return movement;
    }

    @Unique
    private boolean isSafeToMove(PlayerEntity player, double x, double z) {
        double edgeDistance = SafeWalk.currentEdgeDistance;

        Box originalBox = getBoundingBox();
        Box offsetBox = originalBox.offset(x, -edgeDistance, z);

        double[] xCorners = {offsetBox.minX, offsetBox.maxX};
        double[] zCorners = {offsetBox.minZ, offsetBox.maxZ};

        for (double cornerX : xCorners) {
            for (double cornerZ : zCorners) {
                BlockPos pos = new BlockPos(
                        (int) Math.floor(cornerX),
                        (int) Math.floor(offsetBox.minY),
                        (int) Math.floor(cornerZ)
                );

                if (isFullBlockAtPosition(pos)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Unique
    private boolean isFullBlockAtPosition(BlockPos pos) {
        BlockState state = world.getBlockState(pos);

        if (state.isAir()) {
            return false;
        }

        VoxelShape shape = state.getCollisionShape(world, pos);

        if (shape.isEmpty()) {
            return false;
        }

        Box boundingBox = shape.getBoundingBox();

        boolean isFullHeight = boundingBox.getLengthY() >= 0.9;
        boolean isFullWidth = boundingBox.getLengthX() >= 0.9;
        boolean isFullDepth = boundingBox.getLengthZ() >= 0.9;

        boolean startsAtBottom = boundingBox.minY <= 0.1;

        return isFullHeight && isFullWidth && isFullDepth && startsAtBottom;
    }
}