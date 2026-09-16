package de.bigbull.marketblocks.client.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/**
 * Modular helper for rendering titled border frames ("GroupBox" / "Fieldset")
 * in Minecraft GUI style with optional rounded corners.
 */
public final class GroupBox {
    // Default Minecraft container palette
    public static final int DEFAULT_BORDER_COLOR = 0xFF555555;      // Medium-dark border line
    public static final int DEFAULT_BORDER_HIGHLIGHT = 0xFFFFFFFF;  // Lower/right highlight for etched style
    public static final int DEFAULT_BG_COLOR = 0xFFC6C6C6;          // Vanilla container gray background
    public static final int DEFAULT_TITLE_COLOR = 0x303030;         // Dark gray text color
    public static final int DEFAULT_TITLE_INDENT = 6;               // Distance from left edge to title start
    public static final int DEFAULT_TITLE_PADDING = 3;              // Gap before and after title text
    public static final int DEFAULT_CORNER_RADIUS = 2;              // Subtle 1px diagonal corner rounding (radius 2)

    private GroupBox() {
    }

    /**
     * Renders a flat GroupBox with default colors and standard rounded corners (radius 2).
     */
    public static void render(GuiGraphics graphics, Font font, @Nullable Component title, int x, int y, int width, int height) {
        render(graphics, font, title, x, y, width, height, DEFAULT_BORDER_COLOR, DEFAULT_TITLE_COLOR, DEFAULT_BG_COLOR, 0, DEFAULT_CORNER_RADIUS);
    }

    /**
     * Renders a flat GroupBox with default colors and a specific corner radius.
     */
    public static void render(GuiGraphics graphics, Font font, @Nullable Component title, int x, int y, int width, int height, int cornerRadius) {
        render(graphics, font, title, x, y, width, height, DEFAULT_BORDER_COLOR, DEFAULT_TITLE_COLOR, DEFAULT_BG_COLOR, 0, cornerRadius);
    }

    /**
     * Renders a flat GroupBox with custom border, title and background cutout colors.
     */
    public static void render(GuiGraphics graphics, Font font, @Nullable Component title, int x, int y, int width, int height,
                              int borderColor, int titleColor, int bgColor) {
        render(graphics, font, title, x, y, width, height, borderColor, titleColor, bgColor, 0, DEFAULT_CORNER_RADIUS);
    }

    /**
     * Renders a flat GroupBox with optional inner background tint.
     */
    public static void render(GuiGraphics graphics, Font font, @Nullable Component title, int x, int y, int width, int height,
                              int borderColor, int titleColor, int bgColor, int innerBgColor) {
        render(graphics, font, title, x, y, width, height, borderColor, titleColor, bgColor, innerBgColor, DEFAULT_CORNER_RADIUS);
    }

    /**
     * Renders a flat GroupBox with full customization including corner radius.
     * Corner radius:
     *   0 or 1: sharp 90° corners
     *   2: subtle 1-pixel diagonal bevel / rounded corner
     *   3+: gentler 2-pixel rounded curve
     */
    public static void render(GuiGraphics graphics, Font font, @Nullable Component title, int x, int y, int width, int height,
                              int borderColor, int titleColor, int bgColor, int innerBgColor, int cornerRadius) {
        int r = Math.max(0, cornerRadius);

        // Optional inner background fill
        if ((innerBgColor & 0xFF000000) != 0) {
            if (r >= 2) {
                graphics.fill(x + 1, y + 2, x + width - 1, y + height - 2, innerBgColor);
                graphics.fill(x + 2, y + 1, x + width - 2, y + 2, innerBgColor);
                graphics.fill(x + 2, y + height - 2, x + width - 2, y + height - 1, innerBgColor);
            } else {
                graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, innerBgColor);
            }
        }

        // Bottom horizontal line
        graphics.fill(x + r, y + height - 1, x + width - r, y + height, borderColor);

        // Left vertical line
        graphics.fill(x, y + r, x + 1, y + height - r, borderColor);

        // Right vertical line
        graphics.fill(x + width - 1, y + r, x + width, y + height - r, borderColor);

        // Top line & title
        if (title == null || title.getString().isEmpty()) {
            graphics.fill(x + r, y, x + width - r, y + 1, borderColor);
        } else {
            Component styledTitle = GuiConstants.compact(title);
            int textWidth = font.width(styledTitle);
            int textX = x + DEFAULT_TITLE_INDENT + DEFAULT_TITLE_PADDING;
            int textY = y - (font.lineHeight / 2);

            int cutStart = x + DEFAULT_TITLE_INDENT;
            int cutEnd = textX + textWidth + DEFAULT_TITLE_PADDING;

            // Top line left of title
            if (cutStart > x + r) {
                graphics.fill(x + r, y, cutStart, y + 1, borderColor);
            }
            // Top line right of title
            if (cutEnd < x + width - r) {
                graphics.fill(cutEnd, y, x + width - r, y + 1, borderColor);
            }

            // Cutout background under title text to clear any underlying texture
            graphics.fill(cutStart, textY, cutEnd, textY + font.lineHeight, bgColor);

            // Draw title
            graphics.drawString(font, styledTitle, textX, textY, titleColor, false);
        }

