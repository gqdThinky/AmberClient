package com.amberclient.modules.xray;

import net.minecraft.block.BlockState;

public record BlockSearchEntry(BlockState state, BasicColor color, boolean isDefault) {
}