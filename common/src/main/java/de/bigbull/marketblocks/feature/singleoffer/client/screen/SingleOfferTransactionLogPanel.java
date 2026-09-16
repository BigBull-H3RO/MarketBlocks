package de.bigbull.marketblocks.feature.singleoffer.client.screen;

import com.mojang.authlib.GameProfile;
import de.bigbull.marketblocks.Constants;
import de.bigbull.marketblocks.client.gui.OfferTemplateButton;
import de.bigbull.marketblocks.feature.log.TransactionLogEntry;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Encapsulates transaction log rendering, tile cards, vanilla scrollbar and interaction.
 * Styled with clean dark inset styling and authentic vanilla villager scroller.
 */
public class SingleOfferTransactionLogPanel {

    private static final ResourceLocation TRADE_ARROW_ICON = ResourceLocation
            .fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/icon/trade_arrow.png");
    private static final ResourceLocation MOVE_RIGHT_MINI_ICON = ResourceLocation
            .fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/icon/move_right_mini.png");
    private static final ResourceLocation MOVE_DOWN_MINI_ICON = ResourceLocation
            .fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/icon/move_down_mini.png");

    private static final ResourceLocation SCROLLER_SPRITE = ResourceLocation
            .withDefaultNamespace("container/villager/scroller");
    private static final ResourceLocation SCROLLER_DISABLED_SPRITE = ResourceLocation
            .withDefaultNamespace("container/villager/scroller_disabled");

    // Container dimensions (fills the tab area cleanly without extra GroupBox)
    public static final int CONTAINER_X_OFFSET = 7;
    public static final int CONTAINER_Y_OFFSET = 19;
    public static final int CONTAINER_WIDTH = 162;
    public static final int CONTAINER_HEIGHT = 120;

    // Vanilla Villager Scroller (6x27)
    public static final int SCROLLER_TRACK_X_OFFSET = 162;
    public static final int SCROLLER_WIDTH = 6;
    public static final int SCROLLER_HEIGHT = 27;

    // Rows
    public static final int ROW_X_OFFSET = 8;
    public static final int ROW_WIDTH = 153;
    public static final int ROW_HEIGHT_COLLAPSED = 20;
    public static final int ROW_HEIGHT_EXPANDED = 44;

    // State
    private int scrollPixelOffset = 0;
    private int expandedLogIndex = -1;
    private boolean isDragging = false;

    public void reset() {
        scrollPixelOffset = 0;
        expandedLogIndex = -1;
        isDragging = false;
    }

