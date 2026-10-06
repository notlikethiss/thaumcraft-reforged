package thaumcraft.client.research;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import javax.annotation.Nullable;
import thaumcraft.Config;
import thaumcraft.Thaumcraft;
import thaumcraft.client.gui.GuiDraw;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;
import thaumcraft.client.gui.AspectRenderer;
import thaumcraft.client.gui.TcFonts;
import thaumcraft.crafting.CrucibleRecipe;
import thaumcraft.crafting.RecipeReference;
import thaumcraft.crafting.TcIngredient;
import thaumcraft.crafting.ThaumcraftRecipes;
import thaumcraft.crafting.WorkbenchRecipe;
import thaumcraft.registry.ModSounds;
import thaumcraft.research.ResearchItem;
import thaumcraft.research.ResearchList;

public class ResearchPageScreen extends Screen {
    private static final ResourceLocation BOOK = Thaumcraft.id("textures/gui/gui_researchbook.png");
    private static final ResourceLocation OVERLAY = Thaumcraft.id("textures/gui/gui_researchbook_overlay.png");
    private static final int PANE_WIDTH = 256;
    private static final int PANE_HEIGHT = 181;
    private static final int TITLE_COLOR = 0xFF505050;
    private static final int TEXT_COLOR = 0xFF303030;

    private final ResearchItem research;
    private final double mapX;
    private final double mapY;
    private final List<ResearchTexts.Page> pages;
    private final List<Reference> references = new ArrayList<>();
    private int page;
    private long lastCycle;
    private int cycle = -1;
    private @Nullable List<Component> tooltip;
    private ItemStack tooltipStack = ItemStack.EMPTY;

    private record Reference(int x, int y, String key) {
    }

    private record GridRecipe(boolean shaped, int width, int height, List<List<ItemStack>> slots, List<ItemStack> output, int cost, AspectList aspects) {
    }

    public ResearchPageScreen(ResearchItem research, double mapX, double mapY) {
        super(Component.empty());
        this.research = research;
        this.mapX = mapX;
        this.mapY = mapY;
        ResearchTexts.Entry entry = ResearchTexts.get(research.key);
        this.pages = entry == null ? List.of() : entry.pages();
    }

    public static boolean hasPages(String key) {
        ResearchTexts.Entry entry = ResearchTexts.get(key);
        return entry != null && !entry.pages().isEmpty();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderTransparentBackground(graphics);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == InputConstants.KEY_ESCAPE || minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            minecraft.setScreen(new ResearchBookScreen(mapX, mapY));
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        tooltip = null;
        tooltipStack = ItemStack.EMPTY;
        references.clear();
        int sw = (width - PANE_WIDTH) / 2;
        int sh = (height - PANE_HEIGHT) / 2;
        graphics.pose().pushPose();
        graphics.pose().translate((width - PANE_WIDTH * 1.3F) / 2.0F, (height - PANE_HEIGHT * 1.3F) / 2.0F, 0.0F);
        graphics.pose().scale(1.3F, 1.3F, 1.0F);
        GuiDraw.blit(graphics, BOOK, 0, 0, 0.0F, 0.0F, PANE_WIDTH, PANE_HEIGHT, 256, 256);
        graphics.pose().popPose();
        for (int index = page; index <= page + 1 && index < pages.size(); index++) {
            drawPage(graphics, pages.get(index), index % 2, sw, sh, mouseX, mouseY);
        }
        float bob = Mth.sin(minecraft.player.tickCount / 3.0F) * 0.2F + 0.1F;
        if (page > 0) {
            boolean hover = inside(mouseX, mouseY, sw - 17, sh + 189, 14, 10);
            drawArrow(graphics, sw - 16, sh + 190, 0.0F, hover ? 1.0F : 0.6F + bob * 2.0F, bob);
        }
        if (page < pages.size() - 2) {
            boolean hover = inside(mouseX, mouseY, sw + 261, sh + 189, 14, 10);
            drawArrow(graphics, sw + 262, sh + 190, 12.0F, hover ? 1.0F : 0.6F + bob * 2.0F, bob);
        }
        if (tooltip != null) {
            if (tooltipStack.isEmpty()) {
                graphics.renderTooltip(font, tooltip, Optional.empty(), mouseX, mouseY);
            } else {
                graphics.renderTooltip(font, tooltip, tooltipStack.getTooltipImage(), mouseX, mouseY);
            }
        }
    }