        // Draw rounded corner pixels
        renderCorners(graphics, x, y, width, height, r, borderColor);
    }

    /**
     * Renders corner pixels for rounded borders.
     */
    private static void renderCorners(GuiGraphics graphics, int x, int y, int width, int height, int radius, int color) {
        if (radius <= 1) {
            return;
        }
        if (radius == 2) {
            // 1-pixel diagonal step (classic Minecraft pixel art rounded corner)
            graphics.fill(x + 1, y + 1, x + 2, y + 2, color);                             // Top-left
            graphics.fill(x + width - 2, y + 1, x + width - 1, y + 2, color);             // Top-right
            graphics.fill(x + 1, y + height - 2, x + 2, y + height - 1, color);           // Bottom-left
            graphics.fill(x + width - 2, y + height - 2, x + width - 1, y + height - 1, color); // Bottom-right
        } else {
            // 2-pixel diagonal step (gentler radius >= 3)
            // Top-left
            graphics.fill(x + 2, y + 1, x + 3, y + 2, color);
            graphics.fill(x + 1, y + 2, x + 2, y + 3, color);
            // Top-right
            graphics.fill(x + width - 3, y + 1, x + width - 2, y + 2, color);
            graphics.fill(x + width - 2, y + 2, x + width - 1, y + 3, color);
            // Bottom-left
            graphics.fill(x + 1, y + height - 3, x + 2, y + height - 2, color);
            graphics.fill(x + 2, y + height - 2, x + 3, y + height - 1, color);
            // Bottom-right
            graphics.fill(x + width - 2, y + height - 3, x + width - 1, y + height - 2, color);
            graphics.fill(x + width - 3, y + height - 2, x + width - 2, y + height - 1, color);
        }
    }

    /**
     * Renders an etched/beveled 3D GroupBox in classic Minecraft sunken frame style.
     */
    public static void renderEtched(GuiGraphics graphics, Font font, @Nullable Component title, int x, int y, int width, int height) {
        renderEtched(graphics, font, title, x, y, width, height, 0xFF373737, 0xFFFFFFFF, DEFAULT_TITLE_COLOR, DEFAULT_BG_COLOR, 0);
    }

    /**
     * Full-featured etched GroupBox with 3D bevel.
     */
    public static void renderEtched(GuiGraphics graphics, Font font, @Nullable Component title, int x, int y, int width, int height,
                                    int shadowColor, int highlightColor, int titleColor, int bgColor, int innerBgColor) {
        // Optional inner fill
        if ((innerBgColor & 0xFF000000) != 0) {
            graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, innerBgColor);
        }

        // Inner highlight (bottom & right)
        graphics.fill(x + 1, y + height - 1, x + width, y + height, highlightColor);
        graphics.fill(x + width - 1, y + 1, x + width, y + height, highlightColor);

        // Outer shadow (top, left, bottom-1, right-1)
        graphics.fill(x, y, x + 1, y + height - 1, shadowColor);
        graphics.fill(x + width - 2, y, x + width - 1, y + height - 1, shadowColor);
        graphics.fill(x, y + height - 2, x + width - 1, y + height - 1, shadowColor);

        if (title == null || title.getString().isEmpty()) {
            graphics.fill(x, y, x + width - 1, y + 1, shadowColor);
            return;
        }

        Component styledTitle = GuiConstants.compact(title);
        int textWidth = font.width(styledTitle);
        int textX = x + DEFAULT_TITLE_INDENT + DEFAULT_TITLE_PADDING;
        int textY = y - (font.lineHeight / 2);

        int cutStart = x + DEFAULT_TITLE_INDENT;
        int cutEnd = textX + textWidth + DEFAULT_TITLE_PADDING;

        if (cutStart > x) {
            graphics.fill(x, y, cutStart, y + 1, shadowColor);
        }
        if (cutEnd < x + width - 1) {
            graphics.fill(cutEnd, y, x + width - 1, y + 1, shadowColor);
        }

        // Cutout background under title text
        graphics.fill(cutStart, textY, cutEnd, textY + font.lineHeight, bgColor);

        // Draw title
        graphics.drawString(font, styledTitle, textX, textY, titleColor, false);
    }
}
