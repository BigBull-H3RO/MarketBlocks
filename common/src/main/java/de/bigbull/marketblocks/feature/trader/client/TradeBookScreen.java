package de.bigbull.marketblocks.feature.trader.client;

import net.minecraft.ChatFormatting;
import net.minecraft.util.Util;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.PageButton;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;

import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import de.bigbull.marketblocks.Constants;
import de.bigbull.marketblocks.feature.trader.network.TradeBookOpenPacket.ShopOfferData;
import de.bigbull.marketblocks.feature.trader.client.tradebook.ITradeBookElement;
import de.bigbull.marketblocks.feature.trader.client.tradebook.InteractiveZone;
import de.bigbull.marketblocks.feature.trader.client.tradebook.TradeBookLayoutUtils;
import de.bigbull.marketblocks.feature.trader.client.tradebook.TradeBookRenderContext;
import de.bigbull.marketblocks.feature.trader.client.tradebook.elements.MarketTopElement;
import de.bigbull.marketblocks.feature.trader.client.tradebook.elements.MiscIconElement;
import de.bigbull.marketblocks.feature.trader.client.tradebook.elements.RecipeElement;
import de.bigbull.marketblocks.feature.trader.client.tradebook.elements.ShopEntryElement;
import de.bigbull.marketblocks.feature.trader.client.tradebook.elements.TopSellerElement;
import de.bigbull.marketblocks.feature.trader.client.tradebook.elements.TrendElement;

public class TradeBookScreen extends Screen {
    public static final Identifier BOOK_LOCATION = Identifier.fromNamespaceAndPath(Constants.MOD_ID,
            "textures/gui/tradebook/twoside_book.png");

    private static final int IMAGE_WIDTH = 280;
    private static final int IMAGE_HEIGHT = 180;

    private final List<Component> originalPages;
    private final Map<String, ShopOfferData> offers;
    private int currentPage = 0;
    private PageButton forwardButton;
    private PageButton backButton;

    private List<List<FormattedCharSequence>> paginatedLines;
    private Map<Integer, Float> pageScales = new HashMap<>();
    private Map<Integer, Integer> logicalToPhysical;
    private Map<Integer, Integer> physicalToLogical;

    private List<Runnable> tooltipRenderers = new ArrayList<>();
    private long hoverStartTime = 0;
    private Object currentHoveredObject = null;
    private Object nextHoveredObject = null;

    private final List<InteractiveZone> activeZones = new ArrayList<>();
    private final List<ITradeBookElement> elements = new ArrayList<>();

    public TradeBookScreen(List<Component> pages, Map<String, ShopOfferData> offers) {
        super(GameNarrator.NO_TITLE);
        this.originalPages = pages;
        this.offers = offers;
        
        elements.add(new ShopEntryElement());
        elements.add(new TopSellerElement());
        elements.add(new MarketTopElement());
        elements.add(new TrendElement());
        elements.add(new RecipeElement());
        elements.add(new MiscIconElement());
    }

