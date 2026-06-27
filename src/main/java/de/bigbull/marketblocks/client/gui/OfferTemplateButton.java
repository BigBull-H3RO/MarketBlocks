package de.bigbull.marketblocks.client.gui;

import de.bigbull.marketblocks.MarketBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.ChatFormatting;

/**
 * A custom button used in the shop UI to display an offer template.
 * Renders up to two payment items on the left, an arrow in the middle, and the result item on the right.
 */
public class OfferTemplateButton extends Button {
    private static final ResourceLocation TRADE_ARROW =
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/trade_arrow.png");
    private static final ResourceLocation TRADE_ARROW_DISABLED =
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/trade_arrow_disabled.png");
    private static final ResourceLocation OUT_OF_STOCK_ICON = 
            ResourceLocation.fromNamespaceAndPath(MarketBlocks.MODID, "textures/gui/icon/out_of_stock.png");

    // --- Customizable positions for the discount numbers ---
    // Offsets for the old price (crossed out). 0, 0 means exact default item count position.
    private static final int DISCOUNT_OLD_X_OFFSET = 0;   
    private static final int DISCOUNT_OLD_Y_OFFSET = 0;  
    // Offsets for the new price, relative to the END of the old price.
    private static final int DISCOUNT_NEW_X_OFFSET = 1;  
    private static final int DISCOUNT_NEW_Y_OFFSET = 0;   

    // --- Customizable positions for the layout elements (relative to getX() and getY()) ---
    public static final int PAYMENT_1_X_OFFSET = 4;
    public static final int PAYMENT_1_X_OFFSET_DISCOUNTED = 2;
    public static final int PAYMENT_1_Y_OFFSET = 1;
    
    public static final int PAYMENT_2_X_OFFSET = 37;
    public static final int PAYMENT_2_X_OFFSET_DISCOUNTED = 29;
    public static final int PAYMENT_2_Y_OFFSET = 1;
    
    public static final int ARROW_X_OFFSET = 57;
    public static final int ARROW_Y_OFFSET = 5;
    
    public static final int RESULT_X_OFFSET = 70;
    public static final int RESULT_Y_OFFSET = 1;
    // ----------------------------------------------------------------------------------

    private ItemStack payment1 = ItemStack.EMPTY;
    private ItemStack payment2 = ItemStack.EMPTY;
    private ItemStack originalPayment1 = ItemStack.EMPTY;
    private ItemStack originalPayment2 = ItemStack.EMPTY;
    private ItemStack result = ItemStack.EMPTY;
    private boolean arrowActive;
    private boolean isLimitReached;

    public OfferTemplateButton(int x, int y, OnPress onPress) {
        super(x, y, 88, 20, Component.empty(), onPress, DEFAULT_NARRATION);
    }

    public void update(ItemStack payment1, ItemStack payment2, ItemStack result, boolean arrowActive) {
        update(payment1, payment2, ItemStack.EMPTY, ItemStack.EMPTY, result, arrowActive, false);
    }

    public void update(ItemStack payment1, ItemStack payment2, ItemStack originalPayment1, ItemStack originalPayment2, ItemStack result, boolean arrowActive, boolean isLimitReached) {
        this.payment1 = payment1;
        this.payment2 = payment2;
        this.originalPayment1 = originalPayment1;
        this.originalPayment2 = originalPayment2;
        this.result = result;
        this.arrowActive = arrowActive;
        this.isLimitReached = isLimitReached;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(graphics, mouseX, mouseY, partialTick);

        if (!payment1.isEmpty()) {
            boolean hasDiscount1 = !originalPayment1.isEmpty() && payment1.getCount() != originalPayment1.getCount();
            int xOffset = hasDiscount1 ? PAYMENT_1_X_OFFSET_DISCOUNTED : PAYMENT_1_X_OFFSET;
            renderPaymentItem(graphics, payment1, originalPayment1, getX() + xOffset, getY() + PAYMENT_1_Y_OFFSET);
        }

        if (!payment2.isEmpty()) {
            boolean hasDiscount2 = !originalPayment2.isEmpty() && payment2.getCount() != originalPayment2.getCount();
            int xOffset = hasDiscount2 ? PAYMENT_2_X_OFFSET_DISCOUNTED : PAYMENT_2_X_OFFSET;
            renderPaymentItem(graphics, payment2, originalPayment2, getX() + xOffset, getY() + PAYMENT_2_Y_OFFSET);
        }

        ResourceLocation arrowTexture = arrowActive ? TRADE_ARROW : TRADE_ARROW_DISABLED;
        int arrowX = getX() + ARROW_X_OFFSET;
        int arrowY = getY() + ARROW_Y_OFFSET;
        graphics.blit(arrowTexture, arrowX, arrowY, 0, 0, 10, 9, 10, 9);
        
        if (isLimitReached) {
            graphics.pose().pushPose();
            graphics.pose().translate(0, 0, 100);
            graphics.blit(OUT_OF_STOCK_ICON, arrowX - 3, arrowY - 3, 0, 0, 16, 16, 16, 16);
            graphics.pose().popPose();
        }

        if (!result.isEmpty()) {
            int resultX = getX() + RESULT_X_OFFSET;
            int resultY = getY() + RESULT_Y_OFFSET;
            graphics.renderItem(result, resultX, resultY);
            graphics.renderItemDecorations(Minecraft.getInstance().font, result, resultX, resultY);
        }
    }

    private void renderPaymentItem(GuiGraphics graphics, ItemStack effective, ItemStack original, int x, int y) {
        graphics.renderItem(effective, x, y);
        if (!original.isEmpty() && effective.getCount() != original.getCount()) {
            graphics.renderItemDecorations(Minecraft.getInstance().font, effective, x, y, "");

            String oldStr = String.valueOf(original.getCount());
            String newStr = String.valueOf(effective.getCount());

            Component oldText = Component.literal(oldStr).withStyle(ChatFormatting.STRIKETHROUGH, ChatFormatting.RED);
            Component newText = Component.literal(newStr);
            
            int fontWidthOld = Minecraft.getInstance().font.width(oldStr);
            
            // Standard Vanilla count position: x + 17 - fontWidth, y + 9
            int oldX = x + 17 - fontWidthOld + DISCOUNT_OLD_X_OFFSET;
            int oldY = y + 9 + DISCOUNT_OLD_Y_OFFSET;
            
            int newX = oldX + fontWidthOld + DISCOUNT_NEW_X_OFFSET;
            int newY = y + 9 + DISCOUNT_NEW_Y_OFFSET;
            
            graphics.pose().pushPose();
            graphics.pose().translate(0, 0, 200);
            graphics.drawString(Minecraft.getInstance().font, oldText, oldX, oldY, 0xFFFFFF, true);
            graphics.drawString(Minecraft.getInstance().font, newText, newX, newY, 0xFFFFFF, true);
            graphics.pose().popPose();
        } else {
            graphics.renderItemDecorations(Minecraft.getInstance().font, effective, x, y);
        }
    }
}