    public void renderBackground(GuiGraphics graphics, Font font, int leftPos, int topPos, int mouseX, int mouseY,
            List<TransactionLogEntry> entries) {
        int containerX = leftPos + CONTAINER_X_OFFSET;
        int containerY = topPos + CONTAINER_Y_OFFSET;

        // 1. Dark container background & 1px border
        graphics.fill(containerX, containerY, containerX + CONTAINER_WIDTH, containerY + CONTAINER_HEIGHT, 0xFF1B1B1B);
        graphics.fill(containerX, containerY, containerX + CONTAINER_WIDTH, containerY + 1, 0xFF373737);
        graphics.fill(containerX, containerY + CONTAINER_HEIGHT - 1, containerX + CONTAINER_WIDTH,
                containerY + CONTAINER_HEIGHT, 0xFF373737);
        graphics.fill(containerX, containerY, containerX + 1, containerY + CONTAINER_HEIGHT, 0xFF373737);
        graphics.fill(containerX + CONTAINER_WIDTH - 1, containerY, containerX + CONTAINER_WIDTH,
                containerY + CONTAINER_HEIGHT, 0xFF373737);

        // 2. Scrollbar track & divider line
        int trackX = leftPos + SCROLLER_TRACK_X_OFFSET;
        int trackY = containerY + 1;
        int trackH = CONTAINER_HEIGHT - 2;
        graphics.fill(trackX - 1, trackY, trackX, trackY + trackH, 0xFF373737);
        graphics.fill(trackX, trackY, trackX + SCROLLER_WIDTH, trackY + trackH, 0xFF141414);

        int maxScroll = getMaxScroll(entries);
        scrollPixelOffset = Mth.clamp(scrollPixelOffset, 0, maxScroll);

        // 3. Vanilla Scroller Knob
        if (maxScroll > 0) {
            int travel = Math.max(1, trackH - SCROLLER_HEIGHT);
            float progress = (float) scrollPixelOffset / (float) maxScroll;
            int knobY = trackY + (int) (progress * travel);
            graphics.blitSprite(SCROLLER_SPRITE, trackX, knobY, SCROLLER_WIDTH, SCROLLER_HEIGHT);
        } else {
            graphics.blitSprite(SCROLLER_DISABLED_SPRITE, trackX, trackY, SCROLLER_WIDTH, SCROLLER_HEIGHT);
        }

        // 4. Empty State
        if (entries.isEmpty()) {
            Component empty = Component.translatable("gui.marketblocks.log.empty");
            int tw = font.width(empty);
            int contentW = (trackX - 1) - (containerX + 1);
            int textX = containerX + 1 + (contentW - tw) / 2;
            int textY = containerY + (CONTAINER_HEIGHT - font.lineHeight) / 2;
            graphics.drawString(font, empty, textX, textY, 0x808080, false);
            return;
        }

        // 5. Rows with Scissor Clipping
        int rowAreaX = leftPos + ROW_X_OFFSET;
        int rowAreaY = containerY + 1;
        int rowAreaW = ROW_WIDTH;
        int rowAreaH = CONTAINER_HEIGHT - 2;

        graphics.enableScissor(rowAreaX, rowAreaY, rowAreaX + rowAreaW, rowAreaY + rowAreaH);

        int currentY = rowAreaY - scrollPixelOffset;
        for (int i = 0; i < entries.size(); i++) {
            boolean isExpanded = (i == expandedLogIndex);
            int rowHeight = isExpanded ? ROW_HEIGHT_EXPANDED : ROW_HEIGHT_COLLAPSED;

            if (currentY + rowHeight > rowAreaY && currentY < rowAreaY + rowAreaH) {
                renderLogRow(graphics, font, entries.get(i), rowAreaX, currentY, i, isExpanded, mouseX, mouseY,
                        rowAreaY, rowAreaH);
            }
            currentY += rowHeight;
        }

        graphics.disableScissor();
    }

