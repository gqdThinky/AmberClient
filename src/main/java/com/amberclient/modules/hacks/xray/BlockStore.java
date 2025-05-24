package com.amberclient.modules.hacks.xray;

import net.minecraft.block.Blocks;
import com.amberclient.modules.hacks.xray.BasicColor;
import com.amberclient.modules.hacks.xray.BlockSearchEntry;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class BlockStore {
    private static BlockStore instance;
    private final Set<BlockSearchEntry> cache;

    private BlockStore() {
        cache = new HashSet<>();
        // Default blocks to scan
        cache.add(new BlockSearchEntry(Blocks.DIAMOND_ORE.getDefaultState(), new BasicColor(51, 236, 255), true));
        cache.add(new BlockSearchEntry(Blocks.DEEPSLATE_DIAMOND_ORE.getDefaultState(), new BasicColor(51, 236, 255), true));
        cache.add(new BlockSearchEntry(Blocks.GOLD_ORE.getDefaultState(), new BasicColor(255, 252, 51), true));
        cache.add(new BlockSearchEntry(Blocks.DEEPSLATE_GOLD_ORE.getDefaultState(), new BasicColor(255, 252, 51), true));
        cache.add(new BlockSearchEntry(Blocks.IRON_ORE.getDefaultState(), new BasicColor(201, 201, 183), true));
        cache.add(new BlockSearchEntry(Blocks.DEEPSLATE_IRON_ORE.getDefaultState(), new BasicColor(201, 201, 183), true));
    }

    public static BlockStore getInstance() {
        if (instance == null) {
            instance = new BlockStore();
        }
        return instance;
    }

    public Cache<Set<BlockSearchEntry>> getCache() {
        return new Cache<>(Collections.unmodifiableSet(cache));
    }

    public static class Cache<T> {
        private final T cache;

        public Cache(T cache) {
            this.cache = cache;
        }

        public T get() {
            return cache;
        }
    }
}