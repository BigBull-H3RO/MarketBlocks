package de.bigbull.marketblocks.shop.server;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Vollständige Datenstruktur eines Server-Shops.
 */
public final class ServerShopData {
    public static final Codec<ServerShopData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ServerShopPage.CODEC.listOf().fieldOf("pages").orElse(Collections.emptyList()).forGetter(ServerShopData::pages)
    ).apply(instance, ServerShopData::new));

    private final List<ServerShopPage> pages;

    public ServerShopData(List<ServerShopPage> pages) {
        List<ServerShopPage> pageList = pages == null ? Collections.emptyList() : pages;
        this.pages = new ArrayList<>(pageList.stream().map(ServerShopPage::copy).collect(Collectors.toList()));
    }

    public static ServerShopData empty() {
        return new ServerShopData(Collections.emptyList());
    }

    public List<ServerShopPage> pages() {
        return pages.stream().map(ServerShopPage::copy).collect(Collectors.toUnmodifiableList());
    }

    List<ServerShopPage> internalPages() {
        return pages;
    }

    public void addPage(ServerShopPage page) {
        pages.add(Objects.requireNonNull(page, "page").copy());
    }

    public ServerShopPage removePage(int index) {
        ServerShopPage removed = pages.remove(index);
        return removed == null ? null : removed.copy();
    }

    public int size() {
        return pages.size();
    }

    public ServerShopData copy() {
        return new ServerShopData(pages);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ServerShopData other)) {
            return false;
        }
        return Objects.equals(pages, other.pages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pages);
    }
}