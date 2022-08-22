package net.mca.forge.item;

import dev.architectury.registry.registries.RegistrySupplier;
import net.mca.entity.EntitiesMCA;
import net.mca.item.ItemsMCA;
import net.minecraft.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;

import static net.mca.item.ItemsMCA.baseProps;
import static net.mca.item.ItemsMCA.register;

public interface ItemsMCAForge {
    RegistrySupplier<Item> MALE_VILLAGER_SPAWN_EGG = register("male_villager_spawn_egg", () -> new ForgeSpawnEggItem(EntitiesMCA.MALE_VILLAGER, 0x5e9aff, 0x3366bc, baseProps()));
    RegistrySupplier<Item> FEMALE_VILLAGER_SPAWN_EGG = register("female_villager_spawn_egg", () -> new ForgeSpawnEggItem(EntitiesMCA.FEMALE_VILLAGER, 0xe85ca1, 0xe3368c, baseProps()));

    RegistrySupplier<Item> MALE_ZOMBIE_VILLAGER_SPAWN_EGG = register("male_zombie_villager_spawn_egg", () -> new ForgeSpawnEggItem(EntitiesMCA.MALE_ZOMBIE_VILLAGER, 0x5ebaff, 0x33a6bc, baseProps()));
    RegistrySupplier<Item> FEMALE_ZOMBIE_VILLAGER_SPAWN_EGG = register("female_zombie_villager_spawn_egg", () -> new ForgeSpawnEggItem(EntitiesMCA.FEMALE_ZOMBIE_VILLAGER, 0xe8aca1, 0xe3a68c, baseProps()));

    RegistrySupplier<Item> GRIM_REAPER_SPAWN_EGG = register("grim_reaper_spawn_egg", () -> new ForgeSpawnEggItem(EntitiesMCA.GRIM_REAPER, 0x301515, 0x2A1C34, baseProps()));

    static void bootstrap() {
        ItemsMCA.bootstrap();
    }
}
