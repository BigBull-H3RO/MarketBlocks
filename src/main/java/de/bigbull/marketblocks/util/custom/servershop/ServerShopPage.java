package de.bigbull.marketblocks.util.custom.servershop;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Beschreibt eine Seite im Server-Shop.
 */
public final class ServerShopPage {
    public static final Codec<ServerShopPage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(ServerShopPage::name),
            Codec.STRING.optionalFieldOf("icon").forGetter(ServerShopPage::icon),
            ServerShopCategory.CODEC.listOf().fieldOf("categories").orElse(Collections.emptyList()).forGetter(ServerShopPage::categories)
    ).apply(instance, ServerShopPage::new));

    private String name;
    private Optional<String> icon;
    private final List<ServerShopCategory> categories;

    public ServerShopPage(String name, Optional<String> icon, List<ServerShopCategory> categories) {
        this.name = name == null ? "" : name;
        this.icon = icon == null ? Optional.empty() : icon;
        List<ServerShopCategory> categoryList = categories == null ? Collections.emptyList() : categories;
        this.categories = new ArrayList<>(categoryList.stream().map(ServerShopCategory::copy).collect(Collectors.toList()));
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

    public List<ServerShopCategory> categories() {
        return categories.stream().map(ServerShopCategory::copy).collect(Collectors.toUnmodifiableList());
    }

    List<ServerShopCategory> internalCategories() {
        return categories;
    }

    public void addCategory(ServerShopCategory category) {
        categories.add(Objects.requireNonNull(category, "category").copy());
    }

    public ServerShopCategory removeCategory(int index) {
        ServerShopCategory removed = categories.remove(index);
        return removed == null ? null : removed.copy();
    }

    public int indexOf(String categoryName) {
        for (int i = 0; i < categories.size(); i++) {
            if (categories.get(i).name().equalsIgnoreCase(categoryName)) {
                return i;
            }
        }
        return -1;
    }

    public int size() {
        return categories.size();
    }

    public ServerShopPage copy() {
        return new ServerShopPage(name, icon, categories);
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
                && Objects.equals(categories, other.categories);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, icon, categories);
    }
}