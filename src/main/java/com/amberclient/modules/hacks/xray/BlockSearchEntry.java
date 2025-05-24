package com.amberclient.modules.hacks.xray;

import net.minecraft.block.BlockState;

public record BlockSearchEntry(BlockState state, BasicColor color, boolean isDefault) {
}