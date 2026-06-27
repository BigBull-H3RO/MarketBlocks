package de.bigbull.marketblocks.feature.singleoffer.settings;

import de.bigbull.marketblocks.feature.visual.npc.VillagerVisualProfession;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import de.bigbull.marketblocks.util.NameValidator;

/**
 * Settings for the Villager tab: NPC toggle, name, profession, and feedback effects.
 */
public record VillagerSettings(
        boolean npcEnabled,
        String npcName,
        VillagerVisualProfession profession,
        boolean purchaseParticlesEnabled,
        boolean purchaseSoundsEnabled,
        boolean paymentSlotSoundsEnabled,
        boolean usePlayerSkin,
        String playerSkinName
) {
    private static final int MAX_PLAYER_SKIN_NAME_LENGTH = 36;

    private static final String KEY_NPC_ENABLED = "NpcEnabled";
    private static final String KEY_NPC_NAME = "NpcName";
    private static final String KEY_PROFESSION = "NpcProfession";
    private static final String KEY_PURCHASE_PARTICLES = "PurchaseParticles";
    private static final String KEY_PURCHASE_SOUNDS = "PurchaseSounds";
    private static final String KEY_PAYMENT_SLOT_SOUNDS = "PaymentSlotSounds";
    private static final String KEY_USE_PLAYER_SKIN = "UsePlayerSkin";
    private static final String KEY_PLAYER_SKIN_NAME = "PlayerSkinName";

    public static final VillagerSettings DEFAULT = new VillagerSettings(
            false, "", VillagerVisualProfession.NONE, false, false, false, false, ""
    );

    public static final StreamCodec<ByteBuf, VillagerSettings> STREAM_CODEC = StreamCodec.of(
            (buf, settings) -> {
                ByteBufCodecs.BOOL.encode(buf, settings.npcEnabled());
                ByteBufCodecs.STRING_UTF8.encode(buf, settings.npcName());
                ByteBufCodecs.STRING_UTF8.encode(buf, settings.profession().serializedName());
                ByteBufCodecs.BOOL.encode(buf, settings.purchaseParticlesEnabled());
                ByteBufCodecs.BOOL.encode(buf, settings.purchaseSoundsEnabled());
                ByteBufCodecs.BOOL.encode(buf, settings.paymentSlotSoundsEnabled());
                ByteBufCodecs.BOOL.encode(buf, settings.usePlayerSkin());
                ByteBufCodecs.STRING_UTF8.encode(buf, settings.playerSkinName());
            },
            buf -> new VillagerSettings(
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    VillagerVisualProfession.fromSerialized(ByteBufCodecs.STRING_UTF8.decode(buf)),
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf)
            )
    );

    public VillagerSettings {
        npcName = sanitizeNpcName(npcName);
        profession = profession == null ? VillagerVisualProfession.NONE : profession;
        playerSkinName = sanitizePlayerSkinName(playerSkinName);
    }

    public VillagerSettings withNpcEnabled(boolean enabled) {
        return new VillagerSettings(enabled, npcName, profession, purchaseParticlesEnabled, purchaseSoundsEnabled, paymentSlotSoundsEnabled, usePlayerSkin, playerSkinName);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(KEY_NPC_ENABLED, npcEnabled);
        tag.putString(KEY_NPC_NAME, npcName);
        tag.putString(KEY_PROFESSION, profession.serializedName());
        tag.putBoolean(KEY_PURCHASE_PARTICLES, purchaseParticlesEnabled);
        tag.putBoolean(KEY_PURCHASE_SOUNDS, purchaseSoundsEnabled);
        tag.putBoolean(KEY_PAYMENT_SLOT_SOUNDS, paymentSlotSoundsEnabled);
        tag.putBoolean(KEY_USE_PLAYER_SKIN, usePlayerSkin);
        tag.putString(KEY_PLAYER_SKIN_NAME, playerSkinName);
        return tag;
    }

    public static VillagerSettings load(CompoundTag tag) {
        if (tag == null) return DEFAULT;
        return new VillagerSettings(
                tag.getBoolean(KEY_NPC_ENABLED),
                tag.getString(KEY_NPC_NAME),
                VillagerVisualProfession.fromSerialized(tag.getString(KEY_PROFESSION)),
                !tag.contains(KEY_PURCHASE_PARTICLES) || tag.getBoolean(KEY_PURCHASE_PARTICLES),
                !tag.contains(KEY_PURCHASE_SOUNDS) || tag.getBoolean(KEY_PURCHASE_SOUNDS),
                !tag.contains(KEY_PAYMENT_SLOT_SOUNDS) || tag.getBoolean(KEY_PAYMENT_SLOT_SOUNDS),
                tag.getBoolean(KEY_USE_PLAYER_SKIN),
                tag.getString(KEY_PLAYER_SKIN_NAME)
        );
    }

    public static String sanitizeNpcName(String raw) {
        return NameValidator.sanitizeNpcName(raw);
    }

    public static String sanitizePlayerSkinName(String raw) {
        if (raw == null || raw.isBlank()) return "";
        String sanitized = raw.strip().replaceAll("[^\\p{L}\\p{N} _-]", "");
        if (sanitized.length() > MAX_PLAYER_SKIN_NAME_LENGTH) {
            sanitized = sanitized.substring(0, MAX_PLAYER_SKIN_NAME_LENGTH);
        }
        return sanitized;
    }

    /**
     * Mutable draft for the Villager settings tab in the GUI.
     */
    public static final class Draft {
        private boolean npcEnabled;
        private String npcName;
        private VillagerVisualProfession profession;
        private boolean purchaseParticlesEnabled;
        private boolean purchaseSoundsEnabled;
        private boolean paymentSlotSoundsEnabled;
        private boolean usePlayerSkin;
        private String playerSkinName;

        public Draft(VillagerSettings settings) {
            VillagerSettings s = settings == null ? DEFAULT : settings;
            this.npcEnabled = s.npcEnabled();
            this.npcName = s.npcName();
            this.profession = s.profession();
            this.purchaseParticlesEnabled = s.purchaseParticlesEnabled();
            this.purchaseSoundsEnabled = s.purchaseSoundsEnabled();
            this.paymentSlotSoundsEnabled = s.paymentSlotSoundsEnabled();
            this.usePlayerSkin = s.usePlayerSkin();
            this.playerSkinName = s.playerSkinName();
        }

        public boolean npcEnabled() { return npcEnabled; }
        public Draft setNpcEnabled(boolean v) { this.npcEnabled = v; return this; }
        public Draft toggleNpcEnabled() { this.npcEnabled = !this.npcEnabled; return this; }

        public String npcName() { return npcName; }
        public Draft setNpcName(String name) { this.npcName = sanitizeNpcName(name); return this; }

        public VillagerVisualProfession profession() { return profession; }
        public Draft setProfession(VillagerVisualProfession p) { this.profession = p == null ? VillagerVisualProfession.NONE : p; return this; }
        public Draft cycleProfession() {
            this.profession = (this.profession == null ? VillagerVisualProfession.NONE : this.profession).next();
            return this;
        }

        public boolean purchaseParticlesEnabled() { return purchaseParticlesEnabled; }
        public Draft setPurchaseParticlesEnabled(boolean v) { this.purchaseParticlesEnabled = v; return this; }

        public boolean purchaseSoundsEnabled() { return purchaseSoundsEnabled; }
        public Draft setPurchaseSoundsEnabled(boolean v) { this.purchaseSoundsEnabled = v; return this; }

        public boolean paymentSlotSoundsEnabled() { return paymentSlotSoundsEnabled; }
        public Draft setPaymentSlotSoundsEnabled(boolean v) { this.paymentSlotSoundsEnabled = v; return this; }

        public boolean usePlayerSkin() { return usePlayerSkin; }
        public Draft setUsePlayerSkin(boolean v) { this.usePlayerSkin = v; return this; }

        public String playerSkinName() { return playerSkinName; }
        public Draft setPlayerSkinName(String name) { this.playerSkinName = sanitizePlayerSkinName(name); return this; }

        public VillagerSettings toSettings() {
            return new VillagerSettings(npcEnabled, npcName, profession, purchaseParticlesEnabled, purchaseSoundsEnabled, paymentSlotSoundsEnabled, usePlayerSkin, playerSkinName);
        }
    }
}
