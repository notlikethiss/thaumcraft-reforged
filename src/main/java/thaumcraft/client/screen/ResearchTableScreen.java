package thaumcraft.client.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nullable;
import thaumcraft.Thaumcraft;
import thaumcraft.client.gui.GuiDraw;
import thaumcraft.aspect.Aspect;
import thaumcraft.blockentity.ResearchTableBlockEntity;
import thaumcraft.client.gui.AspectRenderer;
import thaumcraft.client.gui.GuiLines;
import thaumcraft.client.gui.TcFonts;
import thaumcraft.client.research.ResearchTexts;
import thaumcraft.item.ResearchNotesItem;
import thaumcraft.menu.ResearchTableMenu;
import thaumcraft.research.ResearchManager;
import thaumcraft.research.ResearchNoteData;

public class ResearchTableScreen extends AbstractContainerScreen<ResearchTableMenu> {
    private static final ResourceLocation TEXTURE = Thaumcraft.id("textures/gui/guiresearchtable.png");
    private static final ResourceLocation PARCHMENT = Thaumcraft.id("textures/misc/parchment.png");

    private final List<int[]> coords = new ArrayList<>();
    private final List<Aspect> diagramTags = new ArrayList<>();
    private long buttonCooldown;
    private float popupScale = 0.05F;

    public ResearchTableScreen(ResearchTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 240;
        this.imageHeight = 242;
    }

    private ContainerData data() {
        return menu.getData();
    }

    private @Nullable Aspect tag(int index) {
        int id = data().get(index);
        return id < 0 ? null : Aspect.get(id);
    }

    private @Nullable ResearchNoteData noteData() {
        ItemStack note = menu.getTable().getItem(ResearchTableBlockEntity.NOTE_SLOT);
        return note.getItem() instanceof ResearchNotesItem ? ResearchManager.getData(note) : null;
    }

    private boolean buttonActive() {
        return buttonCooldown <= System.currentTimeMillis() && menu.canResearch();
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        GuiDraw.blit(graphics, TEXTURE, leftPos, topPos, 0.0F, 0.0F, imageWidth, imageHeight, 256, 256);
        GuiDraw.blit(graphics, TEXTURE, leftPos + 76, topPos + 40, 241.0F, buttonActive() ? 0.0F : 15.0F, 15, 15, 256, 256);
        boolean safe = data().get(15) == 1;
        GuiDraw.blit(graphics, TEXTURE, leftPos + 51, topPos + 130, safe ? 109.0F : 87.0F, 246.0F, 29, 7, 256, 256);
        ResearchNoteData note = noteData();
        for (int a = 0; a < ResearchTableBlockEntity.INPUT_SLOTS; a++) {
            Aspect aspect = tag(a);
            if (aspect == null) {
                continue;
            }
            float opacity = note != null && note.failedTags[aspect.getId()] >= 10 ? 0.15F : 1.0F;
            AspectRenderer.drawTag(graphics, leftPos + 48, topPos + 32 + 16 * a, aspect, data().get(5 + a), data().get(10 + a), false, false, opacity);
        }
        drawResearchData(graphics, note, leftPos + 167, topPos + 76);
    }

    private void drawResearchData(GuiGraphics graphics, @Nullable ResearchNoteData note, int x, int y) {
        coords.clear();
        diagramTags.clear();
        if (note == null) {
            return;
        }
        float total = note.getTotalProgress();
        for (Aspect tag : note.tags) {
            float progress = note.getTagProgress(tag);
            if (progress > 0.0F || total >= 0.5F) {
                diagramTags.add(progress < 0.3F && total < 0.75F ? Aspect.UNKNOWN : tag);
            }
        }
        if (diagramTags.isEmpty()) {
            return;
        }
        float slice = 360 / diagramTags.size();
        float rotation = -90.0F;
        for (Aspect tag : diagramTags) {
            float radius = 15.0F + 40.0F * note.getTagProgress(tag);
            int px = (int) (x + Mth.cos(rotation / 180.0F * Mth.PI) * radius) - 8;
            int py = (int) (y + Mth.sin(rotation / 180.0F * Mth.PI) * radius) - 8;
            rotation += slice;
            coords.add(new int[]{px, py});
        }
        for (int a = 0; a < diagramTags.size(); a++) {
            Aspect shown = diagramTags.get(a);
            if (note.getTagProgress(shown) < 0.2F && total < 0.75F) {
                shown = Aspect.UNKNOWN;
            }
            AspectRenderer.drawTag(graphics, coords.get(a)[0], coords.get(a)[1], shown, 1, false, false);
        }
        if (diagramTags.size() > 1) {
            for (int a = 0; a < diagramTags.size(); a++) {
                float instability = 1.0F - (note.getTagProgress(diagramTags.get(a)) + total) / 2.0F;
                int[] point = coords.get(a);
                GuiLines.unstable(graphics, x, y, point[0] + 8, point[1] + 8, instability, 0.5F);
                if (diagramTags.size() > 2) {
                    for (int b = a + 1; b < diagramTags.size(); b++) {
                        int[] other = coords.get(b);
                        GuiLines.unstable(graphics, other[0] + 8, other[1] + 8, point[0] + 8, point[1] + 8, instability, 0.5F);
                    }
                }
            }
        }
        for (int a = 0; a < diagramTags.size(); a++) {
            if (note.getTagProgress(diagramTags.get(a)) >= 1.0F) {
                int px = coords.get(a)[0];
                int py = coords.get(a)[1];
                GuiLines.unstable(graphics, px - 1, py - 1, px - 1, py + 17, 0.3F, 1.0F);
                GuiLines.unstable(graphics, px - 1, py - 1, px + 17, py - 1, 0.3F, 1.0F);
                GuiLines.unstable(graphics, px + 17, py - 1, px + 17, py + 17, 0.3F, 1.0F);
                GuiLines.unstable(graphics, px - 1, py + 17, px + 17, py + 17, 0.3F, 1.0F);
            }
        }
    }

