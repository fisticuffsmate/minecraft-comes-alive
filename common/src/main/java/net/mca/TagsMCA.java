package net.mca;

import dev.architectury.hooks.tags.TagHooks;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;

public interface TagsMCA {
    interface Blocks {
        Tag.Identified<Block> TOMBSTONES = register("tombstones");

        static void bootstrap() {}

        static Tag.Identified<Block> register(String path) {
            return TagHooks.optionalBlock(new Identifier(MCA.MOD_ID, path));
        }
    }

    interface Items {
        Tag.Identified<Item> VILLAGER_EGGS = register("villager_eggs");
        Tag.Identified<Item> ZOMBIE_EGGS = register("zombie_eggs");
        Tag.Identified<Item> VILLAGER_PLANTABLE = register("villager_plantable");

        Tag.Identified<Item> BABIES = register("babies");

        static void bootstrap() {}

        static Tag.Identified<Item> register(String path) {
            return TagHooks.optionalItem(new Identifier(MCA.MOD_ID, path));
        }
    }
}
