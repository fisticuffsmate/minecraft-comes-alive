package net.mca.advancement.criterion;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.advancement.criterion.Criterion;

public interface CriterionMCA {
    BabyCriterion BABY = register("baby", new BabyCriterion());
    BabyDroppedCriterion BABY_DROPPED = register("baby_dropped", new BabyDroppedCriterion());
    BabySmeltedCriterion BABY_SMELTED = register("baby_smelted", new BabySmeltedCriterion());
    BabySirbenSmeltedCriterion BABY_SIRBEN_SMELTED = register("baby_sirben_smelted", new BabySirbenSmeltedCriterion());
    HeartsCriterion HEARTS = register("hearts", new HeartsCriterion());
    GenericEventCriterion GENERIC_EVENT = register("generic_event", new GenericEventCriterion());
    ChildAgeStateChangeCriterion CHILD_AGE_STATE_CHANGE = register("child_age_state_change", new ChildAgeStateChangeCriterion());
    FamilyCriterion FAMILY = register("family", new FamilyCriterion());
    RankCriterion RANK = register("rank", new RankCriterion());
    VillagerFateCriterion VILLAGER_FATE = register("villager_fate", new VillagerFateCriterion());

    static <T extends Criterion<?>> T register(String id, T obj) {
        return Criteria.register("mca:" + id, obj);
    }

    static void bootstrap() {
    }
}
