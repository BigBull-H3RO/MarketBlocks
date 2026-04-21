package de.bigbull.marketblocks.shop.visual;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerProfession;

public enum VillagerVisualProfession {
    NONE("none", "none"),
    ARMORER("armorer", "armorer"),
    BUTCHER("butcher", "butcher"),
    CARTOGRAPHER("cartographer", "cartographer"),
    CLERIC("cleric", "cleric"),
    FARMER("farmer", "farmer"),
    FISHERMAN("fisherman", "fisherman"),
    FLETCHER("fletcher", "fletcher"),
    LEATHERWORKER("leatherworker", "leatherworker"),
    LIBRARIAN("librarian", "librarian"),
    MASON("mason", "mason"),
    NITWIT("nitwit", "nitwit"),
    SHEPHERD("shepherd", "shepherd"),
    TOOLSMITH("toolsmith", "toolsmith"),
    WEAPONSMITH("weaponsmith", "weaponsmith");

    private final String serializedName;
    private final String vanillaId;

    VillagerVisualProfession(String serializedName, String vanillaId) {
        this.serializedName = serializedName;
        this.vanillaId = vanillaId;
    }

    public String serializedName() {
        return serializedName;
    }

    public String translationKey() {
        return "gui.marketblocks.visuals.profession." + serializedName;
    }

    public VillagerVisualProfession next() {
        VillagerVisualProfession[] values = values();
        int next = (ordinal() + 1) % values.length;
        return values[next];
    }

    public VillagerProfession toVillagerProfession() {
        if (this == NONE) {
            return VillagerProfession.NONE;
        }
        ResourceLocation id = ResourceLocation.withDefaultNamespace(vanillaId);
        return BuiltInRegistries.VILLAGER_PROFESSION.getOptional(id).orElse(VillagerProfession.NONE);
    }

    public static VillagerVisualProfession fromSerialized(String value) {
        if (value == null || value.isBlank()) {
            return NONE;
        }
        for (VillagerVisualProfession profession : values()) {
            if (profession.serializedName.equalsIgnoreCase(value)) {
                return profession;
            }
        }
        return NONE;
    }
}