    private void renderLogRow(GuiGraphics graphics, Font font, TransactionLogEntry entry, int x, int y, int index,
            boolean isExpanded, int mouseX, int mouseY, int clipTop, int clipHeight) {
        int rowHeight = isExpanded ? ROW_HEIGHT_EXPANDED : ROW_HEIGHT_COLLAPSED;
        boolean rowHovered = mouseX >= x && mouseX < x + ROW_WIDTH && mouseY >= y && mouseY < y + rowHeight
                && mouseY >= clipTop && mouseY < clipTop + clipHeight;

        int rowBg = rowHovered ? 0xFF2A2A2A : ((index % 2 == 1) ? 0xFF242424 : 0xFF1E1E1E);
        graphics.fill(x, y, x + ROW_WIDTH, y + rowHeight, rowBg);
        graphics.fill(x, y + rowHeight - 1, x + ROW_WIDTH, y + rowHeight, 0xFF2D2D2D);

        // Header line (y to y + 20)
        int textY = y + 6;

        // Player Head (8x8)
        int headX = x + 4;
        int headY = y + 6;
        renderPlayerHead(graphics, entry.buyerUuid(), entry.buyerName(), headX, headY);

        // Chevron icon
        ResourceLocation expandIcon = isExpanded ? MOVE_DOWN_MINI_ICON : MOVE_RIGHT_MINI_ICON;
        int expandX = x + ROW_WIDTH - 14;
        int expandY = y + 4;
        graphics.blit(expandIcon, expandX, expandY, 12, 12, 0.0F, 0.0F, 18, 18, 18, 18);

        // Relative Time
        Component timeText = formatRelativeTime(entry.epochSecond());
        int timeX = expandX - 4 - font.width(timeText);
        graphics.drawString(font, timeText, timeX, textY, 0x888888, false);

        // Buyer Name
        int nameX = headX + 8 + 4;
        String buyerName = entry.buyerName().isBlank() ? "Unknown" : entry.buyerName();
        int maxNameWidth = timeX - nameX - 4;
        if (maxNameWidth > 0 && font.width(buyerName) > maxNameWidth) {
            buyerName = font.plainSubstrByWidth(buyerName, maxNameWidth - font.width("...")) + "...";
        }
        graphics.drawString(font, buyerName, nameX, textY, rowHovered ? 0xFFFFFF : 0xE0E0E0, false);

        // Expanded preview box
        if (isExpanded) {
            int offerFrameW = 88;
            int offerFrameH = 20;
            int offerFrameX = x + ROW_WIDTH - 4 - offerFrameW;
            int offerFrameY = y + 20;

            // Inset panel for ONLY the trade offer preview
            graphics.fill(offerFrameX, offerFrameY, offerFrameX + offerFrameW, offerFrameY + offerFrameH, 0xFF141414);
            graphics.fill(offerFrameX, offerFrameY, offerFrameX + offerFrameW, offerFrameY + 1, 0xFF282828);
            graphics.fill(offerFrameX, offerFrameY + offerFrameH - 1, offerFrameX + offerFrameW, offerFrameY + offerFrameH, 0xFF282828);
            graphics.fill(offerFrameX, offerFrameY + 1, offerFrameX + 1, offerFrameY + offerFrameH - 1, 0xFF282828);
            graphics.fill(offerFrameX + offerFrameW - 1, offerFrameY + 1, offerFrameX + offerFrameW, offerFrameY + offerFrameH - 1, 0xFF282828);

            int itemY = offerFrameY + 2;
            int arrowY = offerFrameY + 6;

            ItemStack paid1 = entry.paidStacks().size() > 0 ? entry.paidStacks().get(0) : ItemStack.EMPTY;
            ItemStack paid2 = entry.paidStacks().size() > 1 ? entry.paidStacks().get(1) : ItemStack.EMPTY;
            ItemStack bought = entry.boughtStacks().isEmpty() ? ItemStack.EMPTY : entry.boughtStacks().get(0);

            // Right-aligned offer layout matching OfferTemplateButton inside the offer frame
            int slot1X = offerFrameX + OfferTemplateButton.PAYMENT_1_X_OFFSET;
            int slot2X = offerFrameX + OfferTemplateButton.PAYMENT_2_X_OFFSET;
            int arrowX = offerFrameX + OfferTemplateButton.ARROW_X_OFFSET;
            int boughtX = offerFrameX + OfferTemplateButton.RESULT_X_OFFSET;

            // Aggregation count badge (placed to the left OUTSIDE the offer frame)
            if (entry.aggregationCount() > 1) {
                String repeatLabel = "x" + entry.aggregationCount();
                int repW = font.width(repeatLabel);
                int repX = offerFrameX - 8 - repW;
                int repY = offerFrameY + (offerFrameH - font.lineHeight) / 2 + 1;
                graphics.drawString(font, repeatLabel, repX, repY, 0xFFAA00, false);
            }

            // Paid item 1 (no extra background box)
            if (!paid1.isEmpty()) {
                graphics.renderItem(paid1, slot1X, itemY);
                graphics.renderItemDecorations(font, paid1, slot1X, itemY);
            }

            // Paid item 2 (no extra background box)
            if (!paid2.isEmpty()) {
                graphics.renderItem(paid2, slot2X, itemY);
                graphics.renderItemDecorations(font, paid2, slot2X, itemY);
            }

            if (paid1.isEmpty() && paid2.isEmpty()) {
                Component none = Component.translatable("gui.marketblocks.log.none");
                graphics.drawString(font, none, slot1X, itemY + 4, 0x666666, false);
            }

            // Trade arrow
            graphics.blit(TRADE_ARROW_ICON, arrowX, arrowY, 0, 0, 10, 9, 10, 9);

            // Bought item (no extra background box)
            if (!bought.isEmpty()) {
                graphics.renderItem(bought, boughtX, itemY);
                graphics.renderItemDecorations(font, bought, boughtX, itemY);
            }
        }
    }

