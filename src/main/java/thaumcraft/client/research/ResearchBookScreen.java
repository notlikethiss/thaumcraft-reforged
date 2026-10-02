package thaumcraft.client.research;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import thaumcraft.Thaumcraft;
import thaumcraft.client.gui.TcFonts;
import thaumcraft.research.PlayerKnowledge;
import thaumcraft.research.ResearchItem;
import thaumcraft.research.ResearchList;
import thaumcraft.research.ResearchManager;

public class ResearchBookScreen extends Screen {
    private static final Identifier BACKGROUND = Thaumcraft.id("textures/gui/gui_researchback.png");
    private static final Identifier FRAME = Thaumcraft.id("textures/gui/gui_research.png");
    private static final Identifier ICONS = Thaumcraft.id("textures/misc/ss_research.png");
    private static final int PANE_WIDTH = 256;
    private static final int PANE_HEIGHT = 230;

    private static int lastX = -5;
    private static int lastY = -6;

    private final int mapTop = ResearchList.minDisplayColumn * 24 - 85;
    private final int mapLeft = ResearchList.minDisplayRow * 24 - 112;
    private final int mapBottom = ResearchList.maxDisplayColumn * 24 - 112;
    private final int mapRight = ResearchList.maxDisplayRow * 24 - 61;
    private final List<ResearchItem> research = new ArrayList<>(ResearchList.RESEARCH.values());
    private double previousMapX;
    private double previousMapY;
    private double mapX;
    private double mapY;
    private double targetMapX;
    private double targetMapY;
    private int dragState;
    private int dragMouseX;
    private int dragMouseY;
    private @Nullable ResearchItem highlight;

    public ResearchBookScreen() {
        this(lastX * 24 - 141 / 2 - 12, lastY * 24 - 141 / 2);
    }

    public ResearchBookScreen(double x, double y) {
        super(Component.empty());
        previousMapX = mapX = targetMapX = x;
        previousMapY = mapY = targetMapY = y;
    }

    public static void open() {
        Minecraft.getInstance().gui.setScreen(new ResearchBookScreen());
    }

    public double mapX() {
        return mapX;
    }

    public double mapY() {
        return mapY;
    }

