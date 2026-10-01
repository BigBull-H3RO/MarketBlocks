package de.bigbull.marketblocks.feature.trader.client.tradebook;

import java.util.UUID;
import com.mojang.authlib.GameProfile;
import de.bigbull.marketblocks.client.gui.OfferTemplateButton;
import net.minecraft.ChatFormatting;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerFaceExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import de.bigbull.marketblocks.Constants;
import de.bigbull.marketblocks.feature.trader.network.TradeBookOpenPacket.ShopOfferData;

public class TradeBookLayoutUtils {
    public static final Identifier OFFER_GUI = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/tradebook/offer_gui.png");
    private static final Identifier TRADE_ARROW = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/icon/trade_arrow.png");
    private static final Identifier TRADE_ARROW_DISABLED = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/icon/trade_arrow_disabled.png");

    public static final int TEXT_WIDTH = 114;
    public static final float TEXT_SCALE = 0.75f;

    public static void renderPlayerHead(GuiGraphicsExtractor graphics, String username, int x, int y, float scale, int size) {
        renderPlayerHead(graphics, null, username, x, y, scale, size, false);
    }

    public static void renderPlayerHead(GuiGraphicsExtractor graphics, String username, int x, int y, float scale, int size, boolean withBorder) {
        renderPlayerHead(graphics, null, username, x, y, scale, size, withBorder);
    }

    public static void renderPlayerHead(GuiGraphicsExtractor graphics, UUID id, String username, int x, int y, float scale, int size, boolean withBorder) {
        GameProfile profile = new GameProfile(id != null ? id : Util.NIL_UUID, username != null ? username : "");
        PlayerSkin skin = Minecraft.getInstance().getSkinManager().createLookup(profile, false).get();

        graphics.pose().pushMatrix();
        float inverseScale = 1.0f / scale;
        graphics.pose().translate(x, y - 1);
        graphics.pose().scale(inverseScale, inverseScale);

        if (withBorder) {
            graphics.fill(-1, -1, size + 1, size + 1, 0xFF2A2A2A);
            graphics.fill(0, 0, size, size, 0xFF181818);
        }

        PlayerFaceExtractor.extractRenderState(graphics, skin, 0, 0, size);

        graphics.pose().popMatrix();
    }

    public static void renderScaledIcon(GuiGraphicsExtractor graphics, ItemStack stack, int x, int y, float scale) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y - 1);
        graphics.pose().scale(scale, scale);
        graphics.item(stack, 0, 0);
        graphics.pose().popMatrix();
    }

    public static void drawCenteredString(GuiGraphicsExtractor graphics, TradeBookRenderContext context, String text, int startX, int currentY, int areaWidth, int color, boolean dropShadow) {
        int width = context.getFont().width(text);
        int x = startX + (areaWidth - width) / 2;
        int effectiveColor = (color & 0xFF000000) == 0 ? (0xFF000000 | color) : color;
        graphics.text(context.getFont(), text, x, currentY, effectiveColor, dropShadow);
    }

    public static void drawRightAlignedString(GuiGraphicsExtractor graphics, TradeBookRenderContext context, String text, int startX, int currentY, int areaWidth, int color, boolean dropShadow) {
        int width = context.getFont().width(text);
        int x = startX + areaWidth - width;
        int effectiveColor = (color & 0xFF000000) == 0 ? (0xFF000000 | color) : color;
        graphics.text(context.getFont(), text, x, currentY, effectiveColor, dropShadow);
    }

    public static String truncate(String text, int maxLength) {
        if (text.length() > maxLength) {
            return text.substring(0, maxLength - 2) + "..";
        }
        return text;
    }

    public static void renderInlineOffer(GuiGraphicsExtractor graphics, ShopOfferData offer, int x, int y, String status, int mouseX, int mouseY, float scale, TradeBookRenderContext context) {
        int itemY = y - 5;
        graphics.blit(RenderPipelines.GUI_TEXTURED, OFFER_GUI, x - 3, itemY - 6, 0.0f, 2.0f, 96, 28, 96, 32);

        int p1x = x + OfferTemplateButton.PAYMENT_1_X_OFFSET;
        int p2x = x + OfferTemplateButton.PAYMENT_2_X_OFFSET;
        int arrX = x + OfferTemplateButton.ARROW_X_OFFSET;
        int resX = x + OfferTemplateButton.RESULT_X_OFFSET;

        if (!offer.payment1().isEmpty()) {
            graphics.item(offer.payment1(), p1x, itemY);
            graphics.itemDecorations(context.getFont(), offer.payment1(), p1x, itemY);
        }
        if (!offer.payment2().isEmpty()) {
            graphics.item(offer.payment2(), p2x, itemY);
            graphics.itemDecorations(context.getFont(), offer.payment2(), p2x, itemY);
        }

        Identifier arrowTexture = status.equals("OK") ? TRADE_ARROW : TRADE_ARROW_DISABLED;
        graphics.blit(RenderPipelines.GUI_TEXTURED, arrowTexture, arrX, itemY + 4, 0.0f, 0.0f, 10, 9, 10, 9);

        if (!offer.result().isEmpty()) {
            graphics.item(offer.result(), resX, itemY);
            graphics.itemDecorations(context.getFont(), offer.result(), resX, itemY);
        }

        double scaledMouseX = mouseX / scale;
        double scaledMouseY = mouseY / scale;

        // Tooltips (delayed)
        if (scaledMouseX >= p1x && scaledMouseX < p1x + 16 && scaledMouseY >= itemY && scaledMouseY < itemY + 16
                && !offer.payment1().isEmpty()) {
            context.setNextHoveredObject("item_" + offer.payment1().getItem().toString());
            context.addTooltip(() -> graphics.setTooltipForNextFrame(context.getFont(), offer.payment1(), mouseX, mouseY));
        } else if (scaledMouseX >= p2x && scaledMouseX < p2x + 16 && scaledMouseY >= itemY && scaledMouseY < itemY + 16
                && !offer.payment2().isEmpty()) {
            context.setNextHoveredObject("item_" + offer.payment2().getItem().toString());
            context.addTooltip(() -> graphics.setTooltipForNextFrame(context.getFont(), offer.payment2(), mouseX, mouseY));
        } else if (scaledMouseX >= resX && scaledMouseX < resX + 16 && scaledMouseY >= itemY
                && scaledMouseY < itemY + 16 && !offer.result().isEmpty()) {
            context.setNextHoveredObject("item_" + offer.result().getItem().toString());
            context.addTooltip(() -> graphics.setTooltipForNextFrame(context.getFont(), offer.result(), mouseX, mouseY));
        } else if (!status.equals("OK") && scaledMouseX >= arrX && scaledMouseX < arrX + 10 && scaledMouseY >= itemY + 4
                && scaledMouseY < itemY + 13) {
            context.setNextHoveredObject("status_" + status);
            Component tooltip = status.equals("OUT_OF_STOCK")
                    ? Component.translatable("gui.marketblocks.trade_book.status.out_of_stock")
                            .withStyle(ChatFormatting.RED)
                    : Component.translatable("gui.marketblocks.trade_book.status.output_full")
                            .withStyle(ChatFormatting.RED);
            context.addTooltip(() -> graphics.setTooltipForNextFrame(context.getFont(), tooltip, mouseX, mouseY));
        }
    }
}
