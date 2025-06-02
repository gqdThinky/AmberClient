package com.amberclient.modules.render.xray;

import net.minecraft.block.BlockState;

public record BlockSearchEntry(BlockState state, BasicColor color, boolean isDefault) {
}