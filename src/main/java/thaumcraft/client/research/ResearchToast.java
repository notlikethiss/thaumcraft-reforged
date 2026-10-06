package thaumcraft.client.research;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import thaumcraft.registry.ModSounds;
import thaumcraft.research.ResearchItem;
import thaumcraft.research.ResearchList;

public class ResearchToast implements Toast {
    private static final ResourceLocation BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("toast/advancement");
    private static final Component TITLE = Component.translatable("tc.thaumcraft.research_learned");
    private static final long DISPLAY_TIME = 5000L;

    private final Component name;
    private final ItemStack icon;
    private final int width;
    private Toast.Visibility visibility = Toast.Visibility.SHOW;

    public ResearchToast(String key) {
        this.name = Component.literal(ResearchTexts.name(key));
        ResearchItem research = ResearchList.getResearch(key);
        this.icon = research == null ? ItemStack.EMPTY : research.getIconStack();
        Font font = Minecraft.getInstance().font;
        this.width = Math.max(DEFAULT_WIDTH, 38 + Math.max(font.width(TITLE), font.width(name)));
    }

    @Override
    public Toast.Visibility getWantedVisibility() {
        return visibility;
    }

    @Override
    public void update(ToastManager manager, long fullyVisibleForMs) {
        visibility = fullyVisibleForMs >= DISPLAY_TIME * manager.getNotificationDisplayTimeMultiplier()
            ? Toast.Visibility.HIDE
            : Toast.Visibility.SHOW;
    }

    @Override
    public SoundEvent getSoundEvent() {
        return ModSounds.LEARN.value();
    }

    @Override
    public int width() {
        return width;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, Font font, long fullyVisibleForMs) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND_SPRITE, 0, 0, width(), height());
        graphics.text(font, TITLE, 30, 7, 0xFFD58FFF, false);
        graphics.text(font, name, 30, 18, 0xFFFFFFFF, false);
        if (!icon.isEmpty()) {
            graphics.fakeItem(icon, 8, 8);
        }
    }
}
