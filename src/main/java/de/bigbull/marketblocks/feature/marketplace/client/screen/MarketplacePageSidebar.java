package de.bigbull.marketblocks.feature.marketplace.client.screen;

import de.bigbull.marketblocks.feature.marketplace.data.MarketplacePage;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Handles Marketplace page sidebar buttons, pagination, truncation and delayed hover tooltips.
 */
public final class MarketplacePageSidebar {

    private static final int SCREEN_MARGIN_LEFT = 2;
    private static final int GUI_GAP_RIGHT = 2;
    private static final int TARGET_BUTTON_WIDTH = 120;
    private static final int MIN_BUTTON_WIDTH = 44;
    private static final int SIDEBAR_Y_OFFSET = 4;
    private static final int NAV_Y_OFFSET = -22;
    private static final int BUTTON_SPACING_Y = 22;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_TEXT_PADDING = 10;
    private static final long TOOLTIP_DELAY_MS = 450L;

    public static final int MAX_UNPAGINATED_BUTTONS = 9;
    public static final int BUTTONS_PER_PAGE = 9;
    private static final int NAV_BUTTON_WIDTH = 20;
    private static final int NAV_BUTTON_HEIGHT = 20;
    private static final int NAV_BG_COLOR = 0x50000000;

    private final List<TooltipTarget> tooltipTargets = new ArrayList<>();
    private TooltipTarget hoveredTooltipTarget;
    private long hoveredSinceMs;

    private int currentSidebarPage = 0;
    private int lastSelectedPage = -1;

    public static int calculateButtonWidth(int leftPos) {
        int maxAllowedWidth = leftPos - SCREEN_MARGIN_LEFT - GUI_GAP_RIGHT;
        if (maxAllowedWidth <= 0) {
            return MIN_BUTTON_WIDTH;
        }
        return Math.max(MIN_BUTTON_WIDTH, Math.min(TARGET_BUTTON_WIDTH, maxAllowedWidth));
    }

    public static int calculateBaseX(int leftPos, int buttonWidth) {
        return leftPos - GUI_GAP_RIGHT - buttonWidth;
    }

    public void reset() {
        tooltipTargets.clear();
        hoveredTooltipTarget = null;
        hoveredSinceMs = 0L;
    }

    public boolean isPaginated(int totalPages) {
        return totalPages > MAX_UNPAGINATED_BUTTONS;
    }

