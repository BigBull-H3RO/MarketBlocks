package de.bigbull.marketblocks.core.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class MarketplaceConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue BUYER_CHAT_MESSAGE;
    public static final ModConfigSpec.BooleanValue BROADCAST_PURCHASE_TO_ALL;
    public static final ModConfigSpec.BooleanValue SHARED_DAILY_LIMITS;

    static {
        BUILDER.push("Notifications");
        BUYER_CHAT_MESSAGE = BUILDER
                .comment("Send a private chat confirmation to the player upon a successful purchase (Default: true)")
                .define("buyerChatMessage", true);
        BROADCAST_PURCHASE_TO_ALL = BUILDER
                .comment("Broadcast the purchase message to all online players on the server (Default: false)")
                .define("broadcastPurchaseToAll", false);
        BUILDER.pop();

        BUILDER.push("Economy");
        SHARED_DAILY_LIMITS = BUILDER
                .comment("If true, daily purchase limits are shared globally across the entire server (server pool).",
                        "If false, daily limits apply per individual player (Default: false).")
                .define("sharedDailyLimits", false);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