    private void drawArrow(GuiGraphics graphics, int x, int y, float u, float alpha, float scale) {
        graphics.pose().pushPose();
        graphics.pose().translate(x + 6.0F, y + 4.0F, 0.0F);
        graphics.pose().scale(1.0F + scale, 1.0F + scale, 1.0F);
        GuiDraw.blit(graphics, BOOK, -6, -4, u, 184.0F, 12, 8, 256, 256, GuiDraw.whiteAlpha(Mth.clamp(alpha, 0.0F, 1.0F)));
        graphics.pose().popPose();
    }

    private static boolean inside(int mouseX, int mouseY, int x, int y, int width, int height) {
        int mx = mouseX - x;
        int my = mouseY - y;
        return mx >= 0 && my >= 0 && mx < width && my < height;
    }

    private void drawPage(GuiGraphics graphics, ResearchTexts.Page node, int side, int x, int y, int mouseX, int mouseY) {
        if (lastCycle < System.currentTimeMillis()) {
            cycle++;
            lastCycle = System.currentTimeMillis() + 1000L;
        }
        if (page == 0 && side == 0) {
            GuiDraw.blit(graphics, BOOK, x + 4, y - 13, 24.0F, 184.0F, 96, 4, 256, 256);
            GuiDraw.blit(graphics, BOOK, x + 4, y + 4, 24.0F, 184.0F, 96, 4, 256, 256);
            Component name = Component.literal(ResearchTexts.name(research.key));
            int nameWidth = font.width(name);
            if (nameWidth <= 130) {
                graphics.drawString(font, name, x + 52 - nameWidth / 2, y - 6, TITLE_COLOR, false);
            } else {
                float scale = 130.0F / nameWidth;
                graphics.pose().pushPose();
                graphics.pose().translate(x + 52 - nameWidth / 2.0F * scale, y - 6.0F * scale, 0.0F);
                graphics.pose().scale(scale, scale, 1.0F);
                graphics.drawString(font, name, 0, 0, TITLE_COLOR, false);
                graphics.pose().popPose();
            }
            y += 25;
        }
        String content = node.content().trim();
        switch (node.type()) {
            case "text" -> drawTextPage(graphics, side, x, y - 10, node.content());
            case "crucible" -> drawCruciblePage(graphics, side, x - 4, y - 8, mouseX, mouseY, content);
            case "crafting" -> drawCraftingPage(graphics, side, x - 4, y - 8, mouseX, mouseY, content);
            case "arcanecrafting" -> drawArcanePage(graphics, side, x - 4, y - 8, mouseX, mouseY, content);
            case "infusioncrafting" -> drawInfusionPage(graphics, side, x - 4, y - 8, mouseX, mouseY, content);
            case "compoundcrafting" -> drawCompoundPage(graphics, side, x - 4, y - 8, mouseX, mouseY, content);
            default -> {
            }
        }
    }

    private void drawTextPage(GuiGraphics graphics, int side, int x, int y, String text) {
        List<FormattedCharSequence> lines = font.split(TcFonts.uniform(text.stripTrailing()), 139);
        for (int i = 0; i < lines.size(); i++) {
            graphics.drawString(font, lines.get(i), x - 15 + side * 152, y + i * font.lineHeight, TEXT_COLOR, false);
        }
    }

    private void drawTitle(GuiGraphics graphics, String key, int x, int y) {
        Component text = Component.translatable(key);
        graphics.drawString(font, text, x + 56 - font.width(text) / 2, y, TITLE_COLOR, false);
    }

    private void overlay(GuiGraphics graphics, int x, int y, int drawX, int drawY, float u, float v, int width, int height) {
        overlay(graphics, x, y, 0.0F, drawX, drawY, u, v, width, height);
    }