    private void renderPlayerHead(GuiGraphics graphics, UUID id, String name, int x, int y) {
        if (id == null || (id.getLeastSignificantBits() == 0L && id.getMostSignificantBits() == 0L)) {
            id = Util.NIL_UUID;
        }
        Minecraft client = Minecraft.getInstance();
        GameProfile profile = new GameProfile(id, name != null ? name : "");
        ResourceLocation skinTexture = client.getSkinManager().getInsecureSkin(profile).texture();

        // Base head layer (8x8 at u=8, v=8, src 8x8, tex 64x64)
        graphics.blit(skinTexture, x, y, 8, 8, 8.0F, 8.0F, 8, 8, 64, 64);
        // Outer hat layer (8x8 at u=40, v=8, src 8x8, tex 64x64)
        graphics.blit(skinTexture, x, y, 8, 8, 40.0F, 8.0F, 8, 8, 64, 64);
    }

    public void renderHoverTooltip(GuiGraphics graphics, Font font, int leftPos, int topPos, int mouseX, int mouseY,
            List<TransactionLogEntry> entries) {
        if (expandedLogIndex < 0 || expandedLogIndex >= entries.size()) {
            return;
        }

        int rowAreaY = topPos + CONTAINER_Y_OFFSET + 1;
        int rowAreaH = CONTAINER_HEIGHT - 2;

        int currentY = rowAreaY - scrollPixelOffset;
        for (int i = 0; i < expandedLogIndex; i++) {
            currentY += ROW_HEIGHT_COLLAPSED;
        }

        int offerFrameW = 88;
        int offerFrameX = leftPos + ROW_X_OFFSET + ROW_WIDTH - 4 - offerFrameW;
        int offerFrameY = currentY + 20;
        int itemY = offerFrameY + 2;

        if (mouseY < rowAreaY || mouseY >= rowAreaY + rowAreaH || mouseY < itemY || mouseY >= itemY + 16) {
            return;
        }

        int slot1X = offerFrameX + OfferTemplateButton.PAYMENT_1_X_OFFSET;
        int slot2X = offerFrameX + OfferTemplateButton.PAYMENT_2_X_OFFSET;
        int boughtX = offerFrameX + OfferTemplateButton.RESULT_X_OFFSET;

        TransactionLogEntry entry = entries.get(expandedLogIndex);

        if (entry.paidStacks().size() > 0) {
            ItemStack paid1 = entry.paidStacks().get(0);
            if (!paid1.isEmpty() && mouseX >= slot1X && mouseX < slot1X + 16) {
                graphics.renderTooltip(font, paid1, mouseX, mouseY);
                return;
            }
        }

        if (entry.paidStacks().size() > 1) {
            ItemStack paid2 = entry.paidStacks().get(1);
            if (!paid2.isEmpty() && mouseX >= slot2X && mouseX < slot2X + 16) {
                graphics.renderTooltip(font, paid2, mouseX, mouseY);
                return;
            }
        }

        if (!entry.boughtStacks().isEmpty()) {
            ItemStack bought = entry.boughtStacks().get(0);
            if (!bought.isEmpty() && mouseX >= boughtX && mouseX < boughtX + 16) {
                graphics.renderTooltip(font, bought, mouseX, mouseY);
                return;
            }
        }

        if (entry.aggregationCount() > 1) {
            String repeatLabel = "x" + entry.aggregationCount();
            int repW = font.width(repeatLabel);
            int repX = offerFrameX - 8 - repW;
            if (mouseX >= repX && mouseX < repX + repW) {
                graphics.renderTooltip(font, Component.translatable("gui.marketblocks.log.repeat_tooltip", entry.aggregationCount()), mouseX, mouseY);
            }
        }
    }

