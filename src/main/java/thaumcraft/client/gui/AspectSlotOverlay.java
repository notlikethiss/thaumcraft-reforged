package thaumcraft.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import thaumcraft.Config;
import thaumcraft.Thaumcraft;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectHelper;
import thaumcraft.aspect.AspectList;

@EventBusSubscriber(modid = Thaumcraft.MODID, value = Dist.CLIENT)
public final class AspectSlotOverlay {
    private AspectSlotOverlay() {
    }

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen)) {
            return;
        }
        boolean shift = Minecraft.getInstance().hasShiftDown();
        boolean showTags = Config.DISPLAY_ASPECTS.getAsBoolean();
        if (shift == showTags) {
            return;
        }
        Slot slot = screen.getHoveredSlot();
        if (slot == null || !slot.hasItem()) {
            return;
        }
        AspectList aspects = AspectHelper.getBonusTags(slot.getItem(), AspectHelper.getObjectTags(slot.getItem()));
        GuiGraphicsExtractor graphics = event.getGuiGraphics();
        graphics.nextStratum();
        int index = 0;
        for (Aspect aspect : aspects.getAspectsSorted()) {
            int x = event.getMouseX() + 17 + index * 18 - 8;
            int y = event.getMouseY() + 7 - 33 - 8;
            AspectRenderer.drawTag(graphics, x, y, aspect, aspects.getAmount(aspect), true, false);
            index++;
        }
    }
}
