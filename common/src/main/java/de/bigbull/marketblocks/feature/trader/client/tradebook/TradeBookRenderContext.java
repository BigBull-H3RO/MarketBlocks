package de.bigbull.marketblocks.feature.trader.client.tradebook;

import net.minecraft.client.gui.Font;
import java.util.List;
import java.util.Map;
import de.bigbull.marketblocks.feature.trader.network.TradeBookOpenPacket.ShopOfferData;

public class TradeBookRenderContext {
    private final Font font;
    private final Map<String, ShopOfferData> offers;
    private final List<Runnable> tooltipRenderers;
    private final List<InteractiveZone> activeZones;
    private Object nextHoveredObject;

    public TradeBookRenderContext(Font font, Map<String, ShopOfferData> offers, List<Runnable> tooltipRenderers, List<InteractiveZone> activeZones) {
        this.font = font;
        this.offers = offers;
        this.tooltipRenderers = tooltipRenderers;
        this.activeZones = activeZones;
    }

    public Font getFont() {
        return font;
    }

    public Map<String, ShopOfferData> getOffers() {
        return offers;
    }

    public void addTooltip(Runnable tooltipRenderer) {
        this.tooltipRenderers.add(tooltipRenderer);
    }

    public void addActiveZone(InteractiveZone zone) {
        this.activeZones.add(zone);
    }

    public Object getNextHoveredObject() {
        return nextHoveredObject;
    }

    public void setNextHoveredObject(Object nextHoveredObject) {
        this.nextHoveredObject = nextHoveredObject;
    }
}
