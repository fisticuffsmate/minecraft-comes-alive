package net.mca.util.compat;

import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.util.DyeColor;

public interface SheepEntityCompat {
    /**
     * @since MC 1.17
     */
    static float[] getRgbColor(DyeColor dyeColor) {
        return (float[]) SheepEntity.COLORS.get(dyeColor);
    }
}
