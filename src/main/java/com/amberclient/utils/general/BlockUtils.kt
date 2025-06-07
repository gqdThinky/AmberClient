
package com.amberclient.utils.general

import net.minecraft.block.*
import net.minecraft.client.MinecraftClient
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.World


 // Utility class for block operations in AmberClient.

object BlockUtils {

    /**
     * Checks whether the block at the given position is unbreakable.
     *
     * Unbreakable blocks, such as bedrock, have a hardness of less than 0.
     * This method returns true if the block is unbreakable, false otherwise.
     *
     * @param pos The position of the block to be checked.
     * @return true if the block is unbreakable, false otherwise.
     */
    fun isUnbreakable(pos: BlockPos): Boolean {
        val world: World? = MinecraftClient.getInstance().world
        if (world == null) { return false }

        return world.getBlockState(pos).getHardness(world, pos) < 0
    }

    // Placing

    /**
     * Places a block at the specified position using the given hand and slot.
     *
     * @param blockPos The position to place the block.
     * @param hand The hand to use (MAIN_HAND or OFF_HAND).
     * @param slot The inventory slot containing the block item (0-8 for hotbar).
     * @param rotate Whether to rotate the player to face the block.
     * @param swing Whether to swing the hand after placing.
     * @return true if the block was placed successfully, false otherwise.
     */
    fun place(blockPos: BlockPos, hand: Hand, slot: Int, rotate: Boolean, swing: Boolean): Boolean {
        val mc = MinecraftClient.getInstance()
        if (slot < 0 || slot > 8) return false

        val stack: ItemStack = if (hand == Hand.MAIN_HAND) mc.player!!.inventory.getStack(slot) else mc.player!!.offHandStack
        if (stack.item !is BlockItem) return false
        val toPlace: Block = (stack.item as BlockItem).block

        if (!canPlaceBlock(blockPos, true, toPlace)) return false

        var hitPos: Vec3d = Vec3d.ofCenter(blockPos)
        val side: Direction? = getPlaceSide(blockPos)
        val neighbour: BlockPos
        val actualSide: Direction
        if (side == null) {
            actualSide = Direction.UP
            neighbour = blockPos
        } else {
            actualSide = side
            neighbour = blockPos.offset(side)
            hitPos = hitPos.add(side.offsetX * 0.5, side.offsetY * 0.5, side.offsetZ * 0.5)
        }

        val bhr = BlockHitResult(hitPos, actualSide.opposite, neighbour, false)

        if (rotate) {
            val yaw: Float = getYaw(hitPos)
            val pitch: Float = getPitch(hitPos)
            mc.player!!.yaw = yaw.toFloat()
            mc.player!!.pitch = pitch.toFloat()
        }

        val prevSlot: Int = mc.player!!.inventory.selectedSlot
        if (hand == Hand.MAIN_HAND) {
            mc.player!!.inventory.selectedSlot = slot
        }

        interact(bhr, hand, swing)

        if (hand == Hand.MAIN_HAND) {
            mc.player!!.inventory.selectedSlot = prevSlot
        }

        return true
    }

    /**
     * Interacts with a block at the specified hit result.
     *
     * @param blockHitResult The block hit result.
     * @param hand The hand to use.
     * @param swing Whether to swing the hand.
     */
    fun interact(blockHitResult: BlockHitResult, hand: Hand, swing: Boolean) {
        val mc = MinecraftClient.getInstance()
        val wasSneaking: Boolean = mc.player!!.isSneaking
        mc.player!!.isSneaking = false

        val result: ActionResult = mc.interactionManager!!.interactBlock(mc.player, hand, blockHitResult)

        if (result.isAccepted) {
            if (swing) mc.player!!.swingHand(hand)
            else mc.networkHandler!!.sendPacket(HandSwingC2SPacket(hand))
        }

        mc.player!!.isSneaking = wasSneaking
    }

    /**
     * Checks if a block can be placed at the specified position.
     *
     * @param blockPos The position to check.
     * @param checkEntities Whether to check for entity collisions.
     * @param block The block to place.
     * @return true if the block can be placed, false otherwise.
     */
    fun canPlaceBlock(blockPos: BlockPos, checkEntities: Boolean, block: Block): Boolean {
        val mc = MinecraftClient.getInstance()
        if (mc.world == null) return false

        if (!World.isValid(blockPos)) return false

        if (!mc.world!!.getBlockState(blockPos).isReplaceable) return false

        return !checkEntities || mc.world!!.canPlace(block.defaultState, blockPos, ShapeContext.absent())
    }

