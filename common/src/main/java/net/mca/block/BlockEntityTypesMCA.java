package net.mca.block;

import com.google.common.collect.Lists;
import me.shedaniel.architectury.registry.DeferredRegister;
import me.shedaniel.architectury.registry.RegistrySupplier;
import net.mca.MCA;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;

import java.util.List;
import java.util.function.Supplier;

public interface BlockEntityTypesMCA {

    DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(MCA.MOD_ID, Registry.BLOCK_ENTITY_TYPE_KEY);

    RegistrySupplier<BlockEntityType<TombstoneBlock.Data>> TOMBSTONE = register("tombstone", TombstoneBlock.Data.constructor, Lists.newArrayList(
            BlocksMCA.GRAVELLING_HEADSTONE,
            BlocksMCA.UPRIGHT_HEADSTONE,
            BlocksMCA.SLANTED_HEADSTONE,
            BlocksMCA.CROSS_HEADSTONE,
            BlocksMCA.WALL_HEADSTONE
    ));

    static void bootstrap() {
        BLOCK_ENTITY_TYPES.register();
    }

    static <T extends BlockEntity> RegistrySupplier<BlockEntityType<T>> register(String name, Supplier<T> factory, List<RegistrySupplier<Block>> suppliers) {
        Identifier id = new Identifier(MCA.MOD_ID, name);
        return BLOCK_ENTITY_TYPES.register(id, () -> BlockEntityType.Builder.create(
                factory, suppliers.stream().map(RegistrySupplier::get).toArray(Block[]::new)
        ).build(Util.getChoiceType(TypeReferences.BLOCK_ENTITY, id.toString())));
    }
}
