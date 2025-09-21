package de.bigbull.marketblocks.util.custom.servershop;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Beschreibt eine Kategorie innerhalb einer Shop-Seite.
 */
public final class ServerShopCategory {
    public static final Codec<ServerShopCategory> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(ServerShopCategory::name),
            Codec.BOOL.fieldOf("collapsed").orElse(false).forGetter(ServerShopCategory::collapsed),
            ServerShopOffer.CODEC.listOf().fieldOf("offers").orElse(Collections.emptyList()).forGetter(ServerShopCategory::offers)
    ).apply(instance, ServerShopCategory::new));

    private String name;
    private boolean collapsed;
    private final List<ServerShopOffer> offers;

    public ServerShopCategory(String name, boolean collapsed, List<ServerShopOffer> offers) {
        this.name = name == null ? "" : name;
        this.collapsed = collapsed;
        List<ServerShopOffer> offerList = offers == null ? Collections.emptyList() : offers;
        this.offers = new ArrayList<>(offerList.stream().map(ServerShopOffer::copy).collect(Collectors.toList()));
    }

    public String name() {
        return name;
    }

    public void rename(String newName) {
        this.name = newName == null ? "" : newName;
    }

    public boolean collapsed() {
        return collapsed;
    }

    public void setCollapsed(boolean collapsed) {
        this.collapsed = collapsed;
    }

    public List<ServerShopOffer> offers() {
        return offers.stream().map(ServerShopOffer::copy).collect(Collectors.toUnmodifiableList());
    }

    List<ServerShopOffer> internalOffers() {
        return offers;
    }

    public void addOffer(ServerShopOffer offer) {
        offers.add(Objects.requireNonNull(offer, "offer").copy());
    }

    public ServerShopOffer removeOffer(int index) {
        ServerShopOffer removed = offers.remove(index);
        return removed == null ? null : removed.copy();
    }

    public int findOfferIndex(UUID offerId) {
        for (int i = 0; i < offers.size(); i++) {
            if (offers.get(i).id().equals(offerId)) {
                return i;
            }
        }
        return -1;
    }

    public int size() {
        return offers.size();
    }

    public ServerShopCategory copy() {
        return new ServerShopCategory(name, collapsed, offers);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ServerShopCategory other)) {
            return false;
        }
        return collapsed == other.collapsed
                && Objects.equals(name, other.name)
                && Objects.equals(offers, other.offers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, collapsed, offers);
    }
}