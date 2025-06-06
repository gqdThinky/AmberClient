package com.amberclient.commands;

import com.amberclient.commands.impl.DummyCmd;
import com.amberclient.commands.impl.TopCmd;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class AmberCommand {
    public static void register() {
        // Server-side commands (top, dummy)
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                    CommandManager.literal("amber")
                            .then(CommandManager.literal("top")
                                    .executes(context -> {
                                        ServerCommandSource source = context.getSource();
                                        return TopCmd.teleportToTop(source);
                                    })
                            )
                            .then(CommandManager.literal("dummy")
                                    .executes(context -> {
                                        ServerCommandSource source = context.getSource();
                                        return DummyCmd.spawnDummy(source);
                                    })
                            )
            );
        });

        // Client-side commands (dataitem)
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                    ClientCommandManager.literal("amber")
                            .then(ClientCommandManager.literal("dataitem")
                                    .then(ClientCommandManager.argument("slot", IntegerArgumentType.integer(0, 40))
                                            .executes(context -> {
                                                FabricClientCommandSource source = context.getSource();
                                                int slot = IntegerArgumentType.getInteger(context, "slot");
                                                MinecraftClient client = MinecraftClient.getInstance();

                                                if (client.player != null && client.player.getInventory() != null) {
                                                    ItemStack itemStack = client.player.getInventory().getStack(slot);
                                                    if (!itemStack.isEmpty()) {
                                                        displayItemInfo(source, itemStack, slot);
                                                    } else {
                                                        source.sendFeedback(Text.literal("Slot " + slot + ": Empty").formatted(Formatting.GRAY));
                                                    }
                                                    return 1;
                                                } else {
                                                    source.sendError(Text.literal("Cannot access inventory: Not in game."));
                                                    return 0;
                                                }
                                            })
                                    )
                                    .executes(context -> {
                                        FabricClientCommandSource source = context.getSource();
                                        source.sendError(Text.literal("Usage: /amber dataitem <slot: 0-40>"));
                                        return 0;
                                    })
                            )
            );
        });
    }

    private static void displayItemInfo(FabricClientCommandSource source, ItemStack stack, int slot) {
        // Header
        source.sendFeedback(Text.literal("═══════════════════════════════════════").formatted(Formatting.AQUA));
        source.sendFeedback(Text.literal("Item Info - Slot " + slot).formatted(Formatting.GOLD, Formatting.BOLD));
        source.sendFeedback(Text.literal("═══════════════════════════════════════").formatted(Formatting.AQUA));

        // Basic item info
        String itemId = Registries.ITEM.getId(stack.getItem()).toString();
        source.sendFeedback(Text.literal("ID: ").formatted(Formatting.YELLOW)
                .append(Text.literal(itemId).formatted(Formatting.WHITE)));

        source.sendFeedback(Text.literal("Name: ").formatted(Formatting.YELLOW)
                .append(Text.literal(stack.getName().getString()).formatted(Formatting.WHITE)));

        source.sendFeedback(Text.literal("Count: ").formatted(Formatting.YELLOW)
                .append(Text.literal(String.valueOf(stack.getCount())).formatted(Formatting.WHITE)));

        // Durability info
        if (stack.isDamageable()) {
            int maxDamage = stack.getMaxDamage();
            int damage = stack.getDamage();
            int durabilityLeft = maxDamage - damage;
            source.sendFeedback(Text.literal("Durability: ").formatted(Formatting.YELLOW)
                    .append(Text.literal(durabilityLeft + "/" + maxDamage).formatted(Formatting.WHITE)));
        }

        if (stack.contains(DataComponentTypes.CUSTOM_MODEL_DATA)) {
            CustomModelDataComponent customModelData = stack.get(DataComponentTypes.CUSTOM_MODEL_DATA);
            if (customModelData != null) {
                Integer value = customModelData.getColor(0); // Assuming you want the first integer value
                if (value != null) {
                    source.sendFeedback(Text.literal("Custom Model Data: ").formatted(Formatting.YELLOW)
                            .append(Text.literal(String.valueOf(value)).formatted(Formatting.WHITE)));
                }
            }
        }

        // Lore
        if (stack.contains(DataComponentTypes.LORE)) {
            LoreComponent lore = stack.get(DataComponentTypes.LORE);
            if (lore != null && !lore.lines().isEmpty()) {
                source.sendFeedback(Text.literal("Lore:").formatted(Formatting.DARK_PURPLE));
                lore.lines().forEach(line -> {
                    source.sendFeedback(Text.literal("  ").append(line).formatted(Formatting.GRAY));
                });
            }
        }

        // Custom Name
        if (stack.contains(DataComponentTypes.CUSTOM_NAME)) {
            Text customName = stack.get(DataComponentTypes.CUSTOM_NAME);
            if (customName != null) {
                source.sendFeedback(Text.literal("Custom Name: ").formatted(Formatting.YELLOW)
                        .append(customName));
            }
        }

        // Display full NBT if available (for older items or special cases)
        try {
            NbtCompound nbt = new NbtCompound();
            stack.getItem().getComponents();

            // Try to get raw NBT data - this might work for some items
            String rawData = stack.toString();
            if (rawData.contains("{") && rawData.contains("}")) {
                source.sendFeedback(Text.literal("Raw Data:").formatted(Formatting.GRAY));
                source.sendFeedback(Text.literal(rawData).formatted(Formatting.DARK_GRAY));
            }
        } catch (Exception e) {
            // Silent fail - some items might not have accessible NBT
        }

        source.sendFeedback(Text.literal("═══════════════════════════════════════").formatted(Formatting.AQUA));
    }
}