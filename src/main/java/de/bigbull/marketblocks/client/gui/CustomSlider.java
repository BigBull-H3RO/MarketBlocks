package de.bigbull.marketblocks.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Modular custom slider widget with sleek pixel-art styling, dark groove track,
 * beveled metallic thumb, and header label with value badge.
 */
public class CustomSlider extends AbstractWidget {
    // Colors for track
    public static final int TRACK_BORDER = 0xFF141414;
    public static final int TRACK_FILLED_TOP = 0xFF626262;
    public static final int TRACK_FILLED_BOTTOM = 0xFF505050;
    public static final int TRACK_EMPTY_TOP = 0xFF383838;
    public static final int TRACK_EMPTY_BOTTOM = 0xFF282828;

    // Colors for thumb
    public static final int THUMB_BORDER = 0xFF141414;
    public static final int THUMB_HIGHLIGHT = 0xFFA2A2A2;
    public static final int THUMB_BODY = 0xFF727272;
    public static final int THUMB_SHADOW = 0xFF424242;
    public static final int THUMB_HIGHLIGHT_HOVER = 0xFFC4C4C4;
    public static final int THUMB_BODY_HOVER = 0xFF8A8A8A;
    public static final int THUMB_SHADOW_HOVER = 0xFF545454;

    // Colors for label and value badge
    public static final int BADGE_BG = 0xFF383838;
    public static final int BADGE_BORDER = 0xFF1C1C1C;
    public static final int BADGE_TEXT = 0xFFFFFFFF;
    public static final int LABEL_COLOR = 0x303030;

    protected final float min;
    protected final float max;
    protected float currentValue;
    protected float step;
    protected double ratio;
    protected Component prefix;
    protected Consumer<Float> onValueChanged;
    protected Function<Float, Component> valueFormatter;
    protected boolean showBadge = true;
    protected boolean showLabel = true;
    protected boolean isDragging = false;
    protected int labelColor = LABEL_COLOR;

    public CustomSlider(int x, int y, int width, int height, @Nullable Component prefix,
                        float min, float max, float value, @Nullable Consumer<Float> onValueChanged) {
        super(x, y, width, height, prefix != null ? prefix : Component.empty());
        this.prefix = prefix;
        this.min = min;
        this.max = max;
        this.step = 0.0f;
        this.currentValue = Mth.clamp(value, min, max);
        this.ratio = (max > min) ? (this.currentValue - min) / (max - min) : 0.0;
        this.onValueChanged = onValueChanged;
    }

    public CustomSlider setStep(float step) {
        this.step = step;
        return this;
    }

    public CustomSlider setBadge(boolean showBadge) {
        this.showBadge = showBadge;
        return this;
    }

    public CustomSlider setShowLabel(boolean show) {
        this.showLabel = show;
        return this;
    }

    public CustomSlider setLabelColor(int color) {
        this.labelColor = color;
        return this;
    }

    public CustomSlider setValueFormatter(Function<Float, Component> formatter) {
        this.valueFormatter = formatter;
        return this;
    }

    public CustomSlider setStringFormatter(Function<Float, String> formatter) {
        this.valueFormatter = val -> Component.literal(formatter.apply(val));
        return this;
    }

    public float getValue() {
        return this.currentValue;
    }

    public boolean isDragging() {
        return this.isDragging;
    }

    public void setValue(float val) {
        float clamped = Mth.clamp(val, this.min, this.max);
        if (this.step > 0.0f) {
            clamped = Math.round((clamped - this.min) / this.step) * this.step + this.min;
            clamped = Mth.clamp(clamped, this.min, this.max);
        }
        this.currentValue = clamped;
        this.ratio = (this.max > this.min) ? (this.currentValue - this.min) / (this.max - this.min) : 0.0;
        if (this.onValueChanged != null) {
            this.onValueChanged.accept(this.currentValue);
        }
    }

