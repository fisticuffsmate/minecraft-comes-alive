package net.mca.advancement.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mca.server.world.data.FamilyTree;
import net.mca.server.world.data.FamilyTreeNode;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.dynamic.Codecs;

import java.util.Optional;

public class FamilyCriterion extends AbstractCriterion<FamilyCriterion.Conditions> {
    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayerEntity player) {
        FamilyTreeNode familyTree = FamilyTree.get(player.getServerWorld()).getOrCreate(player);
        long c = familyTree.getRelatives(0, 1).count();
        long gc = familyTree.getRelatives(0, 2).count() - c;

        trigger(player, condition -> condition.test((int) c, (int) gc));
    }

    public record Conditions(
            Optional<LootContextPredicate> player,
            NumberRange.IntRange children,
            NumberRange.IntRange grandchildren
    ) implements AbstractCriterion.Conditions {
        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(instance -> {
            return instance.group(
                    Codecs.createStrictOptionalFieldCodec(EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC, "player")
                            .forGetter(Conditions::player),
                    Codecs.createStrictOptionalFieldCodec(NumberRange.IntRange.CODEC, "children", NumberRange.IntRange.ANY)
                            .forGetter(Conditions::children),
                    Codecs.createStrictOptionalFieldCodec(NumberRange.IntRange.CODEC, "grandchildren", NumberRange.IntRange.ANY)
                            .forGetter(Conditions::grandchildren)
            ).apply(instance, Conditions::new);
        });

        public boolean test(int c, int gc) {
            return children.test(c) && grandchildren.test(gc);
        }
    }
}
