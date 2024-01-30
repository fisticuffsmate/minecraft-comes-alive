package net.mca.advancement.criterion;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.mca.resources.Rank;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Optional;

public class RankCriterion extends AbstractCriterion<RankCriterion.Conditions> {
    @Override
    public Conditions conditionsFromJson(JsonObject json, Optional<LootContextPredicate> player, AdvancementEntityPredicateDeserializer deserializer) {
        Rank rank = Rank.fromName(json.get("rank").getAsString());
        return new Conditions(player, rank);
    }

    public void trigger(ServerPlayerEntity player, Rank rank) {
        trigger(player, conditions -> conditions.test(rank));
    }

    public static class Conditions extends AbstractCriterionConditions {
        private final Rank rank;

        public Conditions(Optional<LootContextPredicate> player, Rank rank) {
            super(player);
            this.rank = rank;
        }

        public boolean test(Rank rank) {
            return this.rank == rank;
        }

        @Override
        public JsonObject toJson() {
            JsonObject json = super.toJson();
            json.add("rank", new JsonPrimitive(rank.name()));
            return json;
        }
    }
}