    private void overlay(GuiGraphics graphics, int x, int y, float z, int drawX, int drawY, float u, float v, int width, int height) {
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, z);
        graphics.pose().scale(2.0F, 2.0F, 1.0F);
        GuiDraw.blit(graphics, OVERLAY, drawX, drawY, u, v, width, height, 256, 256);
        graphics.pose().popPose();
    }

    private static @Nullable RecipeReference reference(String key) {
        List<RecipeReference> references = ThaumcraftRecipes.researchRecipes().get(key);
        return references == null || references.isEmpty() ? null : references.get(0);
    }

    private static List<ItemStack> stacks(@Nullable TcIngredient ingredient) {
        return ingredient == null ? List.of() : ingredient.displayStacks();
    }

    private static @Nullable GridRecipe grid(@Nullable RecipeReference reference) {
        if (reference instanceof RecipeReference.Workbench(WorkbenchRecipe recipe)) {
            List<List<ItemStack>> slots = new ArrayList<>();
            for (TcIngredient ingredient : recipe.ingredients()) {
                slots.add(stacks(ingredient));
            }
            return new GridRecipe(recipe.shaped(), recipe.width(), recipe.height(), slots, List.of(recipe.output().create()), recipe.cost(), recipe.aspects());
        }
        if (reference instanceof RecipeReference.FakeShaped fake) {
            List<List<ItemStack>> slots = new ArrayList<>();
            for (TcIngredient ingredient : fake.ingredients()) {
                slots.add(stacks(ingredient));
            }
            return new GridRecipe(true, fake.width(), fake.height(), slots, List.of(fake.output().create()), fake.cost(), new AspectList());
        }
        return null;
    }

    private @Nullable GridRecipe vanillaGrid(@Nullable RecipeReference reference) {
        if (!(reference instanceof RecipeReference.Vanilla(String recipeId)) || minecraft.level == null) {
            return null;
        }
        RecipeHolder<?> holder = ClientRecipes.get(recipeId);
        if (holder == null) {
            return null;
        }
        RegistryAccess registryAccess = minecraft.level.registryAccess();
        Recipe<?> recipe = holder.value();
        List<ItemStack> output = List.of(recipe.getResultItem(registryAccess));
        if (recipe instanceof ShapedRecipe shaped) {
            return new GridRecipe(true, shaped.getWidth(), shaped.getHeight(), resolve(shaped.getIngredients()), output, 0, new AspectList());
        }
        if (recipe instanceof ShapelessRecipe shapeless) {
            return new GridRecipe(false, 3, 3, resolve(shapeless.getIngredients()), output, 0, new AspectList());
        }
        return null;
    }

    private static List<List<ItemStack>> resolve(List<Ingredient> ingredients) {
        List<List<ItemStack>> result = new ArrayList<>();
        for (Ingredient ingredient : ingredients) {
            result.add(List.of(ingredient.getItems()));
        }
        return result;
    }

    private ItemStack cycle(List<ItemStack> stacks) {
        if (stacks.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return stacks.get((int) (System.currentTimeMillis() / 1000L % stacks.size()));
    }

    private void drawItem(GuiGraphics graphics, List<ItemStack> stacks, int x, int y, int mouseX, int mouseY, boolean output) {
        ItemStack stack = cycle(stacks);
        if (stack.isEmpty()) {
            return;
        }
        graphics.renderItem(stack, x, y);
        if (output) {
            graphics.renderItemDecorations(font, stack, x, y);
        }
        if (!inside(mouseX, mouseY, x, y, 16, 16)) {
            return;
        }
        List<Component> lines = new ArrayList<>(getTooltipFromItem(minecraft, stack));
        if (!output) {
            String key = findRecipeReference(stack);
            if (!key.isEmpty()) {
                lines.add(Component.translatable("tc.thaumcraft.click_for_research").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
                references.add(new Reference(x, y, key));
            }
        }
        tooltip = lines;
        tooltipStack = stack;
    }

    private void drawAspect(GuiGraphics graphics, Aspect aspect, int amount, int x, int y, int mouseX, int mouseY) {
        AspectRenderer.drawTag(graphics, x, y, aspect, amount, false, true);
        if (inside(mouseX, mouseY, x, y, 16, 16)) {
            tooltip = List.of(aspect.getDisplayName(), aspect.getMeaning());
            tooltipStack = ItemStack.EMPTY;
        }
    }

    private void drawCost(GuiGraphics graphics, int cost, int x, int y) {
        graphics.drawString(font, Component.translatable("tc.thaumcraft.vis_amount", cost), x, y, TITLE_COLOR, false);
    }

    private static String findRecipeReference(ItemStack stack) {
        CrucibleRecipe crucible = ThaumcraftRecipes.getCrucibleRecipe(stack);
        if (crucible != null) {
            return crucible.key();
        }
        for (WorkbenchRecipe recipe : ThaumcraftRecipes.workbenchRecipes()) {
            if (recipe.output().isResolvable() && stack.is(recipe.output().item())) {
                return recipe.key();
            }
        }
        return "";
    }

    private void drawGrid(GuiGraphics graphics, GridRecipe recipe, int x, int y, int shapedY, int shapelessY, int mouseX, int mouseY) {
        if (recipe.shaped()) {
            for (int i = 0; i < recipe.width() && i < 3; i++) {
                for (int j = 0; j < recipe.height() && j < 3; j++) {
                    int index = i + j * recipe.width();
                    if (index < recipe.slots().size()) {
                        drawItem(graphics, recipe.slots().get(index), x + 16 + i * 32, y + shapedY + j * 32, mouseX, mouseY, false);
                    }
                }
            }
        } else {
            for (int i = 0; i < recipe.slots().size() && i < 9; i++) {
                drawItem(graphics, recipe.slots().get(i), x + 16 + i % 3 * 32, y + shapelessY + i / 3 * 32, mouseX, mouseY, false);
            }
        }
    }

    private void drawArcanePage(GuiGraphics graphics, int side, int x, int y, int mouseX, int mouseY, String key) {
        GridRecipe recipe = grid(reference(key));
        if (recipe == null) {
            return;
        }
        int start = side * 152;
        drawTitle(graphics, "tc.thaumcraft.page.arcane", x + start, y);
        overlay(graphics, x + start, y, 2, 32, 112.0F, 15.0F, 52, 52);
        overlay(graphics, x + start, y, 20, 12, 20.0F, 3.0F, 16, 16);
        overlay(graphics, x + start + 76, y + 28, 0, 0, 68.0F, 76.0F, 12, 12);
        drawItem(graphics, recipe.output(), x + 48 + start, y + 32, mouseX, mouseY, true);
        drawCost(graphics, recipe.cost(), x + 86 + start, y + 43);
        drawGrid(graphics, recipe, x + start, y, 76, 68, mouseX, mouseY);
        if ((key.equals("WARDEDSTONE") || key.equals("WARDEDGLASS") || key.equals("ARCANEDOOR")) && !Config.wardedStone()) {
            graphics.pose().pushPose();
            graphics.pose().translate(x + start + 30, y + 80, 0.0F);
            graphics.pose().mulPose(Axis.ZP.rotation(Mth.PI / 4.0F));
            graphics.pose().scale(2.0F, 2.0F, 1.0F);
            graphics.drawString(font, Component.translatable("tc.thaumcraft.nerfed"), 0, 0, 0xFFFF5050, false);
            graphics.pose().popPose();
        }
    }

    private void drawInfusionPage(GuiGraphics graphics, int side, int x, int y, int mouseX, int mouseY, String key) {
        RecipeReference reference;
        if (key.startsWith("C_")) {
            reference = reference(key + cycle);
            if (reference == null) {
                cycle = 0;
                reference = reference(key + cycle);
            }
        } else {
            reference = reference(key);
        }
        GridRecipe recipe = grid(reference);
        if (recipe == null) {
            return;
        }
        int start = side * 152;
        drawTitle(graphics, "tc.thaumcraft.page.infusion", x + start, y);
        overlay(graphics, x + start, y, 1, 28, 112.0F, 15.0F, 56, 64);
        overlay(graphics, x + start, y, 20, 8, 20.0F, 3.0F, 16, 16);
        overlay(graphics, x + start + 76, y + 20, 0, 0, 68.0F, 76.0F, 12, 12);
        drawItem(graphics, recipe.output(), x + 48 + start, y + 24, mouseX, mouseY, true);
        drawCost(graphics, recipe.cost(), x + 86 + start, y + 37);
        drawGrid(graphics, recipe, x + start, y, 68, 68, mouseX, mouseY);
        AspectList tags = recipe.aspects();
        int count = 0;
        for (Aspect aspect : tags.getAspects()) {
            drawAspect(graphics, aspect, tags.getAmount(aspect), x + start + 12 + 18 * count + (5 - tags.size()) * 8, y + 167, mouseX, mouseY);
            count++;
        }
    }

    private void drawCraftingPage(GuiGraphics graphics, int side, int x, int y, int mouseX, int mouseY, String key) {
        GridRecipe recipe = vanillaGrid(reference(key));
        if (recipe == null) {
            return;
        }
        int start = side * 152;
        drawTitle(graphics, recipe.shaped() ? "tc.thaumcraft.page.workbench" : "tc.thaumcraft.page.workbench_shapeless", x + start, y);
        overlay(graphics, x + start, y, 2, 32, 60.0F, 15.0F, 52, 52);
        overlay(graphics, x + start, y, 20, 12, 20.0F, 3.0F, 16, 16);
        drawItem(graphics, recipe.output(), x + 48 + start, y + 32, mouseX, mouseY, true);
        drawGrid(graphics, recipe, x + start, y, 76, 76, mouseX, mouseY);
    }

    private void drawCruciblePage(GuiGraphics graphics, int side, int x, int y, int mouseX, int mouseY, String key) {
        CrucibleRecipe recipe = ThaumcraftRecipes.getCrucibleRecipe(key);
        if (recipe == null) {
            return;
        }
        int start = side * 152;
        drawTitle(graphics, "tc.thaumcraft.page.crucible", x + start, y);
        overlay(graphics, x + start, y + 28, 0, 0, 0.0F, 3.0F, 56, 64);
        drawItem(graphics, List.of(recipe.output().create()), x + 48 + start, y + 36, mouseX, mouseY, true);
        drawCost(graphics, recipe.cost(), x + 76 + start, y + 41);
        AspectList tags = recipe.tags();
        int rows = (tags.size() - 1) / 3;
        int shift = (3 - tags.size() % 3) * 10;
        int sx = x + start + 28;
        int sy = y + 96 - 10 * rows;
        int total = 0;
        for (Aspect aspect : tags.getAspectsSorted()) {
            int m = total / 3 >= rows && (rows > 1 || tags.size() < 3) ? 1 : 0;
            drawAspect(graphics, aspect, tags.getAmount(aspect), sx + total % 3 * 20 + shift * m, sy + total / 3 * 20, mouseX, mouseY);
            total++;
        }
    }

    private void drawCompoundPage(GuiGraphics graphics, int side, int x, int y, int mouseX, int mouseY, String key) {
        if (!(reference(key) instanceof RecipeReference.Compound compound)) {
            return;
        }
        int dx = compound.width();
        int dy = compound.height();
        int dz = compound.depth();
        int xoff = 64 - (dx * 16 + dz * 16) / 2;
        int yoff = -dy * 25;
        int start = side * 152;
        int base = Math.max(3 - dx, 3 - dz) * 8 + yoff + dx * 4 + dz * 4 + dy * 50;
        drawTitle(graphics, "tc.thaumcraft.page.compound", x + start, y);
        overlay(graphics, x + start - 8, y - 3 + base, 0, 0, 0.0F, 72.0F, 64, 44);
        if (compound.cost() > 0) {
            overlay(graphics, x + start - 5, y + 69 + base, 0, 0, 68.0F, 76.0F, 12, 12);
            drawCost(graphics, compound.cost(), x + start + 5, y + 84 + base);
        }
        List<String> blocks = compound.blocks();
        for (int pass = 0; pass < 3; pass++) {
            int count = 0;
            for (int j = 0; j < dy; j++) {
                for (int k = dz - 1; k >= 0; k--) {
                    for (int i = dx - 1; i >= 0; i--) {
                        int px = x + start + xoff + i * 16 + k * 16;
                        int py = y + 116 + yoff - i * 8 + k * 8 + j * 50;
                        boolean pillar = j < dy - 1 && (k == dz - 1 && i == dx - 1 || i == 0 && k == 0 || i == dx - 1 && k == 0 || k == dz - 1 && i == 0);
                        boolean front = k == dz - 1 && i == 0;
                        if (pass == 0 && pillar && !front || pass == 2 && pillar && front) {
                            overlay(graphics, px + 7, py - 10, pass == 2 ? 200.0F : 0.0F, 0, 0, 80.0F, 76.0F, 2, 30);
                        }
                        String id = count < blocks.size() ? blocks.get(count) : null;
                        if (pass == 1 && id != null) {
                            drawItem(graphics, List.of(RecipeReference.stack(id)), px, py, mouseX, mouseY, false);
                        }
                        count++;
                    }
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(double clickX, double clickY, int button) {
        int mouseX = (int) clickX;
        int mouseY = (int) clickY;
        int sw = (width - PANE_WIDTH) / 2;
        int sh = (height - PANE_HEIGHT) / 2;
        if (page < pages.size() - 2 && inside(mouseX, mouseY, sw + 261, sh + 189, 14, 10)) {
            turnPage(2);
            return true;
        }
        if (page >= 2 && inside(mouseX, mouseY, sw - 17, sh + 189, 14, 10)) {
            turnPage(-2);
            return true;
        }
        for (Reference reference : references) {
            if (inside(mouseX, mouseY, reference.x(), reference.y(), 16, 16)) {
                ResearchItem target = ResearchList.getResearch(reference.key());
                if (target != null && hasPages(target.key)) {
                    minecraft.setScreen(new ResearchPageScreen(target, mapX, mapY));
                    return true;
                }
            }
        }
        return super.mouseClicked(clickX, clickY, button);
    }

    private void turnPage(int delta) {
        page += delta;
        lastCycle = 0L;
        cycle = -1;
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(ModSounds.PAGE.get(), 1.0F, 0.66F));
    }
}
