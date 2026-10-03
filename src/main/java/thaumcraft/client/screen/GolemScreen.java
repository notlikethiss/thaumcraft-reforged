package thaumcraft.client.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import thaumcraft.Thaumcraft;
import thaumcraft.crafting.ConfigRecipes;
import thaumcraft.entity.golem.ClayGolem;
import thaumcraft.entity.golem.AdvancedClayGolem;
import thaumcraft.entity.golem.DecantingGolem;
import thaumcraft.entity.golem.GolemBase;
import thaumcraft.entity.golem.GolemKind;
import thaumcraft.entity.golem.IronGuardianGolem;
import thaumcraft.entity.golem.MultiColorGolem;
import thaumcraft.entity.golem.TallowGolem;
import thaumcraft.menu.GolemMenu;
import thaumcraft.registry.ModItems;

public class GolemScreen extends AbstractContainerScreen<GolemMenu> {
    private static final int[] MARKER_COLORS = {
        0xF0F0F0, 0xEB8844, 0xC354CD, 0x6689D3, 0xDECF2A, 0x41CD34, 0xD88198, 0x434343,
        0xA0A0A0, 0x287697, 0x7B2FBE, 0x253192, 0x51301A, 0x3B511A, 0xB3312C, 0x1E1B1B,
    };
    private static final int TEXT = 0xFFDDDDDD;
    private static final int LABEL = 0xFFFDFDFD;
    private static final int[] TARGET_FLAGS = {
        IronGuardianGolem.HOSTILES, IronGuardianGolem.ANIMALS, IronGuardianGolem.PLAYERS, IronGuardianGolem.CREEPERS,
    };
    private static final String[] TARGET_NAMES = {"monsters", "animals", "players", "creepers"};
    private static final int[] TARGET_COLORS = {0xFFFFCCCC, 0xFFFFFFCC, 0xFFCCCCFF, 0xFFCCFFCC};

    private final GolemBase golem;
    private final Identifier texture;
    private final int quote;

