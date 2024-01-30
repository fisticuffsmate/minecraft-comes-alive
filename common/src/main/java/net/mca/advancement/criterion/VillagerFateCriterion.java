package net.mca.advancement.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mca.resources.Rank;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;

import java.util.Optional;

public class VillagerFateCriterion extends AbstractCriterion<VillagerFateCriterion.Conditions> {
    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayerEntity player, Identifier cause, Rank userRelation) {
        trigger(player, conditions -> conditions.test(cause, userRelation));
    }

    public record Conditions(
            Optional<LootContextPredicate> player,
            String userRelation,
            String cause
    ) implements AbstractCriterion.Conditions {
        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(instance -> {
            return instance.group(
                    Codecs.createStrictOptionalFieldCodec(EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC, "player")
                            .forGetter(Conditions::player),
                    Codecs.createStrictOptionalFieldCodec(Codec.STRING, "user_relation", "")
                            .forGetter(Conditions::userRelation),
                    Codecs.createStrictOptionalFieldCodec(Codec.STRING, "cause", "")
                            .forGetter(Conditions::cause)
            ).apply(instance, Conditions::new);
        });

        public boolean test(Identifier cause, Rank userRelation) {
            return this.cause.equals(cause.toString()) && userRelation.isAtLeast(Rank.fromName(this.userRelation));
        }
    }
}
