package net.mca.advancement.criterion;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.mca.MCA;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Optional;

public class HeartsCriterion extends AbstractCriterion<HeartsCriterion.Conditions> {
    @Override
    public Conditions conditionsFromJson(JsonObject json, Optional<LootContextPredicate> player, AdvancementEntityPredicateDeserializer deserializer) {
        NumberRange.IntRange hearts = NumberRange.IntRange.fromJson(json.get("hearts"));
        NumberRange.IntRange increase = NumberRange.IntRange.fromJson(json.get("increase"));
        String source = json.has("source") ? json.get("source").getAsString() : "";
        return new Conditions(player, hearts, increase, source);
    }

    public void trigger(ServerPlayerEntity player, int hearts, int increase, String source) {
        trigger(player, conditions -> conditions.test(hearts, increase, source));
    }

    public static class Conditions extends AbstractCriterionConditions {
        private final NumberRange.IntRange hearts;
        private final NumberRange.IntRange increase;
        private final String source;

        public Conditions(Optional<LootContextPredicate> player, NumberRange.IntRange hearts, NumberRange.IntRange increase, String source) {
            super(player);
            this.hearts = hearts;
            this.increase = increase;
            this.source = source;
        }

        public boolean test(int hearts, int increase, String source) {
            return this.hearts.test(hearts) && this.increase.test(increase)
                    && (MCA.isBlankString(this.source) || this.source.equals(source));
        }

        @Override
        public JsonObject toJson() {
            JsonObject json = super.toJson();
            json.add("hearts", hearts.toJson());
            json.add("increase", increase.toJson());
            json.add("source", new JsonPrimitive(source));
            return json;
        }
    }
}
