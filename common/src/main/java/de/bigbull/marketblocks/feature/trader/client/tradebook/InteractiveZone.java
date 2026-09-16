package de.bigbull.marketblocks.feature.trader.client.tradebook;

public class InteractiveZone {
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final Runnable onHover;
    private final Runnable onClick;

    public InteractiveZone(int x, int y, int width, int height, Runnable onHover, Runnable onClick) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.onHover = onHover;
        this.onClick = onClick;
    }

    public boolean contains(double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    public Runnable getOnHover() {
        return onHover;
    }

    public Runnable getOnClick() {
        return onClick;
    }
}
