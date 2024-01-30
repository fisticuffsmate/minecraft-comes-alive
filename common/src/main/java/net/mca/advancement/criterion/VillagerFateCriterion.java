package net.mca.advancement.criterion;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.mca.resources.Rank;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Optional;

public class VillagerFateCriterion extends AbstractCriterion<VillagerFateCriterion.Conditions> {
    @Override
    public VillagerFateCriterion.Conditions conditionsFromJson(JsonObject json, Optional<LootContextPredicate> player, AdvancementEntityPredicateDeserializer deserializer) {
        Rank userRelation = Rank.fromName(json.get("user_relation").getAsString());
        Identifier cause = Identifier.tryParse(json.get("cause").getAsString());
        return new Conditions(player, cause, userRelation);
    }

    public void trigger(ServerPlayerEntity player, Identifier cause, Rank userRelation) {
        trigger(player, conditions -> conditions.test(cause, userRelation));
    }

    public static class Conditions extends AbstractCriterionConditions {
        private final Rank userRelation;
        private final Identifier cause;

        public Conditions(Optional<LootContextPredicate> player, Identifier cause, Rank userRelation) {
            super(player);
            this.userRelation = userRelation;
            this.cause = cause;
        }

        public boolean test(Identifier cause, Rank userRelation) {
            return this.cause.toString().equals(cause.toString()) && userRelation.isAtLeast(this.userRelation);
        }

        @Override
        public JsonObject toJson() {
            JsonObject json = super.toJson();
            json.add("cause", new JsonPrimitive(cause.toString()));
            json.add("user_relation", new JsonPrimitive(userRelation.name()));
            return json;
        }
    }
}
