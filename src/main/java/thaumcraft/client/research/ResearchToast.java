package thaumcraft.client.research;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import thaumcraft.registry.ModSounds;
import thaumcraft.research.ResearchItem;
import thaumcraft.research.ResearchList;

public class ResearchToast implements Toast {
    private static final ResourceLocation BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("toast/advancement");
    private static final Component TITLE = Component.translatable("tc.thaumcraft.research_learned");
    private static final long DISPLAY_TIME = 5000L;
    private static final int DEFAULT_WIDTH = 160;

    private final Component name;
    private final ItemStack icon;
    private final int width;
    private boolean soundPlayed;

    public ResearchToast(String key) {
        this.name = Component.literal(ResearchTexts.name(key));
        ResearchItem research = ResearchList.getResearch(key);
        this.icon = research == null ? ItemStack.EMPTY : research.getIconStack();
        Font font = Minecraft.getInstance().font;
        this.width = Math.max(DEFAULT_WIDTH, 38 + Math.max(font.width(TITLE), font.width(name)));
    }

    @Override
    public int width() {
        return width;
    }

    @Override
    public Toast.Visibility render(GuiGraphics graphics, ToastComponent toastComponent, long timeSinceLastVisible) {
        Font font = toastComponent.getMinecraft().font;
        if (!soundPlayed) {
            soundPlayed = true;
            toastComponent.getMinecraft().getSoundManager().play(SimpleSoundInstance.forUI(ModSounds.LEARN.value(), 1.0F, 1.0F));
        }
        graphics.blitSprite(BACKGROUND_SPRITE, 0, 0, width(), height());
        graphics.drawString(font, TITLE, 30, 7, 0xFFD58FFF, false);
        graphics.drawString(font, name, 30, 18, 0xFFFFFFFF, false);
        if (!icon.isEmpty()) {
            graphics.renderFakeItem(icon, 8, 8);
        }
        return timeSinceLastVisible >= DISPLAY_TIME * toastComponent.getNotificationDisplayTimeMultiplier()
            ? Toast.Visibility.HIDE
            : Toast.Visibility.SHOW;
    }
}
