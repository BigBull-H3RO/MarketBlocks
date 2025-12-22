package de.bigbull.marketblocks.util.custom.servershop;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Beschreibt eine Seite im Server-Shop.
 * Jetzt direkt mit einer Liste von Angeboten (ohne Kategorien).
 */
public final class ServerShopPage {
    public static final Codec<ServerShopPage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(ServerShopPage::name),
            Codec.STRING.optionalFieldOf("icon").forGetter(ServerShopPage::icon),
            ServerShopOffer.CODEC.listOf().fieldOf("offers").orElse(Collections.emptyList()).forGetter(ServerShopPage::offers)
    ).apply(instance, ServerShopPage::new));

    private String name;
    private Optional<String> icon;
    private final List<ServerShopOffer> offers;

    public ServerShopPage(String name, Optional<String> icon, List<ServerShopOffer> offers) {
        this.name = name == null ? "" : name;
        this.icon = icon == null ? Optional.empty() : icon;
        List<ServerShopOffer> offerList = offers == null ? Collections.emptyList() : offers;
        this.offers = new ArrayList<>(offerList.stream().map(ServerShopOffer::copy).collect(Collectors.toList()));
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

    public List<ServerShopOffer> offers() {
        return offers.stream().map(ServerShopOffer::copy).collect(Collectors.toUnmodifiableList());
    }

    // Zugriff für den Manager (Package-Private)
    List<ServerShopOffer> internalOffers() {
        return offers;
    }

    public void addOffer(ServerShopOffer offer) {
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

    public ServerShopPage copy() {
        return new ServerShopPage(name, icon, offers);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ServerShopPage other)) {
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