    public boolean onMouseClicked(double mouseX, double mouseY, int leftPos, int topPos,
            List<TransactionLogEntry> entries) {
        int trackX = leftPos + SCROLLER_TRACK_X_OFFSET;
        int trackY = topPos + CONTAINER_Y_OFFSET + 1;
        int trackH = CONTAINER_HEIGHT - 2;

        int maxScroll = getMaxScroll(entries);

        // Click on scrollbar track / knob
        if (maxScroll > 0 && mouseX >= trackX && mouseX <= trackX + SCROLLER_WIDTH
                && mouseY >= trackY && mouseY <= trackY + trackH) {
            isDragging = true;
            int travel = Math.max(1, trackH - SCROLLER_HEIGHT);
            float rel = (float) (mouseY - trackY - SCROLLER_HEIGHT / 2.0F) / (float) travel;
            scrollPixelOffset = Math.round(Mth.clamp(rel, 0.0F, 1.0F) * maxScroll);
            return true;
        }

        // Click on row
        int rowAreaX = leftPos + ROW_X_OFFSET;
        int rowAreaY = topPos + CONTAINER_Y_OFFSET + 1;
        int rowAreaW = ROW_WIDTH;
        int rowAreaH = CONTAINER_HEIGHT - 2;

        if (mouseX >= rowAreaX && mouseX < rowAreaX + rowAreaW
                && mouseY >= rowAreaY && mouseY < rowAreaY + rowAreaH) {
            int currentY = rowAreaY - scrollPixelOffset;
            for (int i = 0; i < entries.size(); i++) {
                int rowHeight = (i == expandedLogIndex) ? ROW_HEIGHT_EXPANDED : ROW_HEIGHT_COLLAPSED;
                if (mouseY >= currentY && mouseY < currentY + rowHeight) {
                    expandedLogIndex = (expandedLogIndex == i) ? -1 : i;
                    Minecraft.getInstance().getSoundManager()
                            .play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    return true;
                }
                currentY += rowHeight;
            }
        }

        return false;
    }

    public boolean onMouseDragged(double mouseY, int topPos, List<TransactionLogEntry> entries) {
        if (!isDragging) {
            return false;
        }
        int maxScroll = getMaxScroll(entries);
        if (maxScroll <= 0) {
            return false;
        }

        int trackY = topPos + CONTAINER_Y_OFFSET + 1;
        int trackH = CONTAINER_HEIGHT - 2;
        int travel = Math.max(1, trackH - SCROLLER_HEIGHT);

        float rel = (float) (mouseY - trackY - SCROLLER_HEIGHT / 2.0F) / (float) travel;
        scrollPixelOffset = Math.round(Mth.clamp(rel, 0.0F, 1.0F) * maxScroll);
        return true;
    }

    public void onMouseReleased() {
        isDragging = false;
    }

    public boolean onMouseScrolled(double mouseX, double mouseY, double scrollY, int leftPos, int topPos,
            List<TransactionLogEntry> entries) {
        int maxScroll = getMaxScroll(entries);
        if (maxScroll <= 0) {
            return false;
        }

        int containerX = leftPos + CONTAINER_X_OFFSET;
        int containerY = topPos + CONTAINER_Y_OFFSET;
        if (mouseX >= containerX && mouseX <= containerX + CONTAINER_WIDTH
                && mouseY >= containerY && mouseY <= containerY + CONTAINER_HEIGHT) {
            scrollPixelOffset -= (int) (scrollY * ROW_HEIGHT_COLLAPSED);
            scrollPixelOffset = Mth.clamp(scrollPixelOffset, 0, maxScroll);
            return true;
        }
        return false;
    }

    private int getTotalHeight(List<TransactionLogEntry> entries) {
        if (entries.isEmpty()) {
            return 0;
        }
        int total = entries.size() * ROW_HEIGHT_COLLAPSED;
        if (expandedLogIndex >= 0 && expandedLogIndex < entries.size()) {
            total += (ROW_HEIGHT_EXPANDED - ROW_HEIGHT_COLLAPSED);
        }
        return total;
    }

    private int getMaxScroll(List<TransactionLogEntry> entries) {
        int contentAreaHeight = CONTAINER_HEIGHT - 2;
        return Math.max(0, getTotalHeight(entries) - contentAreaHeight);
    }

    private Component formatRelativeTime(long epochSecond) {
        long deltaSeconds = Math.max(0L, Instant.now().getEpochSecond() - Math.max(0L, epochSecond));
        if (deltaSeconds < 5L)
            return Component.translatable("gui.marketblocks.log.time.just_now");
        if (deltaSeconds < 60L)
            return Component.translatable("gui.marketblocks.log.time.seconds", deltaSeconds);
        if (deltaSeconds < 3600L)
            return Component.translatable("gui.marketblocks.log.time.minutes", deltaSeconds / 60L);
        if (deltaSeconds < 86400L)
            return Component.translatable("gui.marketblocks.log.time.hours", deltaSeconds / 3600L);
        return Component.translatable("gui.marketblocks.log.time.days", deltaSeconds / 86400L);
    }
}
