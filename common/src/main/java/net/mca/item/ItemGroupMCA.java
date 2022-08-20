package net.mca.item;

import me.shedaniel.architectury.registry.CreativeTabs;
import net.mca.MCA;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;

public interface ItemGroupMCA {
    @SuppressWarnings("Convert2MethodRef")
    ItemGroup MCA_GROUP = CreativeTabs.create(
            new Identifier(MCA.MOD_ID, "mca_tab"),
            () -> ItemsMCA.ENGAGEMENT_RING.get().getDefaultStack()
    );
}
