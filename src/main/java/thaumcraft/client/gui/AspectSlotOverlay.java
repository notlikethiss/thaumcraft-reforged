package thaumcraft.client.gui;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
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
        boolean shift = Screen.hasShiftDown();
        boolean showTags = Config.DISPLAY_ASPECTS.getAsBoolean();
        if (shift == showTags) {
            return;
        }
        Slot slot = screen.getSlotUnderMouse();
        if (slot == null || !slot.hasItem()) {
            return;
        }
        AspectList aspects = AspectHelper.getBonusTags(slot.getItem(), AspectHelper.getObjectTags(slot.getItem()));
        GuiGraphics graphics = event.getGuiGraphics();
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 400.0F);
        int index = 0;
        for (Aspect aspect : aspects.getAspectsSorted()) {
            int x = event.getMouseX() + 17 + index * 18 - 8;
            int y = event.getMouseY() + 7 - 33 - 8;
            AspectRenderer.drawTag(graphics, x, y, aspect, aspects.getAmount(aspect), true, false);
            index++;
        }
        graphics.pose().popPose();
    }
}