    public GolemScreen(GolemMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 166);
        this.golem = menu.getGolem();
        this.texture = Thaumcraft.id("textures/gui/" + switch (golem.kind()) {
            case WOOD, STRAW -> "guigolemwood";
            case CLAY -> "guigolemclay";
            case STONE -> "guigolemstone";
            case TALLOW -> "guigolemtallow";
            case ADVANCED_CLAY -> "guigolemclay2";
            case ADVANCED_STONE -> "guigolemstone2";
            case DECANTING -> "guigolemtallowadv";
            case IRON_GUARDIAN -> "guigolemiron";
        } + ".png");
        this.quote = inventory.player.getRandom().nextInt(golem.kind() == GolemKind.DECANTING ? 7 : 6);
    }

    private boolean smart() {
        return golem.getCore() == 2;
    }

    private void blit(GuiGraphicsExtractor graphics, int x, int y, int u, int v, int width, int height) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, leftPos + x, topPos + y, u, v, width, height, 256, 256);
    }

    private void swatch(GuiGraphicsExtractor graphics, int x, int y, int color) {
        if (color >= 0 && color < MARKER_COLORS.length) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, texture, leftPos + x, topPos + y, 200.0F, 0.0F, 6, 6, 256, 256, ARGB.opaque(MARKER_COLORS[color]));
        }
    }

    private static Component colorName(int color) {
        return color >= 0 && color < ConfigRecipes.WOOL_COLORS.length
            ? Component.translatable("color.minecraft." + ConfigRecipes.WOOL_COLORS[color])
            : Component.translatable("tc.thaumcraft.golem.gui.any_color");
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }

    private void centered(GuiGraphicsExtractor graphics, Component text, int centerX, int y, int color) {
        graphics.text(font, text, leftPos + centerX - font.width(text) / 2, topPos + y, color, true);
    }

    private void wrapped(GuiGraphicsExtractor graphics, String key, int x, int y, int width) {
        graphics.textWithWordWrap(font, Component.translatable(key), leftPos + x, topPos + y, width, TEXT, true);
    }

    private void toggleLabel(GuiGraphicsExtractor graphics, boolean toggled, int indicatorX, int indicatorY, int textX, int textY) {
        blit(graphics, toggled ? indicatorX + 5 : indicatorX, indicatorY, 176, 72, 5, 5);
        Component text = Component.translatable(toggled ? "tc.thaumcraft.golem.gui.any_amount" : "tc.thaumcraft.golem.gui.precise_amount");
        graphics.pose().pushMatrix();
        graphics.pose().translate(leftPos + textX, topPos + textY);
        graphics.pose().scale(0.5F, 0.5F);
        graphics.text(font, text, -font.width(text), 0, LABEL, true);
        graphics.pose().popMatrix();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        blit(graphics, 0, 0, 0, 0, imageWidth, imageHeight);
        switch (golem.kind()) {
            case WOOD, STRAW -> {
                if (smart()) {
                    blit(graphics, 138, 10, 176, 72, 28, 56);
                }
            }
            case CLAY, STONE -> {
                swatch(graphics, 147, 11, golem.getColor());
                if (smart()) {
                    blit(graphics, 124, 43, 176, 40, 52, 26);
                }
                if (golem instanceof ClayGolem clay) {
                    toggleLabel(graphics, clay.isToggled(), 106, 54, 100, 55);
                }
            }
            case TALLOW -> swatch(graphics, 149, 11, golem.getColor());
            case ADVANCED_CLAY, ADVANCED_STONE -> {
                if (golem instanceof MultiColorGolem multi) {
                    for (int column = 0; column < 3; column++) {
                        for (int row = 0; row < 2; row++) {
                            swatch(graphics, 102 + column * 28, 6 + row * 37, multi.getSlotColor(row * 3 + column));
                        }
                    }
                }
                if (golem instanceof AdvancedClayGolem clay) {
                    toggleLabel(graphics, clay.isToggled(), 76, 66, 70, 67);
                }
            }
            case DECANTING -> extractDecanting(graphics);
            case IRON_GUARDIAN -> {
                if (smart() && golem instanceof IronGuardianGolem guardian) {
                    for (int index = 0; index < 4; index++) {
                        blit(graphics, 124, 5 + 16 * index, 176, guardian.hasTargetFlag(TARGET_FLAGS[index]) ? 16 : 0, 13, 13);
                        graphics.text(font, Component.translatable("tc.thaumcraft.golem.gui." + TARGET_NAMES[index]), leftPos + 142, topPos + 7 + 16 * index, TARGET_COLORS[index], true);
                    }
                }
            }
        }
        InventoryScreen.extractEntityInInventoryFollowsMouse(graphics, leftPos + 26, topPos + 8, leftPos + 76, topPos + 78, 45, 0.0625F, mouseX, mouseY, golem);
    }

    private void extractDecanting(GuiGraphicsExtractor graphics) {
        DecantingGolem decanting = (DecantingGolem) golem;
        swatch(graphics, 149, 18, golem.getColor());
        if (decanting.getTallowType() == 0) {
            blit(graphics, 131, 30, 200, 88, 42, 12);
            Fluid watched = decanting.getWatchedLiquid();
            Component name;
            if (watched != null) {
                ItemStack bucket = new ItemStack(watched.getBucket());
                if (!bucket.isEmpty()) {
                    graphics.item(bucket, leftPos + 144, topPos + 29);
                }
                name = watched.getFluidType().getDescription();
            } else {
                name = Component.translatable("tc.thaumcraft.golem.gui.any_liquid");
            }
            centered(graphics, name, 152, 53, LABEL);
            blit(graphics, 143, 28, 200, watched == null ? 40 : 16, 18, 18);
        } else {
            graphics.pose().pushMatrix();
            graphics.pose().translate(leftPos + 140, topPos + 24);
            graphics.pose().scale(1.5F, 1.5F);
            graphics.item(new ItemStack(ModItems.WARDED_JAR.get()), 0, 0);
            graphics.pose().popMatrix();
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        int x = mouseX - leftPos;
        int y = mouseY - topPos;
        switch (golem.kind()) {
            case WOOD, STRAW -> graphics.textWithWordWrap(
                font,
                Component.translatable(smart() ? "tc.thaumcraft.golem.gui.wood_smart" : "tc.thaumcraft.golem.gui.wood_simple"),
                40,
                11,
                smart() ? 96 : 130,
                TEXT,
                true
            );
            case CLAY, STONE -> {
                graphics.textWithWordWrap(font, Component.translatable("tc.thaumcraft.golem.gui." + (golem.kind() == GolemKind.CLAY ? "clay" : "stone")), 40, 11, 96, TEXT, true);
                if (inside(x, y, 139, 10, 22, 8)) {
                    Component name = colorName(golem.getColor());
                    graphics.text(font, name, 150 - font.width(name) / 2, -1, LABEL, true);
                }
            }
            case TALLOW -> {
                int type = golem instanceof TallowGolem tallow ? tallow.getTallowType() : 0;
                graphics.textWithWordWrap(font, Component.translatable(type == 0 ? "tc.thaumcraft.golem.gui.tallow_crucible" : "tc.thaumcraft.golem.gui.tallow_alembic"), 40, 11, 96, TEXT, true);
                if (inside(x, y, 141, 10, 22, 8)) {
                    Component name = colorName(golem.getColor());
                    graphics.text(font, name, 152 - font.width(name) / 2, -1, LABEL, true);
                }
            }
            case ADVANCED_CLAY, ADVANCED_STONE -> {
                graphics.pose().pushMatrix();
                graphics.pose().translate(39.0F, 10.0F);
                graphics.pose().scale(0.75F, 0.75F);
                graphics.textWithWordWrap(font, Component.translatable("tc.thaumcraft.golem.gui.quote." + quote), 0, 0, 72, TEXT, true);
                graphics.pose().popMatrix();
                if (golem instanceof MultiColorGolem multi) {
                    for (int column = 0; column < 3; column++) {
                        for (int row = 0; row < 2; row++) {
                            if (inside(x, y, 94 + column * 28, 5 + row * 37, 22, 8)) {
                                Component name = colorName(multi.getSlotColor(row * 3 + column));
                                graphics.text(font, name, 133 - font.width(name) / 2, -6, LABEL, true);
                            }
                        }
                    }
                }
            }
            case DECANTING -> {
                graphics.textWithWordWrap(font, Component.translatable("tc.thaumcraft.golem.gui.decanting_quote." + quote), 40, 11, 90, TEXT, true);
                if (inside(x, y, 141, 17, 22, 8)) {
                    Component name = colorName(golem.getColor());
                    graphics.text(font, name, 152 - font.width(name) / 2, 6, LABEL, true);
                }
            }
            case IRON_GUARDIAN -> graphics.textWithWordWrap(
                font,
                Component.translatable(smart() ? "tc.thaumcraft.golem.gui.iron_smart" : "tc.thaumcraft.golem.gui.iron_simple"),
                40,
                11,
                smart() ? 80 : 130,
                TEXT,
                true
            );
        }
    }

    private boolean click(int button) {
        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, button);
        return true;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double x = event.x() - leftPos;
        double y = event.y() - topPos;
        switch (golem.kind()) {
            case CLAY, STONE -> {
                if (inside(x, y, 139, 10, 8, 8)) {
                    return click(0);
                }
                if (inside(x, y, 153, 10, 8, 8)) {
                    return click(1);
                }
                if (golem.kind() == GolemKind.CLAY && inside(x, y, 102, 50, 18, 13)) {
                    return click(2);
                }
            }
            case TALLOW -> {
                if (inside(x, y, 141, 10, 8, 8)) {
                    return click(0);
                }
                if (inside(x, y, 155, 10, 8, 8)) {
                    return click(1);
                }
            }
            case ADVANCED_CLAY, ADVANCED_STONE -> {
                for (int column = 0; column < 3; column++) {
                    for (int row = 0; row < 2; row++) {
                        if (inside(x, y, 94 + column * 28, 5 + row * 37, 8, 8)) {
                            return click(row * 3 + column);
                        }
                        if (inside(x, y, 108 + column * 28, 5 + row * 37, 8, 8)) {
                            return click(row * 3 + column + 6);
                        }
                    }
                }
                if (golem.kind() == GolemKind.ADVANCED_CLAY && inside(x, y, 72, 62, 18, 13)) {
                    return click(22);
                }
            }
            case DECANTING -> {
                if (inside(x, y, 141, 17, 8, 8)) {
                    return click(0);
                }
                if (inside(x, y, 155, 17, 8, 8)) {
                    return click(1);
                }
                if (((DecantingGolem) golem).getTallowType() == 0) {
                    if (inside(x, y, 132, 31, 10, 10)) {
                        return click(2);
                    }
                    if (inside(x, y, 162, 31, 10, 10)) {
                        return click(3);
                    }
                }
            }
            case IRON_GUARDIAN -> {
                if (smart()) {
                    for (int index = 0; index < 4; index++) {
                        if (inside(x, y, 124, 5 + 16 * index, 13, 13)) {
                            return click(index);
                        }
                    }
                }
            }
            default -> {
            }
        }
        return super.mouseClicked(event, doubleClick);
    }
}
