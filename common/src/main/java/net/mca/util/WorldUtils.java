package net.mca.util;

import net.mca.MCA;
import net.mca.util.compat.PersistentStateCompat;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.PersistentState;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.StructureFeature;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public interface WorldUtils {
    static List<Entity> getCloseEntities(World world, Entity e) {
        return getCloseEntities(world, e, 256.0);
    }

    static List<Entity> getCloseEntities(World world, Entity e, double range) {
        Vec3d pos = e.getPos();
        return world.getOtherEntities(e, new Box(pos, pos).expand(range));
    }

    static <T extends Entity> List<T> getCloseEntities(World world, Entity e, Class<T> c) {
        return getCloseEntities(world, e, 256.0, c);
    }

    static <T extends Entity> List<T> getCloseEntities(World world, Entity e, double range, Class<T> c) {
        return getCloseEntities(world, e.getPos(), range, c);
    }

    static <T extends Entity> List<T> getCloseEntities(World world, Vec3d pos, double range, Class<T> c) {
        return world.getNonSpectatingEntities(c, new Box(pos, pos).expand(range));
    }

    static <T extends PersistentStateCompat> T loadData(ServerWorld world, Function<NbtCompound, T> loader, Function<ServerWorld, T> factory, String dataId) {
        return world.getPersistentStateManager().getOrCreate(() -> {
            return new PersistentState(dataId) {
                private T obj;

                @Override
                public void fromTag(NbtCompound tag) {
                    obj = loader.apply(tag);
                    obj.attach(this);
                }

                @Override
                public NbtCompound writeNbt(NbtCompound nbt) {
                    return get().writeNbt(nbt);
                }

                public T get() {
                    if (obj == null) {
                        obj = factory.apply(world);
                        obj.attach(this);
                    }
                    return obj;
                }
            };
        }, dataId).get();
    }

    static void spawnEntity(World world, MobEntity entity, SpawnReason reason) {
        entity.initialize((ServerWorldAccess) world, world.getLocalDifficulty(entity.getBlockPos()), reason, null, null);
        world.spawnEntity(entity);
    }

    //a wrapper for the unnecessary complex query provided by minecraft
    static Optional<BlockPos> getClosestStructurePosition(ServerWorld world, BlockPos center, Identifier structure, int radius) {
        StructureFeature<?> feature = Registry.STRUCTURE_FEATURE.get(structure);
        if (feature != null) {
            BlockPos pos = world.getChunkManager().getChunkGenerator().locateStructure(world, feature, center, radius, false);
            return pos == null ? Optional.empty() : Optional.of(pos);
        } else {
            // 1.16.5 ONLY Logic
            if (structure.getPath().contains("_")) {
                String name = structure.getPath().split("_", 2)[0];
                Identifier baseStructure = new Identifier(structure.getNamespace(), name);
                return getClosestStructurePosition(world, center, baseStructure, radius);
            }
            return Optional.empty();
        }
    }
}
