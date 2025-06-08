package com.amberclient.utils.murdererfinder.config;

import com.google.gson.annotations.Expose;
import net.minecraft.item.*;
import net.minecraft.text.Text;
import net.minecraft.text.MutableText;
import net.minecraft.item.Item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class Config {
    public static final class MurderMystery {
        public enum InnocentHighlightOptions {
            NEVER(Text.translatable("config.generic.hypixel.mm.highlight.innocent.option.never")),
            AS_MURDER(Text.translatable("config.generic.hypixel.mm.highlight.innocent.option.as_murder")),
            ALWAYS(Text.translatable("config.generic.hypixel.mm.highlight.innocent.option.always"));

            private final MutableText text;
            InnocentHighlightOptions(MutableText text) {
                this.text = text;
            }
            public MutableText getText() {
                return text;
            }
        }
        public enum DetectiveHighlightOptions {
            NEVER(Text.translatable("config.generic.hypixel.mm.highlight.detective.option.never")),
            AS_MURDER(Text.translatable("config.generic.hypixel.mm.highlight.detective.option.as_murder")),
            ALWAYS(Text.translatable("config.generic.hypixel.mm.highlight.detective.option.always"));

            private final MutableText text;
            DetectiveHighlightOptions(MutableText text) {
                this.text = text;
            }
            public MutableText getText() {
                return text;
            }
        }

        @Expose public boolean highlightMurders = true;
        @Expose public InnocentHighlightOptions innocentHighlightOptions = InnocentHighlightOptions.AS_MURDER;
        @Expose public DetectiveHighlightOptions detectiveHighlightOptions = DetectiveHighlightOptions.ALWAYS;
        @Expose public boolean highlightGold = true;
        @Expose public boolean highlightBows = true;
        @Expose public boolean showNameTags = false;
        @Expose public boolean highlightSpectators = false;

        public void setInnocentHighlightOptions(InnocentHighlightOptions state) {
            innocentHighlightOptions = state;
        }
        public void setShowNameTags(boolean state) {
            showNameTags = state;
        }
        public void setHighlightGold(boolean state) {
            highlightGold = state;
        }
        public void setHighlightBows(boolean state) {
            highlightBows = state;
        }
        public void setHighlightSpectators(boolean state) {
            highlightSpectators = state;
        }

        public boolean shouldHighlightInnocents(boolean clientIsMurder) {
            return innocentHighlightOptions == InnocentHighlightOptions.ALWAYS ||
                    (innocentHighlightOptions == InnocentHighlightOptions.AS_MURDER && clientIsMurder);
        }
        public boolean shouldHighlightDetectives(boolean clientIsMurder) {
            return detectiveHighlightOptions == DetectiveHighlightOptions.ALWAYS ||
                    (detectiveHighlightOptions == DetectiveHighlightOptions.AS_MURDER && clientIsMurder);
        }
        public boolean shouldHighlightMurders() {
            return highlightMurders;
        }
        public boolean shouldHighlightGold() {
            return highlightGold;
        }
        public boolean shouldHighlightBows() {
            return highlightBows;
        }
        public boolean shouldShowNameTags() {
            return showNameTags;
        }
        public boolean shouldHighlightSpectators() {
            return highlightSpectators;
        }

        public boolean isMurderItem(Item item) {
            return item instanceof SwordItem || murderItems.contains(Item.getRawId(item)) ||
                    (item instanceof ShovelItem && item != Items.WOODEN_SHOVEL);
        }

        public boolean validate() {
            boolean valid = true;

            if (innocentHighlightOptions == null) {
                innocentHighlightOptions = InnocentHighlightOptions.NEVER;
                valid = false;
            }
            if (detectiveHighlightOptions == null) {
                detectiveHighlightOptions = DetectiveHighlightOptions.NEVER;
                valid = false;
            }

            return valid;
        }

        public static final int murderTeamColorValue = 0xFF1111;
        public static final int detectiveTeamColorValue = 0x15BFD6;
        public static final int goldTeamColorValue = 0xFFF126;
        public static final int bowTeamColorValue = 0x21E808;

        public static final ArrayList<Item> defaultMurderItems = new ArrayList<>(Arrays.asList(
                Items.IRON_SWORD, Items.ENDER_CHEST, Items.COOKED_CHICKEN, Items.BONE, Items.BLAZE_ROD, Items.NETHER_BRICK, Items.CARROT_ON_A_STICK,
                Items.STONE_SWORD, Items.SPONGE, Items.DEAD_BUSH, Items.OAK_BOAT, Items.GLISTERING_MELON_SLICE, Items.GOLDEN_PICKAXE,
                Items.COOKED_BEEF, Items.BOOK, Items.APPLE, Items.PRISMARINE_SHARD, Items.QUARTZ, Items.DIAMOND_SWORD, Items.NAME_TAG,
                Items.DIAMOND_SHOVEL, Items.ROSE_BUSH, Items.PUMPKIN_PIE, Items.DIAMOND_HOE, Items.CARROT, Items.RED_DYE, Items.SALMON,
                Items.SHEARS, Items.IRON_SHOVEL, Items.GOLDEN_CARROT, Items.WOODEN_SWORD, Items.STICK, Items.STONE_SHOVEL, Items.COOKIE,
                Items.DIAMOND_AXE, Items.GOLDEN_SWORD, Items.WOODEN_AXE, Items.SUGAR_CANE, Items.LEATHER, Items.CHARCOAL, Items.FLINT,
                Items.MUSIC_DISC_BLOCKS, Items.GOLDEN_HOE, Items.LAPIS_LAZULI, Items.BREAD, Items.JUNGLE_SAPLING, Items.GOLDEN_AXE,
                Items.DIAMOND_PICKAXE, Items.GOLDEN_SHOVEL
        ));
        public static HashSet<Integer> murderItems = getMurderItems();
        public static HashSet<Integer> getMurderItems() {
            HashSet<Integer> set = new HashSet<>();
            for (Item item : defaultMurderItems)
                set.add(Item.getRawId(item));
            return set;
        }
    }

    @Expose public boolean enabled = false;
    @Expose public MurderMystery mm = new MurderMystery();
    @Expose public boolean checkForUpdates = false;
    @Expose public boolean shownUpdateNotif = false;

    public boolean validate() {
        boolean valid = true;
        return valid && mm.validate();
    }
}
