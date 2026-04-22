package de.bigbull.marketblocks.shop.visual;

import net.minecraft.nbt.CompoundTag;

public record ShopVisualSettings(
        boolean npcEnabled,
        String npcName,
        VillagerVisualProfession profession,
        boolean purchaseParticlesEnabled,
        boolean purchaseSoundsEnabled,
        boolean paymentSlotSoundsEnabled
) {
    private static final int MAX_NPC_NAME_LENGTH = 32;

    private static final String KEY_NPC_ENABLED = "NpcEnabled";
    private static final String KEY_NPC_NAME = "NpcName";
    private static final String KEY_PROFESSION = "NpcProfession";
    private static final String KEY_PURCHASE_PARTICLES = "PurchaseParticles";
    private static final String KEY_PURCHASE_SOUNDS = "PurchaseSounds";
    private static final String KEY_PAYMENT_SLOT_SOUNDS = "PaymentSlotSounds";

    public static final ShopVisualSettings DEFAULT = new ShopVisualSettings(false, "", VillagerVisualProfession.NONE, true, true, true);

    public ShopVisualSettings {
        npcName = sanitizeNpcName(npcName);
        profession = profession == null ? VillagerVisualProfession.NONE : profession;
    }

    public ShopVisualSettings withNpcEnabled(boolean enabled) {
        return new ShopVisualSettings(enabled, npcName, profession, purchaseParticlesEnabled, purchaseSoundsEnabled, paymentSlotSoundsEnabled);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(KEY_NPC_ENABLED, npcEnabled);
        tag.putString(KEY_NPC_NAME, npcName);
        tag.putString(KEY_PROFESSION, profession.serializedName());
        tag.putBoolean(KEY_PURCHASE_PARTICLES, purchaseParticlesEnabled);
        tag.putBoolean(KEY_PURCHASE_SOUNDS, purchaseSoundsEnabled);
        tag.putBoolean(KEY_PAYMENT_SLOT_SOUNDS, paymentSlotSoundsEnabled);
        return tag;
    }

    public static ShopVisualSettings load(CompoundTag tag) {
        if (tag == null) {
            return DEFAULT;
        }
        return new ShopVisualSettings(
                tag.getBoolean(KEY_NPC_ENABLED),
                tag.getString(KEY_NPC_NAME),
                VillagerVisualProfession.fromSerialized(tag.getString(KEY_PROFESSION)),
                !tag.contains(KEY_PURCHASE_PARTICLES) || tag.getBoolean(KEY_PURCHASE_PARTICLES),
                !tag.contains(KEY_PURCHASE_SOUNDS) || tag.getBoolean(KEY_PURCHASE_SOUNDS),
                !tag.contains(KEY_PAYMENT_SLOT_SOUNDS) || tag.getBoolean(KEY_PAYMENT_SLOT_SOUNDS)
        );
    }

    public static String sanitizeNpcName(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        String sanitized = raw.strip().replaceAll("[^\\p{L}\\p{N} _-]", "");
        if (sanitized.length() > MAX_NPC_NAME_LENGTH) {
            sanitized = sanitized.substring(0, MAX_NPC_NAME_LENGTH);
        }
        return sanitized;
    }
}

