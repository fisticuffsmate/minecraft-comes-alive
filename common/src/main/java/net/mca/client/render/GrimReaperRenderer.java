package net.mca.client.render;

import net.mca.MCA;
import net.mca.client.model.GrimReaperEntityModel;
import net.mca.entity.GrimReaperEntity;
import net.mca.util.compat.model.Dilation;
import net.mca.util.compat.model.TexturedModelData;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class GrimReaperRenderer extends BipedEntityRenderer<GrimReaperEntity, GrimReaperEntityModel<GrimReaperEntity>> {
    private static final Identifier TEXTURE = MCA.locate("textures/entity/grimreaper.png");

    public GrimReaperRenderer(EntityRenderDispatcher ctx) {
        super(ctx, new GrimReaperEntityModel<>(
            TexturedModelData.of(GrimReaperEntityModel.getModelData(Dilation.NONE), 64, 64).createModel()
        ), 0.5F);
    }

    @Override
    protected void scale(GrimReaperEntity reaper, MatrixStack matrices, float tickDelta) {
        matrices.scale(1.3F, 1.3F, 1.3F);
    }

    @Override
    public Identifier getTexture(GrimReaperEntity reaper) {
        return TEXTURE;
    }
}