package com.amberclient.utils.murdererfinder;

import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.floats.FloatBigArrayBigList;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

public class MinecraftUtils {
    public static void sendChatMessage(Text message) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc != null && mc.inGameHud != null)
            mc.inGameHud.getChatHud().addMessage(message);
    }
    public static void sendChatMessage(String message) {
        sendChatMessage(Text.literal(message));
    }

    public static boolean isPlayerInTabList(PlayerEntity player) {
        return isPlayerInTabList(player.getGameProfile());
    }
    public static boolean isPlayerInTabList(GameProfile profile) {
        if (MinecraftClient.getInstance().player != null) {
            String name = profile.getName();
            for (PlayerListEntry entry : MinecraftClient.getInstance().player.networkHandler.getPlayerList())
                if (entry.getProfile().getName().equals(name))
                    return true;
        }
        return false;
    }

    public static ModMetadata getModMetadata(String modId) {
        ModContainer container = FabricLoader.getInstance().getModContainer(modId).get();
        return container.getMetadata();
    }
}
