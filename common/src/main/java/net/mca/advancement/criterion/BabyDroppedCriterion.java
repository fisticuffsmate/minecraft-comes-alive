package net.mca.advancement.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.dynamic.Codecs;

import java.util.Optional;

public class BabyDroppedCriterion extends AbstractCriterion<BabyDroppedCriterion.Conditions> {
    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayerEntity player, int c) {
        trigger(player, conditions -> conditions.test(c));
    }

    public record Conditions(
            Optional<LootContextPredicate> player,
            NumberRange.IntRange count
    ) implements AbstractCriterion.Conditions {
        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(instance -> {
            return instance.group(
                    Codecs.createStrictOptionalFieldCodec(EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC, "player")
                            .forGetter(Conditions::player),
                    Codecs.createStrictOptionalFieldCodec(NumberRange.IntRange.CODEC, "count", NumberRange.IntRange.ANY)
                            .forGetter(Conditions::count)
            ).apply(instance, Conditions::new);
        });

        public boolean test(int c) {
            return count.test(c);
        }
    }
}

