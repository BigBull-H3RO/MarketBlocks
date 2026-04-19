package de.bigbull.marketblocks.util.custom.screen.marketplace;

import de.bigbull.marketblocks.shop.marketplace.MarketplacePage;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles Marketplace page sidebar buttons, truncation and delayed hover tooltips.
 */
public final class MarketplacePageSidebar {

    private static final int SIDEBAR_X_OFFSET = -102;
    private static final int SIDEBAR_Y_OFFSET = 10;
    private static final int BUTTON_SPACING_Y = 22;
    private static final int BUTTON_WIDTH = 100;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_TEXT_PADDING = 10;
    private static final long TOOLTIP_DELAY_MS = 450L;

    private final List<TooltipTarget> tooltipTargets = new ArrayList<>();
    private TooltipTarget hoveredTooltipTarget;
    private long hoveredSinceMs;

    public void reset() {
        tooltipTargets.clear();
        hoveredTooltipTarget = null;
        hoveredSinceMs = 0L;
    }

    public void buildButtons(Context context, Callbacks callbacks) {
        tooltipTargets.clear();
        hoveredTooltipTarget = null;

        int baseX = context.leftPos() + SIDEBAR_X_OFFSET;
        int y = context.topPos() + SIDEBAR_Y_OFFSET;

        for (int i = 0; i < context.pages().size(); i++) {
            int pageIndex = i;
            MarketplacePage page = context.pages().get(i);
            Component fullLabel = pageDisplayName(page, i);
            EllipsizedLabel ellipsizedLabel = ellipsizeForButton(fullLabel, context.font());

            Button button = Button.builder(ellipsizedLabel.text(), ignored -> callbacks.onPageSelected(pageIndex))
                    .bounds(baseX, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                    .build();
            button.active = context.selectedPage() != pageIndex;
            callbacks.addWidget(button);

            if (ellipsizedLabel.truncated()) {
                tooltipTargets.add(new TooltipTarget(baseX, y, BUTTON_WIDTH, BUTTON_HEIGHT, fullLabel));
            }
            y += BUTTON_SPACING_Y;
        }
    }

    public void renderDelayedTooltip(Context context, GuiGraphics guiGraphics, int mouseX, int mouseY) {
        TooltipTarget currentlyHovered = null;
        for (TooltipTarget target : tooltipTargets) {
            if (target.contains(mouseX, mouseY)) {
                currentlyHovered = target;
                break;
            }
        }

        if (currentlyHovered == null) {
            hoveredTooltipTarget = null;
            return;
        }

        if (hoveredTooltipTarget != currentlyHovered) {
            hoveredTooltipTarget = currentlyHovered;
            hoveredSinceMs = Util.getMillis();
            return;
        }

        if (Util.getMillis() - hoveredSinceMs >= TOOLTIP_DELAY_MS) {
            guiGraphics.renderTooltip(context.font(), currentlyHovered.fullLabel(), mouseX, mouseY);
        }
    }

    private Component pageDisplayName(MarketplacePage page, int index) {
        if (page.name().isBlank()) {
            return Component.translatable("gui.marketblocks.marketplace.unnamed_page", index + 1);
        }
        return Component.literal(page.name());
    }

    private EllipsizedLabel ellipsizeForButton(Component text, Font font) {
        int maxTextWidth = Math.max(0, BUTTON_WIDTH - BUTTON_TEXT_PADDING);
        String fullText = text.getString();
        if (fullText.isEmpty() || font.width(fullText) <= maxTextWidth) {
            return new EllipsizedLabel(text, false);
        }

        String ellipsis = "...";
        int ellipsisWidth = font.width(ellipsis);
        if (maxTextWidth <= ellipsisWidth) {
            return new EllipsizedLabel(Component.literal(ellipsis), true);
        }

        int end = fullText.length();
        while (end > 0 && font.width(fullText.substring(0, end)) + ellipsisWidth > maxTextWidth) {
            end--;
        }

        if (end <= 0) {
            return new EllipsizedLabel(Component.literal(ellipsis), true);
        }
        return new EllipsizedLabel(Component.literal(fullText.substring(0, end) + ellipsis), true);
    }

    public interface Callbacks {
        void addWidget(AbstractWidget widget);

        void onPageSelected(int pageIndex);
    }

    public record Context(
            int leftPos,
            int topPos,
            int selectedPage,
            List<MarketplacePage> pages,
            Font font
    ) {
    }

    private record EllipsizedLabel(Component text, boolean truncated) {
    }

    private record TooltipTarget(int x, int y, int width, int height, Component fullLabel) {
        private boolean contains(int mouseX, int mouseY) {
            return mouseX >= x && mouseX < (x + width) && mouseY >= y && mouseY < (y + height);
        }
    }
}

