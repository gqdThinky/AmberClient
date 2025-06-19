package com.amberclient.modules.render.xray;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.block.BlockState;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class ScanTask implements Runnable {
    public static Set<BlockPosWithColor> renderQueue = Collections.synchronizedSet(new HashSet<>());
    private static AtomicBoolean isScanning = new AtomicBoolean(false);
    private static ChunkPos playerLastChunk;
    private final boolean fullScan;
    private final ChunkPos singleChunk;

    public ScanTask() {
        this.fullScan = true;
        this.singleChunk = null;
    }

    public ScanTask(ChunkPos chunkPos) {
        this.fullScan = false;
        this.singleChunk = chunkPos;
    }

    public static void runTask(boolean forceRerun) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null || !SettingsStore.getInstance().get().isActive()) {
            return;
        }

        if (!forceRerun && !playerLocationChanged(client.player)) {
            return;
        }

        playerLastChunk = client.player.getChunkPos();
        client.execute(new ScanTask());
    }

    public static void runTaskForSingleChunk(ChunkPos chunkPos) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null || !SettingsStore.getInstance().get().isActive()) {
            return;
        }

        if (!client.world.isChunkLoaded(chunkPos.x, chunkPos.z)) {
            return;
        }

        client.execute(new ScanTask(chunkPos));
    }

    public static void blockBroken(World world, PlayerEntity player, BlockPos blockPos, BlockState blockState, BlockEntity blockEntity) {
        if (!(player instanceof ClientPlayerEntity)) return;
        ClientPlayerEntity clientPlayer = (ClientPlayerEntity) player;
        if (!SettingsStore.getInstance().get().isActive()) return;
        if (renderQueue.stream().anyMatch(e -> e.pos().equals(blockPos))) {
            runTask(true);
        }
    }

    private static boolean playerLocationChanged(ClientPlayerEntity player) {
        ChunkPos plyChunkPos = player.getChunkPos();
        int range = SettingsStore.getInstance().get().getHalfRange();
        return playerLastChunk == null ||
                plyChunkPos.x > playerLastChunk.x + range || plyChunkPos.x < playerLastChunk.x - range ||
                plyChunkPos.z > playerLastChunk.z + range || plyChunkPos.z < playerLastChunk.z - range;
    }

    @Override
    public void run() {
        if (isScanning.get()) {
            return;
        }

        isScanning.set(true);
        Set<BlockPosWithColor> blocks = fullScan ? collectBlocks() : collectBlocksForSingleChunk();
        renderQueue.clear();
        renderQueue.addAll(blocks);
        isScanning.set(false);
        RenderOutlines.requestedRefresh.set(true);
    }

    private Set<BlockPosWithColor> collectBlocks() {
        Set<BlockSearchEntry> blocks = BlockStore.getInstance().getCache().get();
        if (blocks.isEmpty()) {
            return new HashSet<>();
        }

        MinecraftClient instance = MinecraftClient.getInstance();
        final World world = instance.world;
        final ClientPlayerEntity player = instance.player;

        if (world == null || player == null) {
            return new HashSet<>();
        }

        final Set<BlockPosWithColor> renderQueue = new HashSet<>();
        int cX = player.getChunkPos().x;
        int cZ = player.getChunkPos().z;
        int range = SettingsStore.getInstance().get().getHalfRange();
        boolean exposedOnly = SettingsStore.getInstance().get().isExposedOnly();

        for (int i = cX - range; i <= cX + range; i++) {
            for (int j = cZ - range; j <= cZ + range; j++) {
                if (!world.isChunkLoaded(i, j)) {
                    continue;
                }
                int chunkStartX = i << 4;
                int chunkStartZ = j << 4;
                for (int k = chunkStartX; k < chunkStartX + 16; k++) {
                    for (int l = chunkStartZ; l < chunkStartZ + 16; l++) {
                        int topY = world.getTopY(Heightmap.Type.WORLD_SURFACE, k, l);
                        for (int m = world.getBottomY(); m < topY; m++) {
                            BlockPos pos = new BlockPos(k, m, l);
                            BasicColor color = isValidBlock(pos, world, blocks);
                            if (color != null) {
                                if (!exposedOnly || isBlockExposedAndNearSurface(pos, world)) {
                                    renderQueue.add(new BlockPosWithColor(pos, color));
                                }
                            }
                        }
                    }
                }
            }
        }

        return renderQueue;
    }

    private Set<BlockPosWithColor> collectBlocksForSingleChunk() {
        Set<BlockSearchEntry> blocks = BlockStore.getInstance().getCache().get();
        if (blocks.isEmpty()) {
            return new HashSet<>();
        }

        MinecraftClient instance = MinecraftClient.getInstance();
        final World world = instance.world;
        final ClientPlayerEntity player = instance.player;

        if (world == null || player == null) {
            return new HashSet<>();
        }

        final Set<BlockPosWithColor> renderQueue = new HashSet<>();
        int chunkStartX = singleChunk.x << 4;
        int chunkStartZ = singleChunk.z << 4;
        boolean exposedOnly = SettingsStore.getInstance().get().isExposedOnly();

        for (int k = chunkStartX; k < chunkStartX + 16; k++) {
            for (int l = chunkStartZ; l < chunkStartZ + 16; l++) {
                int topY = world.getTopY(Heightmap.Type.WORLD_SURFACE, k, l);
                for (int m = world.getBottomY(); m < topY; m++) {
                    BlockPos pos = new BlockPos(k, m, l);
                    BasicColor color = isValidBlock(pos, world, blocks);
                    if (color != null) {
                        if (!exposedOnly || isBlockExposedAndNearSurface(pos, world)) {
                            renderQueue.add(new BlockPosWithColor(pos, color));
                        }
                    }
                }
            }
        }

        return renderQueue;
    }

    private BasicColor isValidBlock(BlockPos pos, World world, Set<BlockSearchEntry> blocks) {
        BlockState state = world.getBlockState(pos);
        if (state.isAir()) {
            return null;
        }

        BlockState defaultState = state.getBlock().getDefaultState();
        BasicColor color = blocks.stream()
                .filter(localState -> localState.isDefault() && defaultState == localState.state() ||
                        !localState.isDefault() && state == localState.state())
                .findFirst()
                .map(BlockSearchEntry::color)
                .orElse(null);

        return color;
    }

    private boolean isBlockExposed(BlockPos pos, World world) {
        BlockPos[] adjacentPositions = {
                pos.up(),    // Y+1
                pos.down(),  // Y-1
                pos.north(), // Z-1
                pos.south(), // Z+1
                pos.east(),  // X+1
                pos.west()   // X-1
        };

        for (BlockPos adjacentPos : adjacentPositions) {
            BlockState adjacentState = world.getBlockState(adjacentPos);
            if (adjacentState.isAir() || !adjacentState.isOpaque()) {
                return true;
            }
        }

        return false;
    }

    private boolean isBlockNearSurface(BlockPos pos, World world) {
        int surfaceY = world.getTopY(Heightmap.Type.WORLD_SURFACE, pos.getX(), pos.getZ());
        int maxDepth = 10;

        return pos.getY() >= (surfaceY - maxDepth);
    }

    private boolean isBlockExposedAndNearSurface(BlockPos pos, World world) {
        if (!isBlockExposed(pos, world)) {
            return false;
        }

        int surfaceY = world.getTopY(Heightmap.Type.WORLD_SURFACE, pos.getX(), pos.getZ());
        int maxDepthFromSurface = 15;

        return pos.getY() >= (surfaceY - maxDepthFromSurface);
    }

    private boolean isBlockInLargeAirPocket(BlockPos pos, World world) {
        int airBlockCount = 0;
        int totalBlocksChecked = 0;
        int radius = 2;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;

                    BlockPos checkPos = pos.add(x, y, z);
                    BlockState state = world.getBlockState(checkPos);

                    if (state.isAir()) {
                        airBlockCount++;
                    }
                    totalBlocksChecked++;
                }
            }
        }

        return (airBlockCount / (double) totalBlocksChecked) > 0.3;
    }
}