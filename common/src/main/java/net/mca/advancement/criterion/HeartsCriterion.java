package net.mca.advancement.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mca.MCA;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.dynamic.Codecs;

import java.util.Optional;

public class HeartsCriterion extends AbstractCriterion<HeartsCriterion.Conditions> {
    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayerEntity player, int hearts, int increase, String source) {
        trigger(player, conditions -> conditions.test(hearts, increase, source));
    }

    public record Conditions(
            Optional<LootContextPredicate> player,
            NumberRange.IntRange hearts,
            NumberRange.IntRange increase,
            String source
    ) implements AbstractCriterion.Conditions {
        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create(instance -> {
            return instance.group(
                    Codecs.createStrictOptionalFieldCodec(EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC, "player")
                            .forGetter(Conditions::player),
                    Codecs.createStrictOptionalFieldCodec(NumberRange.IntRange.CODEC, "hearts", NumberRange.IntRange.ANY)
                            .forGetter(Conditions::hearts),
                    Codecs.createStrictOptionalFieldCodec(NumberRange.IntRange.CODEC, "increase", NumberRange.IntRange.ANY)
                            .forGetter(Conditions::increase),
                    Codecs.createStrictOptionalFieldCodec(Codec.STRING, "source", "")
                            .forGetter(Conditions::source)
            ).apply(instance, Conditions::new);
        });

        public boolean test(int hearts, int increase, String source) {
            return this.hearts.test(hearts) && this.increase.test(increase)
                    && (MCA.isBlankString(this.source) || this.source.equals(source));
        }
    }
}