    public void setRatio(double r) {
        this.ratio = Mth.clamp(r, 0.0, 1.0);
        float val = this.min + (float) this.ratio * (this.max - this.min);
        if (this.step > 0.0f) {
            val = Math.round((val - this.min) / this.step) * this.step + this.min;
            val = Mth.clamp(val, this.min, this.max);
        }
        if (Math.abs(val - this.currentValue) > 1e-5f) {
            this.currentValue = val;
            this.ratio = (this.max > this.min) ? (this.currentValue - this.min) / (this.max - this.min) : 0.0;
            if (this.onValueChanged != null) {
                this.onValueChanged.accept(this.currentValue);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.active && this.visible) {
            if (this.isValidClickButton(button)) {
                boolean flag = this.clicked(mouseX, mouseY);
                if (flag) {
                    this.playDownSound(Minecraft.getInstance().getSoundManager());
                    this.isDragging = true;
                    updateFromMouse(mouseX);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        this.isDragging = true;
        updateFromMouse(mouseX);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.visible && this.active && this.isDragging && button == 0) {
            updateFromMouse(mouseX);
            return true;
        }
        return false;
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
        if (this.visible && this.active && this.isDragging) {
            updateFromMouse(mouseX);
        }
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        this.isDragging = false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            this.isDragging = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void updateFromMouse(double mouseX) {
        int trackX = getX();
        int trackWidth = this.width;
        int thumbWidth = 6;
        double d0 = (mouseX - (double) (trackX + thumbWidth / 2)) / (double) (trackWidth - thumbWidth);
        setRatio(d0);
    }

    public int getTrackY() {
        boolean compact = this.height < 14;
        int trackHeight = 5;
        return compact ? getY() + (this.height - trackHeight) / 2 : getY() + this.height - trackHeight - 2;
    }

    public int getThumbY() {
        int trackHeight = 5;
        int thumbHeight = 9;
        return getTrackY() - (thumbHeight - trackHeight) / 2;
    }

    public int getThumbX() {
        int thumbWidth = 6;
        return getX() + (int) (this.ratio * (this.width - thumbWidth));
    }

    @Override
    protected boolean clicked(double mouseX, double mouseY) {
        if (!this.active || !this.visible) {
            return false;
        }
        int thumbY = getThumbY();
        return mouseX >= (double) getX() && mouseX <= (double) (getX() + this.width)
                && mouseY >= (double) thumbY && mouseY <= (double) (getY() + this.height);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (this.visible && this.active && clicked(mouseX, mouseY)) {
            float delta = (this.step > 0 ? this.step : (this.max - this.min) / 20.0f) * (float) Math.signum(scrollY);
            setValue(this.currentValue + delta);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!this.active || !this.visible) {
            return false;
        }
        if (keyCode == 263) { // LEFT
            float delta = this.step > 0 ? this.step : (this.max - this.min) / 20.0f;
            setValue(this.currentValue - delta);
            return true;
        } else if (keyCode == 262) { // RIGHT
            float delta = this.step > 0 ? this.step : (this.max - this.min) / 20.0f;
            setValue(this.currentValue + delta);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        Font font = Minecraft.getInstance().font;
        int x = getX();
        int y = getY();
        int w = this.width;
        int h = this.height;

        boolean compact = h < 14;
        int trackHeight = 5;
        int thumbHeight = 9;
        int thumbWidth = 6;

        int trackY = getTrackY();
        int trackX = x;
        int trackWidth = w;

        int thumbX = getThumbX();
        int thumbY = getThumbY();

        // 1. Text & Badge Header Row (shifted 1px up to create clean gap to track/thumb)
        if (!compact && this.showLabel) {
            int headerY = y - 2;

            // Label
            if (this.prefix != null && !this.prefix.getString().isEmpty()) {
                int curLabelColor = this.active ? this.labelColor : 0x808080;
                graphics.drawString(font, GuiConstants.compact(this.prefix), x, headerY, curLabelColor, false);
            }

            // Value text
            Component valText = (this.valueFormatter != null)
                    ? this.valueFormatter.apply(this.currentValue)
                    : Component.literal(String.format(Locale.US, "%.2f", this.currentValue));

            int valWidth = font.width(valText);
            if (this.showBadge) {
                int badgeW = valWidth + 6;
                int badgeH = 9;
                int badgeX = x + w - badgeW;
                int badgeY = headerY;

                int badgeBg = this.active ? BADGE_BG : 0xFF222222;
                int badgeBorder = this.active ? BADGE_BORDER : 0xFF141414;
                int badgeText = this.active ? BADGE_TEXT : 0xFF808080;

                // Background
                graphics.fill(badgeX, badgeY, badgeX + badgeW, badgeY + badgeH, badgeBg);
                // 1px Border
                graphics.fill(badgeX, badgeY, badgeX + badgeW, badgeY + 1, badgeBorder);
                graphics.fill(badgeX, badgeY + badgeH - 1, badgeX + badgeW, badgeY + badgeH, badgeBorder);
                graphics.fill(badgeX, badgeY + 1, badgeX + 1, badgeY + badgeH - 1, badgeBorder);
                graphics.fill(badgeX + badgeW - 1, badgeY + 1, badgeX + badgeW, badgeY + badgeH - 1, badgeBorder);

                // Text
                graphics.drawString(font, valText, badgeX + 3, badgeY + 1, badgeText, false);
            } else {
                int curLabelColor = this.active ? this.labelColor : 0x808080;
                graphics.drawString(font, valText, x + w - valWidth, headerY, curLabelColor, false);
            }
        }

        // 2. Track
        int trackBorder = this.active ? TRACK_BORDER : 0xFF141414;
        int trackFilledTop = this.active ? TRACK_FILLED_TOP : 0xFF3C3C3C;
        int trackFilledBottom = this.active ? TRACK_FILLED_BOTTOM : 0xFF303030;
        int trackEmptyTop = this.active ? TRACK_EMPTY_TOP : 0xFF242424;
        int trackEmptyBottom = this.active ? TRACK_EMPTY_BOTTOM : 0xFF1A1A1A;

        // Border
        graphics.fill(trackX, trackY, trackX + trackWidth, trackY + 1, trackBorder);
        graphics.fill(trackX, trackY + trackHeight - 1, trackX + trackWidth, trackY + trackHeight, trackBorder);
        graphics.fill(trackX, trackY + 1, trackX + 1, trackY + trackHeight - 1, trackBorder);
        graphics.fill(trackX + trackWidth - 1, trackY + 1, trackX + trackWidth, trackY + trackHeight - 1, trackBorder);

        int progressSplit = Math.clamp(thumbX + thumbWidth / 2, trackX + 1, trackX + trackWidth - 1);

        // Progress fill (left of thumb)
        if (progressSplit > trackX + 1) {
            graphics.fill(trackX + 1, trackY + 1, progressSplit, trackY + 2, trackFilledTop);
            graphics.fill(trackX + 1, trackY + 2, progressSplit, trackY + trackHeight - 1, trackFilledBottom);
        }
        // Empty track (right of thumb)
        if (progressSplit < trackX + trackWidth - 1) {
            graphics.fill(progressSplit, trackY + 1, trackX + trackWidth - 1, trackY + 2, trackEmptyTop);
            graphics.fill(progressSplit, trackY + 2, trackX + trackWidth - 1, trackY + trackHeight - 1, trackEmptyBottom);
        }

        // 3. Thumb (metallic shaded button handle) - highlights only when mouse is over thumb or dragging
        int highlight;
        int body;
        int shadow;
        if (!this.active) {
            highlight = 0xFF585858;
            body = 0xFF404040;
            shadow = 0xFF282828;
        } else {
            boolean isMouseOverThumb = mouseX >= thumbX && mouseX < thumbX + thumbWidth
                    && mouseY >= thumbY && mouseY < thumbY + thumbHeight;
            boolean hovered = isMouseOverThumb || this.isDragging;
            highlight = hovered ? THUMB_HIGHLIGHT_HOVER : THUMB_HIGHLIGHT;
            body = hovered ? THUMB_BODY_HOVER : THUMB_BODY;
            shadow = hovered ? THUMB_SHADOW_HOVER : THUMB_SHADOW;
        }

        // Thumb border
        graphics.fill(thumbX, thumbY, thumbX + thumbWidth, thumbY + 1, THUMB_BORDER);
        graphics.fill(thumbX, thumbY + thumbHeight - 1, thumbX + thumbWidth, thumbY + thumbHeight, THUMB_BORDER);
        graphics.fill(thumbX, thumbY + 1, thumbX + 1, thumbY + thumbHeight - 1, THUMB_BORDER);
        graphics.fill(thumbX + thumbWidth - 1, thumbY + 1, thumbX + thumbWidth, thumbY + thumbHeight - 1, THUMB_BORDER);

        // Thumb interior
        graphics.fill(thumbX + 1, thumbY + 1, thumbX + thumbWidth - 1, thumbY + 2, highlight);
        graphics.fill(thumbX + 1, thumbY + 2, thumbX + 2, thumbY + thumbHeight - 2, highlight);
        graphics.fill(thumbX + 2, thumbY + 2, thumbX + thumbWidth - 2, thumbY + thumbHeight - 2, body);
        graphics.fill(thumbX + thumbWidth - 2, thumbY + 2, thumbX + thumbWidth - 1, thumbY + thumbHeight - 2, shadow);
        graphics.fill(thumbX + 1, thumbY + thumbHeight - 2, thumbX + thumbWidth - 1, thumbY + thumbHeight - 1, shadow);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narration) {
        this.defaultButtonNarrationText(narration);
    }
}
