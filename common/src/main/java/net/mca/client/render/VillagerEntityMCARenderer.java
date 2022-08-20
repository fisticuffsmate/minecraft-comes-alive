package net.mca.client.render;

import net.mca.client.model.VillagerEntityModelMCA;
import net.mca.client.render.layer.ClothingLayer;
import net.mca.client.render.layer.FaceLayer;
import net.mca.client.render.layer.HairLayer;
import net.mca.client.render.layer.SkinLayer;
import net.mca.entity.VillagerEntityMCA;
import net.mca.util.compat.model.Dilation;
import net.mca.util.compat.model.ModelData;
import net.mca.util.compat.model.TexturedModelData;
import net.minecraft.client.render.entity.EntityRenderDispatcher;

public class VillagerEntityMCARenderer extends VillagerLikeEntityMCARenderer<VillagerEntityMCA> {
    public VillagerEntityMCARenderer(EntityRenderDispatcher ctx) {
        super(ctx, createModel(VillagerEntityModelMCA.bodyData(Dilation.NONE)).hideWears());

        addFeature(new SkinLayer<>(this, model));
        addFeature(new FaceLayer<>(this, createModel(VillagerEntityModelMCA.bodyData(new Dilation(0.01F))).hideWears(), "normal"));
        addFeature(new ClothingLayer<>(this, createModel(VillagerEntityModelMCA.bodyData(new Dilation(0.0625F))), "normal"));
        addFeature(new HairLayer<>(this, createModel(VillagerEntityModelMCA.hairData(new Dilation(0.125F)))));
    }

    private static VillagerEntityModelMCA<VillagerEntityMCA> createModel(ModelData data) {
        return new VillagerEntityModelMCA<>(TexturedModelData.of(data, 64, 64).createModel());
    }
}