    public void buildButtons(Context context, Callbacks callbacks) {
        tooltipTargets.clear();
        hoveredTooltipTarget = null;

        int totalPages = context.pages().size();
        int buttonWidth = calculateButtonWidth(context.leftPos());
        int baseX = calculateBaseX(context.leftPos(), buttonWidth);
        int y = context.topPos() + SIDEBAR_Y_OFFSET;

        if (!isPaginated(totalPages)) {
            currentSidebarPage = 0;
            for (int i = 0; i < totalPages; i++) {
                addPageButton(context, callbacks, baseX, y, i, buttonWidth);
                y += BUTTON_SPACING_Y;
            }
            return;
        }

        int totalSidebarPages = (totalPages + BUTTONS_PER_PAGE - 1) / BUTTONS_PER_PAGE;

        // Auto-navigate to the sidebar page of selectedPage when selectedPage changed
        if (context.selectedPage() != lastSelectedPage) {
            lastSelectedPage = context.selectedPage();
            if (context.selectedPage() >= 0 && context.selectedPage() < totalPages) {
                currentSidebarPage = context.selectedPage() / BUTTONS_PER_PAGE;
            }
        }

        currentSidebarPage = Mth.clamp(currentSidebarPage, 0, totalSidebarPages - 1);

        // Header Navigation Bar [<  X/Y  >] at NAV_Y_OFFSET (same height as Add Page / Rename Page buttons)
        int navY = context.topPos() + NAV_Y_OFFSET;

        Button prevButton = Button.builder(Component.literal("<"), ignored -> {
            if (currentSidebarPage > 0) {
                currentSidebarPage--;
                callbacks.rebuildUi();
            }
        }).bounds(baseX, navY, NAV_BUTTON_WIDTH, NAV_BUTTON_HEIGHT)
          .build();
        prevButton.active = currentSidebarPage > 0;
        callbacks.addWidget(prevButton);

        Button nextButton = Button.builder(Component.literal(">"), ignored -> {
            if (currentSidebarPage < totalSidebarPages - 1) {
                currentSidebarPage++;
                callbacks.rebuildUi();
            }
        }).bounds(baseX + buttonWidth - NAV_BUTTON_WIDTH, navY, NAV_BUTTON_WIDTH, NAV_BUTTON_HEIGHT)
          .build();
        nextButton.active = currentSidebarPage < totalSidebarPages - 1;
        callbacks.addWidget(nextButton);

        int labelX = baseX + NAV_BUTTON_WIDTH;
        int labelWidth = buttonWidth - (NAV_BUTTON_WIDTH * 2);
        Component pageIndicator = Component.literal((currentSidebarPage + 1) + "/" + totalSidebarPages);
        AbstractWidget pageLabel = new AbstractWidget(labelX, navY, labelWidth, NAV_BUTTON_HEIGHT, pageIndicator) {
            @Override
            protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                // Slightly darker background between the two arrow buttons like JEI for better contrast
                guiGraphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), NAV_BG_COLOR);
                guiGraphics.drawCenteredString(context.font(), getMessage(), getX() + getWidth() / 2, getY() + (getHeight() - 8) / 2, 0xFFFFFFFF);
            }

            @Override
            protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
                defaultButtonNarrationText(narrationElementOutput);
            }
        };
        pageLabel.active = false;
        callbacks.addWidget(pageLabel);

        // Page Buttons for currentSidebarPage
        int startIndex = currentSidebarPage * BUTTONS_PER_PAGE;
        int endIndex = Math.min(totalPages, startIndex + BUTTONS_PER_PAGE);
        for (int i = startIndex; i < endIndex; i++) {
            addPageButton(context, callbacks, baseX, y, i, buttonWidth);
            y += BUTTON_SPACING_Y;
        }
    }

    private void addPageButton(Context context, Callbacks callbacks, int baseX, int y, int pageIndex, int buttonWidth) {
        MarketplacePage page = context.pages().get(pageIndex);
        Component fullLabel = pageDisplayName(page, pageIndex);
        EllipsizedLabel ellipsizedLabel = ellipsizeForButton(fullLabel, context.font(), buttonWidth);

        Button button = Button.builder(ellipsizedLabel.text(), ignored -> callbacks.onPageSelected(pageIndex))
                .bounds(baseX, y, buttonWidth, BUTTON_HEIGHT)
                .build();
        button.active = context.selectedPage() != pageIndex;
        callbacks.addWidget(button);

        if (ellipsizedLabel.truncated()) {
            tooltipTargets.add(new TooltipTarget(baseX, y, buttonWidth, BUTTON_HEIGHT, fullLabel));
        }
    }

    public boolean handleMouseScrolled(double mouseX, double mouseY, double scrollY, Context context, Callbacks callbacks) {
        int totalPages = context.pages().size();
        if (!isPaginated(totalPages)) {
            return false;
        }

        int buttonWidth = calculateButtonWidth(context.leftPos());
        int baseX = calculateBaseX(context.leftPos(), buttonWidth);
        int topY = context.topPos() + NAV_Y_OFFSET;
        int bottomY = context.topPos() + SIDEBAR_Y_OFFSET + BUTTONS_PER_PAGE * BUTTON_SPACING_Y;

        if (mouseX >= baseX && mouseX <= baseX + buttonWidth && mouseY >= topY && mouseY <= bottomY) {
            int totalSidebarPages = (totalPages + BUTTONS_PER_PAGE - 1) / BUTTONS_PER_PAGE;
            if (scrollY > 0 && currentSidebarPage > 0) {
                currentSidebarPage--;
                callbacks.rebuildUi();
                return true;
            } else if (scrollY < 0 && currentSidebarPage < totalSidebarPages - 1) {
                currentSidebarPage++;
                callbacks.rebuildUi();
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the extra area occupied by the page sidebar column for JEI / REI / EMI exclusions.
     * Blocks the entire vertical column band from y = 0 to screenHeight so JEI does not place
     * bookmark icons above or below the buttons in this column.
     */
    public List<Rect2i> getExtraAreas(int leftPos, int screenHeight) {
        int buttonWidth = calculateButtonWidth(leftPos);
        int startX = calculateBaseX(leftPos, buttonWidth);
        int clampedX = Math.max(0, startX);
        int width = leftPos - clampedX;
        if (width <= 0 || screenHeight <= 0) {
            return Collections.emptyList();
        }
        return List.of(new Rect2i(clampedX, 0, width, screenHeight));
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

    private EllipsizedLabel ellipsizeForButton(Component text, Font font, int buttonWidth) {
        int maxTextWidth = Math.max(0, buttonWidth - BUTTON_TEXT_PADDING);
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

        void rebuildUi();
    }

    public record Context(
            int leftPos,
            int topPos,
            int selectedPage,
            List<MarketplacePage> pages,
            Font font
    ) {
    }

    private record TooltipTarget(
            int x,
            int y,
            int width,
            int height,
            Component fullLabel
    ) {
        public boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        }
    }

    private record EllipsizedLabel(Component text, boolean truncated) {
    }
}