    /**
     * Gets the side of the block to place against.
     *
     * @param blockPos The position of the block to place.
     * @return The direction to place against, or null if no suitable side is found.
     */
    fun getPlaceSide(blockPos: BlockPos): Direction? {
        val mc = MinecraftClient.getInstance()
        val lookVec: Vec3d = blockPos.toCenterPos().subtract(mc.player!!.eyePos)
        var bestRelevancy = -Double.MAX_VALUE
        var bestSide: Direction? = null

        for (side in Direction.values()) {
            val neighbor: BlockPos = blockPos.offset(side)
            val state: BlockState = mc.world!!.getBlockState(neighbor)

            if (state.isAir || isClickable(state.block)) continue

            if (!state.fluidState.isEmpty) continue

            val relevancy: Double = side.axis.choose(lookVec.x, lookVec.y, lookVec.z) * side.direction.offset()
            if (relevancy > bestRelevancy) {
                bestRelevancy = relevancy
                bestSide = side
            }
        }

        return bestSide
    }

    // Breaking

    /**
     * Breaks a block at the specified position.
     *
     * @param blockPos The position of the block to break.
     * @param swing Whether to swing the hand.
     * @return true if the block breaking was initiated, false otherwise.
     */
    fun breakBlock(blockPos: BlockPos, swing: Boolean): Boolean {
        val mc = MinecraftClient.getInstance()
        if (!canBreak(blockPos)) return false

        val direction: Direction = getDirection(blockPos)
        mc.interactionManager!!.attackBlock(blockPos, direction)

        if (swing) mc.player!!.swingHand(Hand.MAIN_HAND)
        else mc.networkHandler!!.sendPacket(HandSwingC2SPacket(Hand.MAIN_HAND))

        return true
    }

    /**
     * Checks if a block can be broken.
     *
     * @param blockPos The position of the block.
     * @return true if the block can be broken, false otherwise.
     */
    fun canBreak(blockPos: BlockPos): Boolean {
        val mc = MinecraftClient.getInstance()
        if (mc.world == null) return false
        val state: BlockState = mc.world!!.getBlockState(blockPos)
        return state.getHardness(mc.world, blockPos) >= 0 && state.getOutlineShape(mc.world, blockPos) != VoxelShapes.empty()
    }

    // Utilities

    /**
     * Checks if a block is clickable (interactive).
     *
     * @param block The block to check.
     * @return true if the block is clickable, false otherwise.
     */
    fun isClickable(block: Block): Boolean {
        return block is CraftingTableBlock
                || block is AnvilBlock
                || block is LoomBlock
                || block is CartographyTableBlock
                || block is GrindstoneBlock
                || block is StonecutterBlock
                || block is ButtonBlock
                || block is AbstractPressurePlateBlock
                || block is BlockWithEntity
                || block is BedBlock
                || block is FenceGateBlock
                || block is DoorBlock
                || block is NoteBlock
                || block is TrapdoorBlock
    }

    /**
     * Gets the best direction to interact with a block.
     *
     * @param pos The position of the block.
     * @return The direction to face.
     */
    fun getDirection(pos: BlockPos): Direction {
        val mc = MinecraftClient.getInstance()
        val eyesPos = Vec3d(mc.player!!.x, mc.player!!.y + mc.player!!.getEyeHeight(mc.player!!.pose), mc.player!!.z)
        if (pos.y.toDouble() > eyesPos.y) {
            if (mc.world!!.getBlockState(pos.add(0, -1, 0)).isReplaceable) return Direction.DOWN
            else return mc.player!!.horizontalFacing.opposite
        }
        if (!mc.world!!.getBlockState(pos.add(0, 1, 0)).isReplaceable) return mc.player!!.horizontalFacing.opposite
        return Direction.UP
    }

    /**
     * Calculates the yaw to face a target position.
     *
     * @param target The target position.
     * @return The yaw angle in degrees.
     */
    fun getYaw(target: Vec3d): Float {
        val mc = MinecraftClient.getInstance()
        val diffX: Double = target.x - mc.player!!.x
        val diffZ: Double = target.z - mc.player!!.z
        return mc.player!!.yaw + MathHelper.wrapDegrees((Math.toDegrees(kotlin.math.atan2(diffZ, diffX)) - 90).toFloat() - mc.player!!.yaw)
    }

    /**
     * Calculates the pitch to face a target position.
     *
     * @param target The target position.
     * @return The pitch angle in degrees.
     */
    fun getPitch(target: Vec3d): Float {
        val mc = MinecraftClient.getInstance()
        val diffX: Double = target.x - mc.player!!.x
        val diffY: Double = target.y - (mc.player!!.y + mc.player!!.getEyeHeight(mc.player!!.pose))
        val diffZ: Double = target.z - mc.player!!.z
        val diffXZ: Double = kotlin.math.sqrt(diffX * diffX + diffZ * diffZ)
        return mc.player!!.pitch + MathHelper.wrapDegrees((-Math.toDegrees(kotlin.math.atan2(diffY, diffXZ))).toFloat() - mc.player!!.pitch)
    }
}

