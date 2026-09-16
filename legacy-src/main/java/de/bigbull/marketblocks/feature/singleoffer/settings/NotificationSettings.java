package de.bigbull.marketblocks.feature.singleoffer.settings;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Settings for the Notifications tab: toggles for when to notify the owner/co-owners.
 */
public record NotificationSettings(
        boolean notifyOnPurchase,
        boolean notifyOnOutOfStock,
        boolean notifyOnOutputFull,
        boolean notifyCoOwners
) {
    private static final String KEY_NOTIFY_ON_PURCHASE = "NotifyOnPurchase";
    private static final String KEY_NOTIFY_ON_OUT_OF_STOCK = "NotifyOnOutOfStock";
    private static final String KEY_NOTIFY_ON_OUTPUT_FULL = "NotifyOnOutputFull";
    private static final String KEY_NOTIFY_CO_OWNERS = "NotifyCoOwners";

    public static final NotificationSettings DEFAULT = new NotificationSettings(true, true, true, false);

    public static final StreamCodec<ByteBuf, NotificationSettings> STREAM_CODEC = StreamCodec.of(
            (buf, settings) -> {
                ByteBufCodecs.BOOL.encode(buf, settings.notifyOnPurchase());
                ByteBufCodecs.BOOL.encode(buf, settings.notifyOnOutOfStock());
                ByteBufCodecs.BOOL.encode(buf, settings.notifyOnOutputFull());
                ByteBufCodecs.BOOL.encode(buf, settings.notifyCoOwners());
            },
            buf -> new NotificationSettings(
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf)
            )
    );

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(KEY_NOTIFY_ON_PURCHASE, notifyOnPurchase);
        tag.putBoolean(KEY_NOTIFY_ON_OUT_OF_STOCK, notifyOnOutOfStock);
        tag.putBoolean(KEY_NOTIFY_ON_OUTPUT_FULL, notifyOnOutputFull);
        tag.putBoolean(KEY_NOTIFY_CO_OWNERS, notifyCoOwners);
        return tag;
    }

    public static NotificationSettings load(CompoundTag tag) {
        if (tag == null || tag.isEmpty()) return DEFAULT;
        return new NotificationSettings(
                tag.contains(KEY_NOTIFY_ON_PURCHASE) ? tag.getBoolean(KEY_NOTIFY_ON_PURCHASE) : DEFAULT.notifyOnPurchase(),
                tag.contains(KEY_NOTIFY_ON_OUT_OF_STOCK) ? tag.getBoolean(KEY_NOTIFY_ON_OUT_OF_STOCK) : DEFAULT.notifyOnOutOfStock(),
                tag.contains(KEY_NOTIFY_ON_OUTPUT_FULL) ? tag.getBoolean(KEY_NOTIFY_ON_OUTPUT_FULL) : DEFAULT.notifyOnOutputFull(),
                tag.contains(KEY_NOTIFY_CO_OWNERS) ? tag.getBoolean(KEY_NOTIFY_CO_OWNERS) : DEFAULT.notifyCoOwners()
        );
    }

    /**
     * Mutable draft for the Notifications settings tab in the GUI.
     */
    public static final class Draft {
        private boolean notifyOnPurchase;
        private boolean notifyOnOutOfStock;
        private boolean notifyOnOutputFull;
        private boolean notifyCoOwners;

        public Draft(NotificationSettings settings) {
            NotificationSettings s = settings == null ? DEFAULT : settings;
            this.notifyOnPurchase = s.notifyOnPurchase();
            this.notifyOnOutOfStock = s.notifyOnOutOfStock();
            this.notifyOnOutputFull = s.notifyOnOutputFull();
            this.notifyCoOwners = s.notifyCoOwners();
        }

        public boolean notifyOnPurchase() { return notifyOnPurchase; }
        public Draft setNotifyOnPurchase(boolean v) { this.notifyOnPurchase = v; return this; }

        public boolean notifyOnOutOfStock() { return notifyOnOutOfStock; }
        public Draft setNotifyOnOutOfStock(boolean v) { this.notifyOnOutOfStock = v; return this; }

        public boolean notifyOnOutputFull() { return notifyOnOutputFull; }
        public Draft setNotifyOnOutputFull(boolean v) { this.notifyOnOutputFull = v; return this; }

        public boolean notifyCoOwners() { return notifyCoOwners; }
        public Draft setNotifyCoOwners(boolean v) { this.notifyCoOwners = v; return this; }

        public NotificationSettings toSettings() {
            return new NotificationSettings(notifyOnPurchase, notifyOnOutOfStock, notifyOnOutputFull, notifyCoOwners);
        }
    }
}
