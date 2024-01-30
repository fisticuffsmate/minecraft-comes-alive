package net.mca.advancement.criterion;

import com.google.gson.JsonObject;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Optional;

public class BabySmeltedCriterion extends AbstractCriterion<BabySmeltedCriterion.Conditions> {
    @Override
    public Conditions conditionsFromJson(JsonObject json, Optional<LootContextPredicate> player, AdvancementEntityPredicateDeserializer deserializer) {
        NumberRange.IntRange c = NumberRange.IntRange.atLeast(json.get("count").getAsInt());
        return new Conditions(player, c);
    }

    public void trigger(ServerPlayerEntity player, int c) {
        trigger(player, conditions -> conditions.test(c));
    }

    public static class Conditions extends AbstractCriterionConditions {
        private final NumberRange.IntRange count;

        public Conditions(Optional<LootContextPredicate> player, NumberRange.IntRange count) {
            super(player);
            this.count = count;
        }

        public boolean test(int c) {
            return count.test(c);
        }

        @Override
        public JsonObject toJson() {
            JsonObject json = super.toJson();
            json.add("count", count.toJson());
            return json;
        }
    }
}
