package de.bigbull.marketblocks.util.custom.servershop;

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
            ServerShopPage.CODEC.listOf().fieldOf("pages").orElse(Collections.emptyList()).forGetter(ServerShopData::pages),
            Codec.INT.fieldOf("selected_page").orElse(0).forGetter(ServerShopData::selectedPage)
    ).apply(instance, ServerShopData::new));

    private final List<ServerShopPage> pages;
    private int selectedPage;

    public ServerShopData(List<ServerShopPage> pages, int selectedPage) {
        List<ServerShopPage> pageList = pages == null ? Collections.emptyList() : pages;
        this.pages = new ArrayList<>(pageList.stream().map(ServerShopPage::copy).collect(Collectors.toList()));
        this.selectedPage = Math.max(0, Math.min(selectedPage, Math.max(0, this.pages.size() - 1)));
    }

    public static ServerShopData empty() {
        return new ServerShopData(Collections.emptyList(), 0);
    }

    public List<ServerShopPage> pages() {
        return pages.stream().map(ServerShopPage::copy).collect(Collectors.toUnmodifiableList());
    }

    List<ServerShopPage> internalPages() {
        return pages;
    }

    public int selectedPage() {
        return selectedPage;
    }

    public void setSelectedPage(int selectedPage) {
        if (!pages.isEmpty()) {
            this.selectedPage = Math.max(0, Math.min(selectedPage, pages.size() - 1));
        } else {
            this.selectedPage = 0;
        }
    }

    public void addPage(ServerShopPage page) {
        pages.add(Objects.requireNonNull(page, "page").copy());
    }

    public ServerShopPage removePage(int index) {
        ServerShopPage removed = pages.remove(index);
        if (pages.isEmpty()) {
            selectedPage = 0;
        } else if (selectedPage >= pages.size()) {
            selectedPage = pages.size() - 1;
        }
        return removed == null ? null : removed.copy();
    }

    public int size() {
        return pages.size();
    }

    public ServerShopData copy() {
        return new ServerShopData(pages, selectedPage);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ServerShopData other)) {
            return false;
        }
        return selectedPage == other.selectedPage
                && Objects.equals(pages, other.pages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pages, selectedPage);
    }
}