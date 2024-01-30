package net.mca.advancement.criterion;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Optional;

public class ChildAgeStateChangeCriterion extends AbstractCriterion<ChildAgeStateChangeCriterion.Conditions> {
    @Override
    public Conditions conditionsFromJson(JsonObject json, Optional<LootContextPredicate> player, AdvancementEntityPredicateDeserializer deserializer) {
        String event = json.has("state") ? json.get("state").getAsString() : "";
        return new Conditions(player, event);
    }

    public void trigger(ServerPlayerEntity player, String event) {
        trigger(player, conditions -> conditions.test(event));
    }

    public static class Conditions extends AbstractCriterionConditions {
        private final String event;

        public Conditions(Optional<LootContextPredicate> player, String event) {
            super(player);
            this.event = event;
        }

        public boolean test(String event) {
            return this.event.equals(event);
        }

        @Override
        public JsonObject toJson() {
            JsonObject json = super.toJson();
            json.add("state", new JsonPrimitive(event));
            return json;
        }
    }
}
