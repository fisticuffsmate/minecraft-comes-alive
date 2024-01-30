package net.mca.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.BlockState;

public class InfernalFlameBlock extends AbstractFireBlock {
    public static final MapCodec<InfernalFlameBlock> CODEC = createCodec(InfernalFlameBlock::new);

    public InfernalFlameBlock(AbstractBlock.Settings settings) {
        super(settings, 2.0F);
    }

    @Override
    protected MapCodec<? extends AbstractFireBlock> getCodec() {
        return CODEC;
    }

    @Override
    protected boolean isFlammable(BlockState state) {
        return true;
    }
}
