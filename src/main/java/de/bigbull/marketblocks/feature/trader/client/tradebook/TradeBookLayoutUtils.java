package de.bigbull.marketblocks.feature.trader.client.tradebook;

import com.mojang.authlib.GameProfile;
import de.bigbull.marketblocks.client.gui.OfferTemplateButton;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import de.bigbull.marketblocks.MarketBlocks;
import de.bigbull.marketblocks.feature.trader.network.TradeBookOpenPacket.ShopOfferData;

public class TradeBookLayoutUtils {
    public static final ResourceLocation OFFER_GUI = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/tradebook/offer_gui.png");
    private static final ResourceLocation TRADE_ARROW = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/trade_arrow.png");
    private static final ResourceLocation TRADE_ARROW_DISABLED = ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/trade_arrow_disabled.png");

    public static final int TEXT_WIDTH = 114;
    public static final float TEXT_SCALE = 0.75f;

    public static void renderPlayerHead(GuiGraphics graphics, String username, int x, int y, float scale, int size) {
        GameProfile profile = new GameProfile(Util.NIL_UUID, username);
        PlayerSkin skin = Minecraft.getInstance().getSkinManager().getInsecureSkin(profile);

        graphics.pose().pushPose();
        float inverseScale = 1.0f / scale;
        graphics.pose().translate(x, y - 1, 0);
        graphics.pose().scale(inverseScale, inverseScale, 1.0f);

        PlayerFaceRenderer.draw(graphics, skin.texture(), 0, 0, size);

        graphics.pose().popPose();
    }

    public static void renderScaledIcon(GuiGraphics graphics, ItemStack stack, int x, int y, float scale) {
        graphics.pose().pushPose();
        graphics.pose().translate(x, y - 1, 0);
        graphics.pose().scale(scale, scale, 1.0f);
        graphics.renderItem(stack, 0, 0);
        graphics.pose().popPose();
    }

    public static void drawCenteredString(GuiGraphics graphics, TradeBookRenderContext context, String text, int startX, int currentY, int areaWidth, int color, boolean dropShadow) {
        int width = context.getFont().width(text);
        int x = startX + (areaWidth - width) / 2;
        graphics.drawString(context.getFont(), text, x, currentY, color, dropShadow);
    }

    public static void drawRightAlignedString(GuiGraphics graphics, TradeBookRenderContext context, String text, int startX, int currentY, int areaWidth, int color, boolean dropShadow) {
        int width = context.getFont().width(text);
        int x = startX + areaWidth - width;
        graphics.drawString(context.getFont(), text, x, currentY, color, dropShadow);
    }

    public static String truncate(String text, int maxLength) {
        if (text.length() > maxLength) {
            return text.substring(0, maxLength - 2) + "..";
        }
        return text;
    }

    public static void renderInlineOffer(GuiGraphics graphics, ShopOfferData offer, int x, int y, String status, int mouseX, int mouseY, float scale, TradeBookRenderContext context) {
        int itemY = y - 5;
        graphics.blit(OFFER_GUI, x - 2, itemY - 5, 1, 3, 94, 26, 96, 32);

        int p1x = x + OfferTemplateButton.PAYMENT_1_X_OFFSET;
        int p2x = x + OfferTemplateButton.PAYMENT_2_X_OFFSET;
        int arrX = x + OfferTemplateButton.ARROW_X_OFFSET;
        int resX = x + OfferTemplateButton.RESULT_X_OFFSET;

        if (!offer.payment1().isEmpty()) {
            graphics.renderItem(offer.payment1(), p1x, itemY);
            graphics.renderItemDecorations(context.getFont(), offer.payment1(), p1x, itemY);
        }
        if (!offer.payment2().isEmpty()) {
            graphics.renderItem(offer.payment2(), p2x, itemY);
            graphics.renderItemDecorations(context.getFont(), offer.payment2(), p2x, itemY);
        }

        ResourceLocation arrowTexture = status.equals("OK") ? TRADE_ARROW : TRADE_ARROW_DISABLED;
        graphics.blit(arrowTexture, arrX, itemY + 4, 0, 0, 10, 9, 10, 9);

        if (!offer.result().isEmpty()) {
            graphics.renderItem(offer.result(), resX, itemY);
            graphics.renderItemDecorations(context.getFont(), offer.result(), resX, itemY);
        }

        double scaledMouseX = mouseX / scale;
        double scaledMouseY = mouseY / scale;

        // Tooltips (delayed)
        if (scaledMouseX >= p1x && scaledMouseX < p1x + 16 && scaledMouseY >= itemY && scaledMouseY < itemY + 16
                && !offer.payment1().isEmpty()) {
            context.setNextHoveredObject("item_" + offer.payment1().getItem().toString());
            context.addTooltip(() -> graphics.renderTooltip(context.getFont(), offer.payment1(), mouseX, mouseY));
        } else if (scaledMouseX >= p2x && scaledMouseX < p2x + 16 && scaledMouseY >= itemY && scaledMouseY < itemY + 16
                && !offer.payment2().isEmpty()) {
            context.setNextHoveredObject("item_" + offer.payment2().getItem().toString());
            context.addTooltip(() -> graphics.renderTooltip(context.getFont(), offer.payment2(), mouseX, mouseY));
        } else if (scaledMouseX >= resX && scaledMouseX < resX + 16 && scaledMouseY >= itemY
                && scaledMouseY < itemY + 16 && !offer.result().isEmpty()) {
            context.setNextHoveredObject("item_" + offer.result().getItem().toString());
            context.addTooltip(() -> graphics.renderTooltip(context.getFont(), offer.result(), mouseX, mouseY));
        } else if (!status.equals("OK") && scaledMouseX >= arrX && scaledMouseX < arrX + 10 && scaledMouseY >= itemY + 4
                && scaledMouseY < itemY + 13) {
            context.setNextHoveredObject("status_" + status);
            Component tooltip = status.equals("OUT_OF_STOCK")
                    ? Component.translatable("gui.marketblocks.trade_book.status.out_of_stock")
                            .withStyle(ChatFormatting.RED)
                    : Component.translatable("gui.marketblocks.trade_book.status.output_full")
                            .withStyle(ChatFormatting.RED);
            context.addTooltip(() -> graphics.renderTooltip(context.getFont(), tooltip, mouseX, mouseY));
        }
    }
}
