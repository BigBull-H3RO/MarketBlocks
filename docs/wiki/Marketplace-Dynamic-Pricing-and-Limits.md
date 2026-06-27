# Marketplace: Dynamic Pricing & Limits

The Marketplace in MarketBlocks offers a highly sophisticated, server-authoritative limits and dynamic pricing engine designed to simulate a living economy.

## Limits Overview

Each Marketplace offer can combine several independent limiting mechanisms simultaneously to control inflation and prevent hoarding:

- **Daily Limit**: Restricts the maximum number of times an offer can be purchased within a real-time 24-hour cycle.
- **Stock Limit**: Establishes a maximum inventory pool for an offer, simulating a finite stock of goods.
- **Restock Interval**: Refills the stock limit automatically after a defined interval of seconds passes.

## Daily Limit: Global vs Per Player

The scope of daily limits is governed by the server configuration setting in `marketblocks-common.toml`:

- `marketplaceGlobalDailyLimit`

### Behavior Modes:

- **Enabled (`true`)**: All players on the server share a single global daily purchase pool. For example, if a rare item has a daily limit of 5, once 5 total purchases are made across the server, no one else can buy it until the daily reset.
- **Disabled (`false` - Default)**: Each player maintains an individual daily counter. If an offer has a daily limit of 5, Player A can buy 5, and Player B can still buy 5.

> **Tip for Admins:** If a player gets stuck or you need to reset their daily limits for testing or events, use the command `/marketblocks admin resetlimits <player>`.

## Demand Pricing

Demand Pricing dynamically adjusts the effective payment cost of an offer based on player buying activity. When an item is heavily purchased, its price increases; when it sits unbought, its price naturally decays over time.

### Key Parameters:

- **Enabled (`enabled`)**: Toggles whether demand pricing is active for the specific offer.
- **Base Multiplier (`base_multiplier`)**: The starting price multiplier (typically `1.0`).
- **Demand Step (`demand_step`)**: The amount by which the price multiplier increases upon each successful purchase (e.g., `0.05` adds +5% cost per buy).
- **Lower Bound (`min_multiplier`)**: The absolute minimum multiplier the price can decay to (e.g., `0.5` for a 50% price floor).
- **Upper Bound (`max_multiplier`)**: The absolute maximum multiplier the price can escalate to (e.g., `3.0` for a 300% price ceiling).

### Mathematical Calculation:

When a player buys an item, the required payment stack counts are multiplied by the current `priceMultiplier`. The resulting required item count is always rounded up to ensure that at least 1 item is required for payment (unless the base price was 0).

## Runtime Upkeep & Decay

The server continuously processes runtime state updates in the background (`MarketplaceManager.tick()`):

- **Daily Resets**: At the turn of a new day, daily purchase counters are reset to zero.
- **Restock Cycles**: When `restock_seconds` elapse, the `stockRemaining` is replenished up to the maximum stock limit.
- **Demand Decay**: Every 24 hours without significant purchasing activity causes the demand multiplier to decay backward toward the `base_multiplier`, encouraging players to buy neglected goods.

## Recommendations for Server Operation

- **Scarcity vs Accessibility**: Use global daily limits for ultra-rare items (like elytra or boss spawners) and per-player limits for common commodities (like building blocks or food).
- **Control Price Spikes**: Always define realistic bounds (`min_multiplier` and `max_multiplier`) to prevent essential items from becoming entirely unaffordable or free.
- **Match Server Pace**: Adjust restock intervals based on your server's average concurrency and economic velocity.
