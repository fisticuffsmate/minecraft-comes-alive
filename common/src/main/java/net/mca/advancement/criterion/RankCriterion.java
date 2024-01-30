package net.mca.advancement.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mca.resources.Rank;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.dynamic.Codecs;

import java.util.Objects;
import java.util.Optional;

public class RankCriterion extends AbstractCriterion<RankCriterion.Conditions> {
    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayerEntity player, Rank rank) {
        trigger(player, conditions -> conditions.test(rank));
    }

    public record Conditions(
            Optional<LootContextPredicate> player,
            String rank
    ) implements AbstractCriterion.Conditions {
        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(instance -> {
            return instance.group(
                    Codecs.createStrictOptionalFieldCodec(EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC, "player")
                            .forGetter(Conditions::player),
                    Codecs.createStrictOptionalFieldCodec(Codec.STRING, "rank", "")
                            .forGetter(Conditions::rank)
            ).apply(instance, Conditions::new);
        });

        public boolean test(Rank rank) {
            return Objects.equals(this.rank, rank.name());
        }
    }
}