    @Override
    public void removed() {
        lastX = (int) ((mapX + 141 / 2 + 12.0) / 24.0);
        lastY = (int) ((mapY + 141 / 2) / 24.0);
        super.removed();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean isInGameUi() {
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (minecraft.options.keyInventory.matches(event)) {
            onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void tick() {
        previousMapX = mapX;
        previousMapY = mapY;
        double dx = targetMapX - mapX;
        double dy = targetMapY - mapY;
        if (dx * dx + dy * dy < 4.0) {
            mapX += dx;
            mapY += dy;
        } else {
            mapX += dx * 0.85;
            mapY += dy * 0.85;
        }
    }

    private PlayerKnowledge knowledge() {
        return ResearchManager.knowledge(minecraft.player);
    }

    private boolean isComplete(ResearchItem item) {
        return knowledge().isComplete(item.key);
    }

    private boolean canUnlock(ResearchItem item) {
        for (ResearchItem[] parents : new ResearchItem[][]{item.parents, item.parentsHidden}) {
            if (parents == null) {
                continue;
            }
            for (ResearchItem parent : parents) {
                if (parent != null && !knowledge().isComplete(parent.key)) {
                    return false;
                }
            }
        }
        return true;
    }

    private void handleDrag(int mouseX, int mouseY) {
        if (!minecraft.mouseHandler.isLeftPressed()) {
            dragState = 0;
            return;
        }
        int left = (width - PANE_WIDTH) / 2 + 8;
        int top = (height - PANE_HEIGHT) / 2 + 17;
        if ((dragState == 0 || dragState == 1) && mouseX >= left && mouseX < left + 224 && mouseY >= top && mouseY < top + 196) {
            if (dragState == 0) {
                dragState = 1;
            } else {
                mapX -= mouseX - dragMouseX;
                mapY -= mouseY - dragMouseY;
                targetMapX = previousMapX = mapX;
                targetMapY = previousMapY = mapY;
            }
            dragMouseX = mouseX;
            dragMouseY = mouseY;
        }
        targetMapX = Mth.clamp(targetMapX, mapTop, mapBottom - 1);
        targetMapY = Mth.clamp(targetMapY, mapLeft, mapRight - 1);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        handleDrag(mouseX, mouseY);
        int viewX = Mth.clamp(Mth.floor(previousMapX + (mapX - previousMapX) * partialTick), mapTop, mapBottom - 1);
        int viewY = Mth.clamp(Mth.floor(previousMapY + (mapY - previousMapY) * partialTick), mapLeft, mapRight - 1);
        int paneX = (width - PANE_WIDTH) / 2;
        int paneY = (height - PANE_HEIGHT) / 2;
        int originX = paneX + 16;
        int originY = paneY + 17;
        graphics.enableScissor(originX, originY, originX + 224, originY + 196);
        int u = (int) ((float) (viewX - mapTop) / Math.abs(mapTop - mapBottom) * 288.0F);
        int v = (int) ((float) (viewY - mapLeft) / Math.abs(mapLeft - mapRight) * 316.0F);
        graphics.pose().pushMatrix();
        graphics.pose().scale(2.0F, 2.0F);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, originX / 2, originY / 2, u / 2.0F, v / 2.0F, 112, 98, 256, 256);
        graphics.pose().popMatrix();
        drawConnections(graphics, viewX, viewY, originX, originY);
        drawIcons(graphics, viewX, viewY, originX, originY, mouseX, mouseY);
        graphics.disableScissor();
        graphics.blit(RenderPipelines.GUI_TEXTURED, FRAME, paneX, paneY, 0.0F, 0.0F, PANE_WIDTH, PANE_HEIGHT, 256, 256);
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        if (highlight != null) {
            graphics.nextStratum();
            drawTooltip(graphics, highlight, mouseX + 12, mouseY - 4);
        }
    }

    private int pulse() {
        return Math.sin(Util.getMillis() % 600L / 600.0 * Math.PI * 2.0) > 0.6 ? 255 : 130;
    }

    private void drawConnections(GuiGraphicsExtractor graphics, int viewX, int viewY, int originX, int originY) {
        for (ResearchItem item : research) {
            if (item.parents != null) {
                for (ResearchItem parent : item.parents) {
                    if (parent != null && research.contains(parent)) {
                        drawConnection(graphics, item, parent, viewX, viewY, originX, originY);
                    }
                }
            }
            if (item.siblings != null) {
                for (ResearchItem sibling : item.siblings) {
                    if (sibling != null && research.contains(sibling) && sibling.parents == null
                        || sibling.parents != null && !Arrays.asList(sibling.parents).contains(item)) {
                        drawConnection(graphics, item, sibling, viewX, viewY, originX, originY);
                    }
                }
            }
        }
    }

    private void drawConnection(GuiGraphicsExtractor graphics, ResearchItem item, ResearchItem other, int viewX, int viewY, int originX, int originY) {
        int x1 = item.displayColumn * 24 - viewX + 11 + originX;
        int y1 = item.displayRow * 24 - viewY + 11 + originY;
        int x2 = other.displayColumn * 24 - viewX + 11 + originX;
        int y2 = other.displayRow * 24 - viewY + 11 + originY;
        int color = 0xFF000000;
        if (isComplete(item)) {
            color = -9408400;
        } else {
            if (item.getHidden()) {
                return;
            }
            if (isComplete(other)) {
                color = 0x00FF00 + (pulse() << 24);
            }
        }
        graphics.horizontalLine(x1, x2, y1, color);
        graphics.verticalLine(x2, y1, y2, color);
    }

    private void drawIcons(GuiGraphicsExtractor graphics, int viewX, int viewY, int originX, int originY, int mouseX, int mouseY) {
        highlight = null;
        for (ResearchItem item : research) {
            int dx = item.displayColumn * 24 - viewX;
            int dy = item.displayRow * 24 - viewY;
            if (dx < -24 || dy < -24 || dx > 224 || dy > 196) {
                continue;
            }
            float brightness;
            if (isComplete(item)) {
                brightness = 1.0F;
            } else {
                if (item.getHidden()) {
                    continue;
                }
                if (canUnlock(item)) {
                    brightness = Math.sin(Util.getMillis() % 600L / 600.0 * Math.PI * 2.0) < 0.6 ? 0.6F : 0.8F;
                } else {
                    brightness = 0.3F;
                }
            }
            int frameColor = ARGB.colorFromFloat(1.0F, brightness, brightness, brightness);
            int x = originX + dx;
            int y = originY + dy;
            float frameU = item.getStub() ? 54.0F : item.getLost() ? 86.0F : 0.0F;
            graphics.blit(RenderPipelines.GUI_TEXTURED, FRAME, x - 2, y - 2, frameU, 230.0F, 26, 26, 256, 256, frameColor);
            if (item.getSpecial()) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, FRAME, x - 2, y - 2, 26.0F, 230.0F, 26, 26, 256, 256, frameColor);
            }
            boolean unlockable = canUnlock(item);
            ItemStack icon = item.getIconStack();
            if (!icon.isEmpty()) {
                graphics.item(icon, x + 3, y + 3);
                if (!unlockable) {
                    graphics.fill(x + 3, y + 3, x + 19, y + 19, 0xE6000000);
                }
            } else if (item.iconIndex >= 0) {
                float iconBrightness = unlockable ? brightness : 0.1F;
                int iconColor = ARGB.colorFromFloat(1.0F, iconBrightness, iconBrightness, iconBrightness);
                graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    ICONS,
                    x + 3,
                    y + 3,
                    item.iconIndex % 16 * 16.0F,
                    item.iconIndex / 16 * 16.0F,
                    16,
                    16,
                    256,
                    256,
                    iconColor
                );
            }
            if (mouseX >= originX && mouseY >= originY && mouseX < originX + 224 && mouseY < originY + 196
                && mouseX >= x && mouseX <= x + 22 && mouseY >= y && mouseY <= y + 22) {
                highlight = item;
            }
        }
    }

    private void drawTooltip(GuiGraphicsExtractor graphics, ResearchItem item, int x, int y) {
        boolean complete = isComplete(item);
        boolean galactic = !complete;
        ResearchTexts.Entry entry = ResearchTexts.get(item.key);
        String name = entry == null ? item.key : entry.name();
        String popup = entry == null ? "" : entry.popup();
        Component nameText = TcFonts.text(name, galactic);
        Component popupText = TcFonts.text(popup, galactic);
        Font font = this.font;
        if (canUnlock(item)) {
            int textWidth = Math.max(font.width(nameText), 120);
            List<FormattedCharSequence> lines = font.split(popupText, textWidth);
            int textHeight = lines.size() * font.lineHeight;
            if (complete) {
                textHeight += 12;
            }
            graphics.fill(x - 3, y - 3, x + textWidth + 3, y + textHeight + 3 + 12, 0xC0000000);
            for (int i = 0; i < lines.size(); i++) {
                graphics.text(font, lines.get(i), x, y + 12 + i * font.lineHeight, -6250336, false);
            }
            if (complete) {
                graphics.text(font, Component.translatable("tc.thaumcraft.research_completed"), x, y + textHeight + 4, -7302913, true);
            }
        } else {
            int textWidth = Math.max(font.width(popupText), 120);
            List<FormattedCharSequence> lines = font.split(Component.translatable("tc.thaumcraft.missing_research"), textWidth);
            int textHeight = lines.size() * font.lineHeight;
            graphics.fill(x - 3, y - 3, x + textWidth + 3, y + textHeight + 12 + 3, 0xC0000000);
            for (int i = 0; i < lines.size(); i++) {
                graphics.text(font, lines.get(i), x, y + 12 + i * font.lineHeight, -9416624, false);
            }
        }
        int nameColor = canUnlock(item) ? (item.getSpecial() ? -128 : -1) : (item.getSpecial() ? -8355776 : -8355712);
        graphics.text(font, nameText, x, y, nameColor, true);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (highlight != null && isComplete(highlight) && ResearchPageScreen.hasPages(highlight.key)) {
            minecraft.gui.setScreen(new ResearchPageScreen(highlight, mapX, mapY));
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }
}
