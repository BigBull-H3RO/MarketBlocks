package de.bigbull.marketblocks.feature.singleoffer.settings;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.core.UUIDUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Settings for the Access tab: shop ownership, access control, and admin shop toggle.
 */
public record AccessSettings(
        boolean adminShopEnabled,
        @Nullable UUID ownerId,
        String ownerName,
        Map<UUID, String> additionalOwners,
        AccessMode accessMode,
        Map<UUID, String> accessList
) {
    private static final String KEY_ADMIN_SHOP = "AdminShopEnabled";
    private static final String KEY_OWNER_ID = "OwnerId";
    private static final String KEY_OWNER_NAME = "OwnerName";
    private static final String KEY_ADDITIONAL_OWNERS = "AdditionalOwners";
    private static final String KEY_ADDITIONAL_OWNER_ID = "Id";
    private static final String KEY_ADDITIONAL_OWNER_NAME = "Name";
    private static final String KEY_ACCESS_MODE = "AccessMode";
    private static final String KEY_ACCESS_LIST = "AccessList";
    private static final String KEY_ACCESS_PLAYER_ID = "Id";
    private static final String KEY_ACCESS_PLAYER_NAME = "Name";

    public static final AccessSettings DEFAULT = new AccessSettings(false, null, "", Map.of(), AccessMode.WHITELIST, Map.of());

    public static final StreamCodec<ByteBuf, AccessSettings> STREAM_CODEC = StreamCodec.of(
            (buf, settings) -> {
                ByteBufCodecs.BOOL.encode(buf, settings.adminShopEnabled());
                ByteBufCodecs.BOOL.encode(buf, settings.ownerId() != null);
                if (settings.ownerId() != null) {
                    UUIDUtil.STREAM_CODEC.encode(buf, settings.ownerId());
                }
                ByteBufCodecs.stringUtf8(36).encode(buf, settings.ownerName());

                buf.writeInt(settings.additionalOwners().size());
                settings.additionalOwners().forEach((id, name) -> {
                    UUIDUtil.STREAM_CODEC.encode(buf, id);
                    ByteBufCodecs.stringUtf8(32).encode(buf, name);
                });

                ByteBufCodecs.stringUtf8(16).encode(buf, settings.accessMode().name());
                buf.writeInt(settings.accessList().size());
                settings.accessList().forEach((id, name) -> {
                    UUIDUtil.STREAM_CODEC.encode(buf, id);
                    ByteBufCodecs.stringUtf8(32).encode(buf, name);
                });
            },
            buf -> {
                boolean adminEnabled = ByteBufCodecs.BOOL.decode(buf);
                boolean hasOwner = ByteBufCodecs.BOOL.decode(buf);
                UUID ownerId = hasOwner ? UUIDUtil.STREAM_CODEC.decode(buf) : null;
                String ownerName = ByteBufCodecs.stringUtf8(36).decode(buf);

                int size = buf.readInt();
                if (size < 0 || size > 100) {
                    throw new IllegalArgumentException("Invalid additionalOwners size: " + size);
                }
                Map<UUID, String> additionalOwners = new HashMap<>();
                for (int i = 0; i < size; i++) {
                    UUID id = UUIDUtil.STREAM_CODEC.decode(buf);
                    String name = ByteBufCodecs.stringUtf8(32).decode(buf);
                    additionalOwners.put(id, name);
                }

                String modeStr = ByteBufCodecs.stringUtf8(16).decode(buf);
                AccessMode accessMode = AccessMode.WHITELIST;
                try {
                    accessMode = AccessMode.valueOf(modeStr);
                } catch (IllegalArgumentException e) {
                    // Fallback to default
                }
                int accessSize = buf.readInt();
                if (accessSize < 0 || accessSize > 100) {
                    throw new IllegalArgumentException("Invalid accessList size: " + accessSize);
                }
                Map<UUID, String> accessList = new HashMap<>();
                for (int i = 0; i < accessSize; i++) {
                    UUID id = UUIDUtil.STREAM_CODEC.decode(buf);
                    String name = ByteBufCodecs.stringUtf8(32).decode(buf);
                    accessList.put(id, name);
                }

                return new AccessSettings(adminEnabled, ownerId, ownerName, additionalOwners, accessMode, accessList);
            }
    );

    public AccessSettings {
        ownerName = ownerName == null ? "" : ownerName;
        additionalOwners = additionalOwners == null ? Map.of() : Map.copyOf(additionalOwners);
        accessMode = accessMode == null ? AccessMode.WHITELIST : accessMode;
        accessList = accessList == null ? Map.of() : Map.copyOf(accessList);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(KEY_ADMIN_SHOP, adminShopEnabled);
        if (ownerId != null) {
            tag.store(KEY_OWNER_ID, UUIDUtil.CODEC, ownerId);
        }
        tag.putString(KEY_OWNER_NAME, ownerName);

        if (!additionalOwners.isEmpty()) {
            ListTag list = new ListTag();
            for (Map.Entry<UUID, String> entry : additionalOwners.entrySet()) {
                CompoundTag ownerTag = new CompoundTag();
                ownerTag.store(KEY_ADDITIONAL_OWNER_ID, UUIDUtil.CODEC, entry.getKey());
                ownerTag.putString(KEY_ADDITIONAL_OWNER_NAME, entry.getValue());
                list.add(ownerTag);
            }
            tag.put(KEY_ADDITIONAL_OWNERS, list);
        }

        tag.putString(KEY_ACCESS_MODE, accessMode.name());
        if (!accessList.isEmpty()) {
            ListTag list = new ListTag();
            for (Map.Entry<UUID, String> entry : accessList.entrySet()) {
                CompoundTag accessTag = new CompoundTag();
                accessTag.store(KEY_ACCESS_PLAYER_ID, UUIDUtil.CODEC, entry.getKey());
                accessTag.putString(KEY_ACCESS_PLAYER_NAME, entry.getValue());
                list.add(accessTag);
            }
            tag.put(KEY_ACCESS_LIST, list);
        }
        return tag;
    }

    public static AccessSettings load(CompoundTag tag) {
        if (tag == null) return DEFAULT;

        boolean adminShopEnabled = tag.getBooleanOr(KEY_ADMIN_SHOP, false);
        UUID ownerId = tag.read(KEY_OWNER_ID, UUIDUtil.CODEC).orElse(null);
        String ownerName = tag.getStringOr(KEY_OWNER_NAME, "");

        Map<UUID, String> additionalOwners = new HashMap<>();
        ListTag ownerList = tag.getListOrEmpty(KEY_ADDITIONAL_OWNERS);
        for (int i = 0; i < ownerList.size(); i++) {
            CompoundTag ownerTag = ownerList.getCompoundOrEmpty(i);
            ownerTag.read(KEY_ADDITIONAL_OWNER_ID, UUIDUtil.CODEC).ifPresent(uuid -> {
                additionalOwners.put(uuid, ownerTag.getStringOr(KEY_ADDITIONAL_OWNER_NAME, ""));
            });
        }

        AccessMode accessMode = AccessMode.WHITELIST;
        String modeStr = tag.getStringOr(KEY_ACCESS_MODE, "");
        if (!modeStr.isEmpty()) {
            try {
                accessMode = AccessMode.valueOf(modeStr);
            } catch (IllegalArgumentException ignored) {
            }
        }

        Map<UUID, String> accessList = new HashMap<>();
        ListTag accessListTag = tag.getListOrEmpty(KEY_ACCESS_LIST);
        for (int i = 0; i < accessListTag.size(); i++) {
            CompoundTag accessTag = accessListTag.getCompoundOrEmpty(i);
            accessTag.read(KEY_ACCESS_PLAYER_ID, UUIDUtil.CODEC).ifPresent(uuid -> {
                accessList.put(uuid, accessTag.getStringOr(KEY_ACCESS_PLAYER_NAME, ""));
            });
        }

        return new AccessSettings(adminShopEnabled, ownerId, ownerName, additionalOwners, accessMode, accessList);
    }

    public AccessSettings withAdminShopEnabled(boolean enabled) {
        return new AccessSettings(enabled, ownerId, ownerName, additionalOwners, accessMode, accessList);
    }

    public AccessSettings withOwner(@Nullable UUID id, String name) {
        return new AccessSettings(adminShopEnabled, id, name, additionalOwners, accessMode, accessList);
    }

    public AccessSettings withAdditionalOwners(Map<UUID, String> owners) {
        return new AccessSettings(adminShopEnabled, ownerId, ownerName, owners, accessMode, accessList);
    }

    /**
     * Mutable draft for the Access settings tab in the GUI.
     */
    public static final class Draft {
        private boolean adminShopEnabled;
        private UUID ownerId;
        private String ownerName;
        private final Map<UUID, String> additionalOwners = new HashMap<>();
        private AccessMode accessMode;
        private final Map<UUID, String> accessList = new HashMap<>();

        public Draft(AccessSettings settings) {
            AccessSettings s = settings == null ? DEFAULT : settings;
            this.adminShopEnabled = s.adminShopEnabled();
            this.ownerId = s.ownerId();
            this.ownerName = s.ownerName();
            this.additionalOwners.putAll(s.additionalOwners());
            this.accessMode = s.accessMode();
            this.accessList.putAll(s.accessList());
        }

        public boolean adminShopEnabled() { return adminShopEnabled; }
        public Draft setAdminShopEnabled(boolean v) { this.adminShopEnabled = v; return this; }

        public UUID ownerId() { return ownerId; }
        public String ownerName() { return ownerName; }

        public Map<UUID, String> additionalOwners() { return Collections.unmodifiableMap(additionalOwners); }

        public Draft setAdditionalOwners(Map<UUID, String> owners) {
            this.additionalOwners.clear();
            if (owners != null) {
                this.additionalOwners.putAll(owners);
            }
            return this;
        }

        public AccessMode accessMode() { return accessMode; }
        public Draft setAccessMode(AccessMode mode) { this.accessMode = mode == null ? AccessMode.WHITELIST : mode; return this; }

        public Map<UUID, String> accessList() { return Collections.unmodifiableMap(accessList); }

        public Draft setAccessList(Map<UUID, String> list) {
            this.accessList.clear();
            if (list != null) {
                this.accessList.putAll(list);
            }
            return this;
        }

        public AccessSettings toSettings() {
            return new AccessSettings(adminShopEnabled, ownerId, ownerName, additionalOwners, accessMode, accessList);
        }
    }
}
