package com.amberclient.modules.hacks.xray;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;

public class BlockPlaceListener {
    public static void init() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            // Vérifier si le monde est côté client et si le module XRay est actif
            if (world.isClient() && SettingsStore.getInstance().get().isActive()) {
                // Récupérer l'ItemStack de la main utilisée
                ItemStack itemStack = player.getStackInHand(hand);
                // Dériver le bloc à partir de l'item
                Block block = Block.getBlockFromItem(itemStack.getItem());
                if (block != null) {
                    // Obtenir l'état par défaut du bloc
                    BlockState defaultState = block.getDefaultState();
                    // Vérifier si l'état du bloc est dans le cache
                    if (BlockStore.getInstance().getCache().get().stream().anyMatch(e -> e.state() == defaultState)) {
                        ScanTask.runTask(true);
                    }
                }
            }
            // Laisser passer l'événement sans l'interrompre
            return ActionResult.PASS;
        });
    }
}