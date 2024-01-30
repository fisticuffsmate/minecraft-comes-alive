package net.mca.client.gui.widget;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.MutableText;
import net.minecraft.util.Identifier;

public class OldTexturedButtonWidget extends ButtonWidget {
    protected final int u;
    protected final int v;
    protected final Identifier texture;
    protected final int highlightOffset;
    protected final int tw;
    protected final int th;

    public OldTexturedButtonWidget(int x, int y, int width, int height, int u, int v, int highlightOffset, Identifier texture, int tw, int th, ButtonWidget.PressAction pressAction, MutableText text) {
        super(x, y, width, height, text, pressAction, DEFAULT_NARRATION_SUPPLIER);

        this.u = u;
        this.v = v;
        this.texture = texture;
        this.highlightOffset = highlightOffset;
        this.tw = tw;
        this.th = th;
    }

    @Override
    public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
        context.drawTexture(texture, getX(), getY() + (isHovered() ? highlightOffset : 0), u, v, width, height, tw, th);
    }
}

