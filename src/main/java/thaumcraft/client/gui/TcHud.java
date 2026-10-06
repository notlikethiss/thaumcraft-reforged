package thaumcraft.client.gui;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.Util;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import thaumcraft.client.aura.AuraClientData;
import thaumcraft.client.render.AuraNodeRenderer;
import thaumcraft.item.armor.Hover;
import thaumcraft.item.wand.CastingWandItem;
import thaumcraft.item.wand.EqualTradeWandItem;
import thaumcraft.item.wand.HellrodItem;
import thaumcraft.item.wand.WandManager;

public final class TcHud {
    private TcHud() {
    }

    public static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.level == null || minecraft.screen != null) {
            return;
        }
        ItemStack harness = Hover.getHarness(player);
        if (!harness.isEmpty()) {
            renderHover(graphics, harness);
        }
        if (AuraNodeRenderer.hasGoggles(player)) {
            renderGoggles(graphics, minecraft, player);
        }
        ItemStack held = player.getMainHandItem();
        if (held.getItem() instanceof CastingWandItem wand) {
            Integer vis = CastingWandItem.getVis(held);
            if (vis != null) {
                renderCastingWand(graphics, minecraft, player, vis, wand.getMaxVis());
            }
        } else if (held.getItem() instanceof HellrodItem) {
            renderHellrod(graphics, player, HellrodItem.getCharges(held));
        } else if (held.getItem() instanceof EqualTradeWandItem) {
            BlockState picked = EqualTradeWandItem.getPickedBlock(held);
            if (picked != null) {
                renderEqualTrade(graphics, minecraft, player, new ItemStack(picked.getBlock().asItem()));
            }
        }
    }

    private static void renderHover(GuiGraphics graphics, ItemStack armor) {
        int height = graphics.guiHeight();
        int level = Math.round(Hover.getFuel(armor) / 64.0F * 48.0F);
        if (level > 0) {
            GuiDraw.blit(graphics, AspectRenderer.PARTICLES, 6, height / 2 + 24 - level, 224.0F, (float) (48 - level), 8, level, 256, 256, FastColor.ARGB32.colorFromFloat(1.0F, 0.0F, 1.0F, 0.75F));
        }
        blit(graphics, 5, height / 2 - 28, 240, 0, 10, 56);
        if (Hover.isHovering(armor)) {
            int frame = (int) (Util.getMillis() % 700L) / 50;
            GuiDraw.blit(graphics, AspectRenderer.PARTICLES, 2, height / 2 - 43, 16.0F * frame, 32.0F, 16, 16, 256, 256, GuiDraw.whiteAlpha(0.66F));
        }
        graphics.renderItem(armor, 2, height / 2 - 43);
    }

    private static void renderGoggles(GuiGraphics graphics, Minecraft minecraft, Player player) {
        int height = graphics.guiHeight();
        AuraClientData.ClientNode closest = null;
        double closestDistance = Double.MAX_VALUE;
        for (AuraClientData.ClientNode node : AuraClientData.NODES.values()) {
            if (node.dimension() != player.level().dimension()) {
                continue;
            }
            double distance = player.distanceToSqr(node.x(), node.y(), node.z());
            if (distance < closestDistance) {
                closestDistance = distance;
                closest = node;
            }
        }
        if (closest == null) {
            return;
        }
        Font font = minecraft.font;
        int barHeight = (int) (closest.level() / (closest.baseLevel() * 2.0F) * 48.0F);
        graphics.drawString(font, "A: " + closest.level() + "/" + closest.baseLevel(), 18, height - 28, 0xFFFFFFFF, true);
        String flux = "none";
        int color = 0x888888;
        if (closest.flux() > 0) {
            flux = "minimal";
            color = 0x8888AA;
        }
        if (closest.flux() > 50) {
            flux = "moderate";
            color = 0xAA8888;
        }
        if (closest.flux() > 150) {
            flux = "high";
            color = 0xFF8888;
        }
        if (closest.flux() > 500) {
            flux = "dangerous";
            color = 0xFF1111;
        }
        graphics.drawString(
            font,
            Component.translatable("tc.thaumcraft.hud.flux", Component.translatable("tc.thaumcraft.hud.flux." + flux)),
            18,
            height - 18,
            0xFF000000 | color,
            true
        );
        blit(graphics, 6, height - 9 - barHeight, 224, 48 - barHeight, 8, barHeight);
        blit(graphics, 5, height - 61, 240, 0, 10, 56);
        AuraClientData.History history = AuraClientData.HISTORY.get(closest.key());
        if (history == null) {
            return;
        }
        if (history.level() < closest.level()) {
            blit(graphics, 6, height - 37, 208, 0, 8, 8);
        } else if (history.level() > closest.level()) {
            blit(graphics, 6, height - 37, 216, 0, 8, 8);
        }
        if (history.flux() < closest.flux()) {
            long millis = Util.getMillis();
            blit(graphics, 2, height - (65 - (int) (millis % 1250L) / 50 * 2), 16 * ((int) (millis % 700L) / 50), 32, 16, 16);
        }
    }

    private static void renderCastingWand(GuiGraphics graphics, Minecraft minecraft, Player player, int vis, int maxVis) {
        int shift = player.isCreative() ? 6 : (player.isUnderWater() ? 29 : 20);
        int size = (int) ((float) vis / maxVis * 80.0F);
        Font font = minecraft.font;
        graphics.pose().pushPose();
        graphics.pose().translate(graphics.guiWidth() / 2 + 10, graphics.guiHeight() - 28.5F - shift, 0.0F);
        blit(graphics, 0, 0, 0, 248, size, 8);
        blit(graphics, size, 0, size, 240, 80 - size, 8);
        graphics.pose().translate(40.0F, 1.5F, 0.0F);
        graphics.pose().scale(0.5F, 0.5F, 1.0F);
        Component amount = Component.translatable("tc.thaumcraft.vis_amount", vis);
        graphics.drawString(font, amount, -font.width(amount) / 2, 1, 0xFFFFFFFF, true);
        int discount = WandManager.getTotalVisDiscount(player);
        if (discount > 0) {
            String text = "-" + discount + "%";
            graphics.drawString(font, text, 75 - font.width(text), 1, 0xFFFFFFFF, true);
        }
        graphics.pose().popPose();
    }

    private static void renderHellrod(GuiGraphics graphics, Player player, int charges) {
        int shift = player.isCreative() ? 6 : (player.isUnderWater() ? 29 : 20);
        graphics.pose().pushPose();
        graphics.pose().translate(graphics.guiWidth() / 2 + 10, graphics.guiHeight() - 29.0F - shift, 0.0F);
        graphics.pose().scale(0.525F, 0.525F, 1.0F);
        for (int index = 0; index < 9; index++) {
            float bob = Mth.sin((player.tickCount + index * 10) / 5.0F) + 1.0F;
            graphics.pose().pushPose();
            graphics.pose().translate(0.0F, bob, 0.0F);
            blit(graphics, index * 17, 0, 160 + (index >= charges ? 16 : 0), 0, 16, 16);
            graphics.pose().popPose();
        }
        graphics.pose().popPose();
    }

    private static void renderEqualTrade(GuiGraphics graphics, Minecraft minecraft, Player player, ItemStack picked) {
        int amount = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (ItemStack.isSameItemSameComponents(stack, picked)) {
                amount += stack.getCount();
            }
        }
        int slot = player.getInventory().selected * 20;
        int shift = player.isCreative() ? 0 : 20;
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        int frameU = 32 * (player.tickCount % 16);
        int frameV = 32 * (player.tickCount % 32 / 16);
        GuiDraw.blitAdditive(graphics, AspectRenderer.PARTICLES, width / 2 - 96 + slot, height - 58 - shift, frameU, 96 + frameV, 32, 32, 256, 256);
        graphics.renderItem(picked, width / 2 - 96 + 8 + slot, height - 50 - shift);
        Font font = minecraft.font;
        String text = String.valueOf(amount);
        graphics.pose().pushPose();
        graphics.pose().translate(width / 2 - 96 + 24 + slot, height - 30 - shift - font.lineHeight, 0.0F);
        graphics.pose().scale(0.5F, 0.5F, 1.0F);
        graphics.drawString(font, text, -font.width(text), 0, 0xFFFFFFFF, true);
        graphics.pose().popPose();
    }

    private static void blit(GuiGraphics graphics, int x, int y, int u, int v, int width, int height) {
        if (width <= 0 || height <= 0) {
            return;
        }
        GuiDraw.blit(graphics, AspectRenderer.PARTICLES, x, y, u, v, width, height, 256, 256);
    }
}