    private void drawPopups(GuiGraphics graphics, int mouseX, int mouseY) {
        int mx = mouseX - (leftPos + 51);
        int my = mouseY - (topPos + 130);
        if (mx >= 0 && my >= 0 && mx < 29 && my < 7) {
            Component text = Component.translatable(data().get(15) == 1 ? "tc.thaumcraft.research_cursory" : "tc.thaumcraft.research_thorough");
            graphics.pose().pushPose();
            graphics.pose().translate(leftPos + 65, topPos + 142, 0.0F);
            graphics.pose().scale(0.65F, 0.65F, 1.0F);
            graphics.drawString(font, text, -font.width(text) / 2, 0, 0xFFAEAEAE, false);
            graphics.pose().popPose();
        }
        ResearchNoteData note = noteData();
        if (diagramTags.isEmpty() || note == null || note.key == null) {
            return;
        }
        mx = mouseX - (leftPos + 103);
        my = mouseY - (topPos + 12);
        if (popupScale < 0.48F && mx >= 0 && my >= 0 && mx < 17 && my < 17) {
            popupScale = Math.min(0.48F, popupScale * 1.25F);
        }
        if (popupScale > 0.05F && (mx < 0 || my < 0 || mx > 17.0F * popupScale * 15.5F || my > 17.0F * popupScale * 15.5F)) {
            popupScale = Math.max(0.05F, popupScale * 0.75F);
        }
        graphics.pose().pushPose();
        graphics.pose().translate(leftPos + 105, topPos + 14, 0.0F);
        graphics.pose().scale(popupScale, popupScale, 1.0F);
        GuiDraw.blit(graphics, PARCHMENT, 0, 0, 0.0F, 0.0F, 256, 256, 256, 256);
        graphics.pose().scale(1.5F, 1.5F, 1.0F);
        ResearchTexts.Entry entry = ResearchTexts.get(note.key);
        String text = entry == null ? "" : entry.longText();
        boolean galactic = note.getTotalProgress() < 0.5F;
        Component component = TcFonts.text(text, galactic);
        List<FormattedCharSequence> lines = font.split(component, 130);
        int lift = (int) (font.width(component) / 130.0F * (font.lineHeight / 2.0F));
        for (int i = 0; i < lines.size(); i++) {
            graphics.drawString(font, lines.get(i), 22, 80 - lift + i * font.lineHeight, 0xFF000000, false);
        }
        graphics.pose().popPose();
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderTooltip(graphics, mouseX, mouseY);
        for (int a = 0; a < ResearchTableBlockEntity.INPUT_SLOTS; a++) {
            Aspect aspect = tag(a);
            int mx = mouseX - (leftPos + 48);
            int my = mouseY - (topPos + 32 + 16 * a);
            if (aspect != null && mx >= 0 && my >= 0 && mx < 16 && my < 16) {
                graphics.renderTooltip(font, List.of(aspect.getDisplayName(), aspect.getMeaning()), Optional.empty(), mouseX, mouseY);
            }
        }
        ResearchNoteData note = noteData();
        if (note == null) {
            return;
        }
        for (int a = 0; a < diagramTags.size() && a < coords.size(); a++) {
            int mx = mouseX - coords.get(a)[0];
            int my = mouseY - coords.get(a)[1];
            if (mx >= 0 && my >= 0 && mx < 16 && my < 16) {
                Aspect aspect = diagramTags.get(a);
                List<Component> lines = new ArrayList<>(List.of(aspect.getDisplayName(), aspect.getMeaning()));
                float progress = note.getTagProgress(aspect);
                if (progress > 0.333332F) {
                    lines.add(Component.literal(String.valueOf((int) (progress * 100.0F))).append(Component.translatable("tc.thaumcraft.discoveryprogress")));
                }
                graphics.renderTooltip(font, lines, Optional.empty(), mouseX, mouseY);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.pose().pushPose();
        graphics.pose().translate(-leftPos, -topPos, 300.0F);
        drawPopups(graphics, mouseX, mouseY);
        graphics.pose().popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int mx = (int) mouseX - (leftPos + 76);
        int my = (int) mouseY - (topPos + 40);
        if (buttonActive() && mx >= 0 && my >= 0 && mx < 15 && my < 15) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, ResearchTableMenu.BUTTON_RESEARCH);
            buttonCooldown = System.currentTimeMillis() + 150L;
            return true;
        }
        mx = (int) mouseX - (leftPos + 51);
        my = (int) mouseY - (topPos + 130);
        if (buttonCooldown <= System.currentTimeMillis() && mx >= 0 && my >= 0 && mx < 29 && my < 7) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, ResearchTableMenu.BUTTON_TOGGLE_SAFE);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