    @Override
    protected void init() {
        super.init();

        paginate();

        int leftPos = (this.width - IMAGE_WIDTH) / 2;
        int topPos = 2;

        this.forwardButton = this.addRenderableWidget(
                new PageButton(leftPos + 233, topPos + 156, true, button -> this.pageForward(), true));
        this.backButton = this.addRenderableWidget(
                new PageButton(leftPos + 23, topPos + 156, false, button -> this.pageBack(), true));

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose())
                .bounds(this.width / 2 - 100, topPos + 196, 200, 20)
                .build());

        this.updateButtonVisibility();
    }

    private String extractSectionTitle(String pageText) {
        String[] titles = {
            Component.translatable("gui.marketblocks.trade_book.my_shops.title").getString(),
            Component.translatable("gui.marketblocks.trade_book.active.title").getString(),
            Component.translatable("gui.marketblocks.trade_book.shops.title").getString(),
            Component.translatable("gui.marketblocks.trade_book.marketplace.title").getString(),
            Component.translatable("gui.marketblocks.trade_book.trends.title").getString()
        };
        for (String title : titles) {
            if (pageText.startsWith(title)) {
                return title;
            }
        }
        return null;
    }

    private void paginate() {
        this.paginatedLines = new ArrayList<>();
        this.logicalToPhysical = new HashMap<>();
        this.physicalToLogical = new HashMap<>();
        this.pageScales = new HashMap<>();
        
        String lastSectionTitle = null;

        for (int i = 0; i < this.originalPages.size(); i++) {
            this.logicalToPhysical.put(i, this.paginatedLines.size());

            Component pageContent = getDisplayedPageContent(i);
            String unstrippedPageText = this.originalPages.get(i).getString();
            String currentSectionTitle = extractSectionTitle(unstrippedPageText);

            if (currentSectionTitle != null) {
                if (!currentSectionTitle.equals(lastSectionTitle)) {
                    if (this.paginatedLines.size() % 2 != 0) {
                        this.physicalToLogical.put(this.paginatedLines.size(), -1);
                        this.pageScales.put(this.paginatedLines.size(), TradeBookLayoutUtils.TEXT_SCALE);
                        this.paginatedLines.add(new ArrayList<>());
                    }
                    lastSectionTitle = currentSectionTitle;
                }
            } else {
                lastSectionTitle = null;
            }

            final float[] currentScale = { TradeBookLayoutUtils.TEXT_SCALE };
            pageContent.visit((style, text) -> {
                String ins = style.getInsertion();
                if (ins != null && ins.startsWith("PAGE_SCALE:")) {
                    try {
                        currentScale[0] = Float.parseFloat(ins.substring(11));
                    } catch (NumberFormatException ignored) {
                    }
                }
                return java.util.Optional.empty();
            }, Style.EMPTY);

            int scaledWidth = (int) (TradeBookLayoutUtils.TEXT_WIDTH / currentScale[0]);
            int maxPageHeight = (int) (138 / currentScale[0]);

            List<FormattedCharSequence> lines = this.font.split(pageContent, scaledWidth);

            if (lines.isEmpty()) {
                continue;
            }

            List<FormattedCharSequence> currentPageLines = new ArrayList<>();
            int currentHeight = 0;

            List<FormattedCharSequence> currentBlockLines = new ArrayList<>();
            int currentBlockHeight = 0;
            boolean inBlock = false;

            for (FormattedCharSequence line : lines) {
                int lineHeight = 9;
                int extraHeight = getLineExtraHeight(line);
                int totalLineHeight = lineHeight + extraHeight;

                boolean isStart = hasInsertion(line, "BLOCK_START");

                if (isStart && !inBlock) {
                    inBlock = true;
                }

                if (inBlock) {
                    currentBlockLines.add(line);
                    currentBlockHeight += totalLineHeight;

                    boolean isEnd = hasInsertion(line, "DIVIDER");
                    if (isEnd) {
                        if (!currentPageLines.isEmpty() && currentHeight + currentBlockHeight > maxPageHeight) {
                            this.physicalToLogical.put(this.paginatedLines.size(), i);
                            this.pageScales.put(this.paginatedLines.size(), currentScale[0]);
                            this.paginatedLines.add(new ArrayList<>(currentPageLines));
                            currentPageLines.clear();
                            currentHeight = 0;
                        }

                        currentPageLines.addAll(currentBlockLines);
                        currentHeight += currentBlockHeight;

                        currentBlockLines.clear();
                        currentBlockHeight = 0;
                        inBlock = false;
                    }
                } else {
                    if (!currentPageLines.isEmpty() && currentHeight + totalLineHeight > maxPageHeight) {
                        this.physicalToLogical.put(this.paginatedLines.size(), i);
                        this.pageScales.put(this.paginatedLines.size(), currentScale[0]);
                        this.paginatedLines.add(new ArrayList<>(currentPageLines));
                        currentPageLines.clear();
                        currentHeight = 0;
                    }
                    currentPageLines.add(line);
                    currentHeight += totalLineHeight;
                }
            }

            if (inBlock) {
                for (FormattedCharSequence line : currentBlockLines) {
                    int totalLineHeight = 9 + getLineExtraHeight(line);
                    if (!currentPageLines.isEmpty() && currentHeight + totalLineHeight > maxPageHeight) {
                        this.physicalToLogical.put(this.paginatedLines.size(), i);
                        this.pageScales.put(this.paginatedLines.size(), currentScale[0]);
                        this.paginatedLines.add(new ArrayList<>(currentPageLines));
                        currentPageLines.clear();
                        currentHeight = 0;
                    }
                    currentPageLines.add(line);
                    currentHeight += totalLineHeight;
                }
            }

            if (!currentPageLines.isEmpty() && !isPageVisuallyEmpty(currentPageLines)) {
                this.physicalToLogical.put(this.paginatedLines.size(), i);
                this.pageScales.put(this.paginatedLines.size(), currentScale[0]);
                this.paginatedLines.add(currentPageLines);
            }
        }
    }

    private boolean hasInsertion(FormattedCharSequence line, String targetPrefix) {
        boolean[] found = { false };
        line.accept((index, style, codePoint) -> {
            String ins = style.getInsertion();
            if (ins != null && ins.startsWith(targetPrefix)) {
                found[0] = true;
                return false;
            }
            return true;
        });
        return found[0];
    }

    private void pageForward() {
        if (this.currentPage + 2 < this.paginatedLines.size()) {
            this.currentPage += 2;
            this.updateButtonVisibility();
        }
    }

    private void pageBack() {
        if (this.currentPage > 0) {
            this.currentPage -= 2;
            this.updateButtonVisibility();
        }
    }

    private void updateButtonVisibility() {
        this.forwardButton.visible = this.currentPage + 2 < this.paginatedLines.size();
        this.backButton.visible = this.currentPage > 0;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (super.keyPressed(event)) {
            return true;
        } else {
            return switch (event.key()) {
                case 266 -> {
                    this.backButton.onPress(event);
                    yield true;
                }
                case 267 -> {
                    this.forwardButton.onPress(event);
                    yield true;
                }
                default -> false;
            };
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.extractTransparentBackground(guiGraphics);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BOOK_LOCATION, (this.width - IMAGE_WIDTH) / 2, 2, 121.0f, 0.0f, IMAGE_WIDTH, IMAGE_HEIGHT, 512, 256);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.nextHoveredObject = null;
        this.tooltipRenderers.clear();
        this.activeZones.clear();

        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
        int leftPos = (this.width - IMAGE_WIDTH) / 2;
        int topPos = 2;

        TradeBookRenderContext context = new TradeBookRenderContext(this.font, this.offers, this.tooltipRenderers, this.activeZones);

        if (!this.paginatedLines.isEmpty()) {
            // Render Left Page
            renderPage(guiGraphics, this.currentPage, leftPos + 18, topPos + 30, mouseX, mouseY, context);
            renderPageHeader(guiGraphics, this.currentPage, leftPos + 18, topPos, leftPos + 70);

            // Render Right Page
            if (this.currentPage + 1 < this.paginatedLines.size()) {
                renderPage(guiGraphics, this.currentPage + 1, leftPos + 150, topPos + 30, mouseX, mouseY, context);
                renderPageHeader(guiGraphics, this.currentPage + 1, leftPos + 148, topPos, leftPos + 210);
            }
        }

        for (InteractiveZone zone : this.activeZones) {
            if (zone.contains(mouseX, mouseY) && zone.getOnHover() != null) {
                zone.getOnHover().run();
            }
        }
        
        if (context.getNextHoveredObject() != null) {
            this.nextHoveredObject = context.getNextHoveredObject();
        }

        if (this.nextHoveredObject != null) {
            if (this.nextHoveredObject.equals(this.currentHoveredObject)) {
                if (Util.getMillis() - this.hoverStartTime > 500) {
                    for (Runnable renderer : this.tooltipRenderers) {
                        renderer.run();
                    }
                }
            } else {
                this.currentHoveredObject = this.nextHoveredObject;
                this.hoverStartTime = Util.getMillis();
            }
        } else {
            this.currentHoveredObject = null;
        }
    }

    private void renderPageHeader(GuiGraphicsExtractor guiGraphics, int pageIndex, int textX, int topPos, int centerX) {
        int logicalIndex = this.physicalToLogical.getOrDefault(pageIndex, -1);
        String sectionName = getSectionName(logicalIndex);
        if (sectionName != null) {
            Component sectionLabel = Component.literal(sectionName).withStyle(ChatFormatting.DARK_GRAY,
                    ChatFormatting.BOLD);
            int sectionWidth = this.font.width(sectionLabel);
            int headerX = textX + (TradeBookLayoutUtils.TEXT_WIDTH - sectionWidth) / 2;
            int headerY = topPos + 14;
            guiGraphics.text(this.font, sectionLabel, headerX, headerY, 0xFF383838, false);
            int lineY = headerY + 10;
            guiGraphics.fill(textX + 2, lineY, textX + TradeBookLayoutUtils.TEXT_WIDTH - 2, lineY + 1, 0x44000000);
        }
    }

    private void renderPage(GuiGraphicsExtractor guiGraphics, int pageIndex, int textX, int textY, int mouseX, int mouseY, TradeBookRenderContext context) {
        float scale = this.pageScales.getOrDefault(pageIndex, TradeBookLayoutUtils.TEXT_SCALE);
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().scale(scale, scale);

        int scaledTextX = (int) (textX / scale);
        int scaledTextY = (int) (textY / scale);
        int lineY = scaledTextY;

        List<FormattedCharSequence> lines = this.paginatedLines.get(pageIndex);
        for (FormattedCharSequence line : lines) {
            final int[] currentX = { scaledTextX };
            final String[] lastProcessedInsertion = { null };
            final int currentLineY = lineY;

            line.accept((index, style, codePoint) -> {
                String insertion = style.getInsertion();
                if (insertion != null && !insertion.equals(lastProcessedInsertion[0])) {
                    
                    for (ITradeBookElement element : elements) {
                        if (element.canHandle(insertion)) {
                            // Note: elements can use scaledTextX for drawing fixed at line start,
                            // or currentX[0] if they draw inline. For block entries, startX is usually scaledTextX.
                            // We pass currentX[0] so inline icons get drawn exactly where the character is.
                            int renderX = element.isInline() ? currentX[0] : scaledTextX;
                            element.render(guiGraphics, insertion, renderX, currentLineY, mouseX, mouseY, scale, context);
                            break;
                        }
                    }
                    
                    lastProcessedInsertion[0] = insertion;
                } else if (insertion == null) {
                    lastProcessedInsertion[0] = null;
                }
                currentX[0] += font.width(Character.toString(codePoint));
                return true;
            });

            guiGraphics.text(this.font, line, scaledTextX, lineY, 0xFF000000, false);

            int lineHeight = 9;
            int extraHeight = getLineExtraHeight(line);
            lineY += lineHeight + extraHeight;
        }
        guiGraphics.pose().popMatrix();

        Style style = this.getClickedStyleForPage(mouseX, mouseY, pageIndex, textX, textY);
        if (style != null && style.getHoverEvent() != null) {
            if (style.getHoverEvent() instanceof net.minecraft.network.chat.HoverEvent.ShowText showText) {
                guiGraphics.setTooltipForNextFrame(this.font, showText.value(), mouseX, mouseY);
            } else if (style.getHoverEvent() instanceof net.minecraft.network.chat.HoverEvent.ShowItem showItem) {
                guiGraphics.setTooltipForNextFrame(this.font, showItem.item().create(), mouseX, mouseY);
            }
        }
    }

    @Nullable
    public Style getClickedComponentStyleAt(double mouseX, double mouseY) {
        if (this.paginatedLines.isEmpty()) {
            return null;
        }
        int leftPos = (this.width - IMAGE_WIDTH) / 2;
        int topPos = 2;

        Style style = getClickedStyleForPage(mouseX, mouseY, this.currentPage, leftPos + 18, topPos + 30);
        if (style == null && this.currentPage + 1 < this.paginatedLines.size()) {
            style = getClickedStyleForPage(mouseX, mouseY, this.currentPage + 1, leftPos + 148, topPos + 30);
        }
        return style;
    }

    private Style getClickedStyleForPage(double mouseX, double mouseY, int pageIndex, int textX, int textY) {
        double relX = mouseX - textX;
        double relY = mouseY - textY;

        if (relX < 0 || relX > TradeBookLayoutUtils.TEXT_WIDTH || relY < 0 || relY > 138) {
            return null;
        }

        float scale = this.pageScales.getOrDefault(pageIndex, TradeBookLayoutUtils.TEXT_SCALE);
        double scaledRelX = relX / scale;
        double scaledRelY = relY / scale;

        List<FormattedCharSequence> lines = this.paginatedLines.get(pageIndex);
        int currentY = 0;

        for (FormattedCharSequence line : lines) {
            int lineHeight = 9;
            int extraHeight = getLineExtraHeight(line);

            int totalLineHeight = lineHeight + extraHeight;

            if (scaledRelY >= currentY && scaledRelY < currentY + totalLineHeight) {
                return findStyleAtWidth(line, (int) scaledRelX);
            }
            currentY += totalLineHeight;
        }
        return null;
    }

    /**
     * Finds the Style at a given pixel X position within a FormattedCharSequence.
     * Replacement for the removed StringSplitter.componentStyleAtWidth.
     */
    @Nullable
    private Style findStyleAtWidth(FormattedCharSequence sequence, int targetX) {
        final Style[] foundStyle = {null};
        final float[] currentX = {0.0f};
        sequence.accept((index, style, codePoint) -> {
            float charWidth = this.font.getSplitter().stringWidth(FormattedCharSequence.codepoint(codePoint, style));
            currentX[0] += charWidth;
            if (currentX[0] > targetX) {
                foundStyle[0] = style;
                return false;
            }
            return true;
        });
        return foundStyle[0];
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            double mouseX = event.x();
            double mouseY = event.y();
            for (InteractiveZone zone : this.activeZones) {
                if (zone.contains(mouseX, mouseY) && zone.getOnClick() != null) {
                    zone.getOnClick().run();
                    return true;
                }
            }

            Style style = this.getClickedComponentStyleAt(mouseX, mouseY);
            if (style != null && style.getClickEvent() != null && this.handleClickEvent(style.getClickEvent())) {
                return true;
            }
        }
        boolean result = super.mouseClicked(event, doubleClick);
        if (result) {
            this.setFocused(null);
            if (this.forwardButton != null)
                this.forwardButton.setFocused(false);
            if (this.backButton != null)
                this.backButton.setFocused(false);
        }
        return result;
    }

    protected boolean handleClickEvent(@Nullable ClickEvent clickevent) {
        if (clickevent == null) {
            return false;
        }
        switch (clickevent) {
            case ClickEvent.ChangePage(int page) -> {
                int logicalIndex = page - 1;
                if (this.logicalToPhysical.containsKey(logicalIndex)) {
                    int physicalIndex = this.logicalToPhysical.get(logicalIndex);
                    this.currentPage = (physicalIndex / 2) * 2;
                    this.updateButtonVisibility();
                    return true;
                }
                return false;
            }
            case ClickEvent.RunCommand runCommand -> {
                Screen.defaultHandleGameClickEvent(runCommand, this.minecraft, this);
                this.onClose();
                return true;
            }
            default -> {
                Screen.defaultHandleGameClickEvent(clickevent, this.minecraft, this);
                return true;
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean isInGameUi() {
        return true;
    }

    private Component getDisplayedPageContent(int logicalPageIndex) {
        Component page = this.originalPages.get(logicalPageIndex);
        String sectionName = getSectionName(logicalPageIndex);
        if (sectionName != null) {
            page = stripSectionTitle(page);
        }
        return page;
    }

    @Nullable
    private String getSectionName(int logicalPageIndex) {
        if (logicalPageIndex < 0 || logicalPageIndex >= this.originalPages.size()) {
            return null;
        }
        String text = this.originalPages.get(logicalPageIndex).getString();
        int start = text.indexOf("===");
        if (start >= 0) {
            int end = text.indexOf("===", start + 3);
            if (end > start) {
                String title = text.substring(start + 3, end).trim();
                if (!title.isEmpty()) {
                    return title;
                }
            }
        }
        return null;
    }

    private static final Pattern SECTION_TITLE_PATTERN = Pattern.compile("===\\s*[^=]+\\s*===\\s*");

    private Component stripSectionTitle(Component original) {
        String plainText = original.getString();
        Matcher matcher = SECTION_TITLE_PATTERN.matcher(plainText);
        if (!matcher.find() || matcher.start() != 0) {
            return original;
        }
        int charsToSkip = matcher.end();

        MutableComponent result = Component.empty();
        final int[] skipped = { 0 };

        original.visit((style, text) -> {
            if (skipped[0] >= charsToSkip) {
                result.append(Component.literal(text).withStyle(style));
            } else if (skipped[0] + text.length() > charsToSkip) {
                int skip = charsToSkip - skipped[0];
                result.append(Component.literal(text.substring(skip)).withStyle(style));
            }
            skipped[0] += text.length();
            return java.util.Optional.empty();
        }, Style.EMPTY);

        return result;
    }

    private int getLineExtraHeight(FormattedCharSequence line) {
        int[] extraHeight = { 0 };
        line.accept((index, style, codePoint) -> {
            String insertion = style.getInsertion();
            if (insertion != null) {
                if (insertion.startsWith("SPACING:")) {
                    try {
                        int spacing = Integer.parseInt(insertion.substring(8));
                        extraHeight[0] = Math.max(extraHeight[0], spacing);
                    } catch (NumberFormatException ignored) {
                    }
                } else {
                    for (ITradeBookElement element : elements) {
                        if (element.canHandle(insertion)) {
                            extraHeight[0] = Math.max(extraHeight[0], element.getExtraHeight(insertion));
                            break;
                        }
                    }
                }
            }
            return true;
        });
        return extraHeight[0];
    }

    private boolean isPageVisuallyEmpty(List<FormattedCharSequence> pageLines) {
        StringBuilder sb = new StringBuilder();
        for (FormattedCharSequence line : pageLines) {
            line.accept((index, style, codePoint) -> {
                String insertion = style.getInsertion();
                if (insertion != null) {
                    if (insertion.equals("DIVIDER")) {
                        return true; 
                    }
                    boolean handled = false;
                    for (ITradeBookElement element : elements) {
                        if (element.canHandle(insertion)) {
                            handled = true;
                            break;
                        }
                    }
                    if (handled) {
                        sb.append("x"); 
                        return true;
                    }
                }
                sb.appendCodePoint(codePoint);
                return true;
            });
        }
        return sb.toString().trim().isEmpty();
    }
}
