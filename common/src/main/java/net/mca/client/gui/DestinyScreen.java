package net.mca.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.mca.MCAClient;
import net.mca.cobalt.network.NetworkHandler;
import net.mca.entity.VillagerLike;
import net.mca.network.c2s.DestinyMessage;
import net.mca.util.compat.RenderSystemCompat;
import net.mca.util.localization.FlowingText;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class DestinyScreen extends VillagerEditorScreen {
    private static final Identifier LOGO_TEXTURE = new Identifier("mca:textures/banner.png");
    private LinkedList<Text> story;
    private String location;
    private boolean teleported = false;

    private final boolean allowTeleportation;
    private final boolean allowPlayerModel;
    private final boolean allowVillagerModel;

    public DestinyScreen(UUID playerUUID, boolean allowTeleportation, boolean allowPlayerModel, boolean allowVillagerModel) {
        super(playerUUID, playerUUID);

        this.allowTeleportation = allowTeleportation;
        this.allowPlayerModel = allowPlayerModel;
        this.allowVillagerModel = allowVillagerModel;
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    @Override
    public void onClose() {
        if (!page.equals("general") && !page.equals("story")) {
            setPage("destiny");
        }
    }

    @Override
    protected String[] getPages() {
        return new String[] {"general", "body", "head", "traits"};
    }

    @Override
    public void renderBackground(MatrixStack matrices) {
        assert MinecraftClient.getInstance().world != null;
        renderBackgroundTexture((int)MinecraftClient.getInstance().world.getTime());
    }

    private void drawScaledText(MatrixStack transform, Text text, int x, int y, float scale) {
        transform.push();
        transform.scale(scale, scale, scale);
        drawCenteredText(transform, textRenderer, text, (int)(x / scale), (int)(y / scale), 0xffffffff);
        transform.pop();
    }

    @Override
    public void render(MatrixStack transform, int mouseX, int mouseY, float delta) {
        super.render(transform, mouseX, mouseY, delta);

        switch (page) {
            case "general":
                drawScaledText(transform, new TranslatableText("gui.destiny.whoareyou"), width / 2, height / 2 - 24, 1.5f);
                transform.push();
                transform.scale(0.25f, 0.25f, 0.25f);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.color4f(1, 1, 1, 1);
                RenderSystemCompat.setShaderTexture(0, LOGO_TEXTURE);
                DrawableHelper.drawTexture(transform, width * 2 - 512, -40, 0, 0, 1024, 512, 1024, 512);
                transform.pop();
                break;
            case "destiny":
                drawScaledText(transform, new TranslatableText("gui.destiny.journey"), width / 2, height / 2 - 48, 1.5f);
                break;
            case "story":
                List<Text> text = FlowingText.wrap(story.getFirst(), 256);
                int y = (int)(height / 2 - 20 - 7.5f * text.size());
                for (Text t : text) {
                    drawScaledText(transform, t, width / 2, y, 1.25f);
                    y += 15;
                }
                break;
        }
    }

    @Override
    protected boolean shouldDrawEntity() {
        return !page.equals("general") && !page.equals("destiny") && !page.equals("story") && super.shouldDrawEntity();
    }

    @Override
    protected void setPage(String page) {
        if (page.equals("destiny") && !allowTeleportation) {
            NetworkHandler.sendToServer(new DestinyMessage(null));
            MCAClient.getDestinyManager().allowClosing();
            super.onClose();
            return;
        }

        this.page = page;
        children.clear();
        buttons.clear();
        switch (page) {
            case "general":
                drawName(width / 2 - DATA_WIDTH / 2, height / 2);
                drawGender(width / 2 - DATA_WIDTH / 2, height / 2 + 24);

                if (allowPlayerModel && allowVillagerModel) {
                    drawModel(width / 2 - DATA_WIDTH / 2, height / 2 + 24 + 22);
                }

                addButton(new ButtonWidget(width / 2 - 32, height / 2 + 60 + 22, 64, 20, new TranslatableText("gui.button.accept"), sender -> {
                    setPage("body");
                    if (villager.getTrackedValue(VillagerLike.VILLAGER_NAME).isEmpty()) {
                        villager.setTrackedValue(VillagerLike.VILLAGER_NAME, "Nameless Traveller");
                    }
                }));
                break;
            case "destiny":
                int x = 0;
                int y = 0;
                for (String location : new String[] {"somewhere", "shipwreck_beached", "village_desert", "village_taiga", "village_snowy", "village_plains", "village_savanna"}) {
                    addButton(new ButtonWidget((int)(width / 2 - 96 * 1.5f + x * 96), height / 2 + y * 20 - 16, 96, 20, new TranslatableText("gui.destiny." + location), sender -> {
                        //story
                        story = new LinkedList<>();
                        story.add(new TranslatableText("destiny.story.reason"));
                        story.add(new TranslatableText(location.equals("shipwreck_beached") ? "destiny.story.sailing" : "destiny.story.travelling"));
                        story.add(new TranslatableText("destiny.story." + location));
                        this.location = location;
                        setPage("story");
                    }));
                    x++;
                    if (x >= 3) {
                        x = 0;
                        y++;
                    }
                }
                break;
            case "story":
                addButton(new ButtonWidget(width / 2 - 48, height / 2 + 32, 96, 20, new TranslatableText("gui.destiny.next"), sender -> {
                //we teleport early here to avoid initial flickering
                if (!teleported) {
                    NetworkHandler.sendToServer(new DestinyMessage(location));
                    MCAClient.getDestinyManager().allowClosing();
                    teleported = true;
                }
                if (story.size() > 1) {
                    story.remove(0);
                } else {
                    super.onClose();
                }
                }));
                break;
            default:
                super.setPage(page);
                break;
        }
    }
}
