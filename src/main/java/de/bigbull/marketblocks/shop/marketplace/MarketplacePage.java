package de.bigbull.marketblocks.shop.marketplace;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a single page in the marketplace, holding a flat list of trade offers.
 */
public final class MarketplacePage {
    public static final Codec<MarketplacePage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(MarketplacePage::name),
            Codec.STRING.optionalFieldOf("icon").forGetter(MarketplacePage::icon),
            MarketplaceOffer.CODEC.listOf().fieldOf("offers").orElse(Collections.emptyList()).forGetter(MarketplacePage::offers)
    ).apply(instance, MarketplacePage::new));

    private String name;
    private Optional<String> icon;
    private final List<MarketplaceOffer> offers;

    public MarketplacePage(String name, Optional<String> icon, List<MarketplaceOffer> offers) {
        this.name = name == null ? "" : name;
        this.icon = icon == null ? Optional.empty() : icon;
        List<MarketplaceOffer> offerList = offers == null ? Collections.emptyList() : offers;
        this.offers = new ArrayList<>(offerList.stream().map(MarketplaceOffer::copy).collect(Collectors.toList()));
    }

    public String name() {
        return name;
    }

    public void rename(String newName) {
        this.name = newName == null ? "" : newName;
    }

    public Optional<String> icon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = Optional.ofNullable(icon);
    }

    public List<MarketplaceOffer> offers() {
        return offers.stream().map(MarketplaceOffer::copy).collect(Collectors.toUnmodifiableList());
    }

    /**
     * Package-private access to the mutable offer list, used by the shop manager to apply in-place edits.
     */
    List<MarketplaceOffer> internalOffers() {
        return offers;
    }

    public void addOffer(MarketplaceOffer offer) {
        offers.add(Objects.requireNonNull(offer, "offer").copy());
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

    public MarketplacePage copy() {
        return new MarketplacePage(name, icon, offers);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MarketplacePage other)) {
            return false;
        }
        return Objects.equals(name, other.name)
                && Objects.equals(icon, other.icon)
                && Objects.equals(offers, other.offers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, icon, offers);
    }
}