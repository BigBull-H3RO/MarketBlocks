package de.bigbull.marketblocks.feature.marketplace.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Vollständige Datenstruktur eines Marktplatzs.
 */
public final class MarketplaceData {
    public static final Codec<MarketplaceData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            MarketplacePage.CODEC.listOf().fieldOf("pages").orElse(Collections.emptyList()).forGetter(MarketplaceData::pages)
    ).apply(instance, MarketplaceData::new));

    private final List<MarketplacePage> pages;

    public MarketplaceData(List<MarketplacePage> pages) {
        List<MarketplacePage> pageList = pages == null ? Collections.emptyList() : pages;
        this.pages = new ArrayList<>(pageList.stream().map(MarketplacePage::copy).collect(Collectors.toList()));
    }

    public static MarketplaceData empty() {
        return new MarketplaceData(Collections.emptyList());
    }

    public List<MarketplacePage> pages() {
        return pages.stream().map(MarketplacePage::copy).collect(Collectors.toUnmodifiableList());
    }

    List<MarketplacePage> internalPages() {
        return pages;
    }

    public void addPage(MarketplacePage page) {
        pages.add(Objects.requireNonNull(page, "page").copy());
    }

    public MarketplacePage removePage(int index) {
        MarketplacePage removed = pages.remove(index);
        return removed == null ? null : removed.copy();
    }

    public int size() {
        return pages.size();
    }

    public MarketplaceData copy() {
        return new MarketplaceData(pages);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MarketplaceData other)) {
            return false;
        }
        return Objects.equals(pages, other.pages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pages);
    }
}