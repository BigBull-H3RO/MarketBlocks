# Marketplace: Dynamic Pricing & Limits

The Marketplace in MarketBlocks offers a server-authoritative limits and dynamic pricing engine designed to simulate a living, balanced economy.

---

## Limits Overview

Each Marketplace offer can combine several independent limiting mechanisms to prevent inflation, hoarding, or market flooding:

- **Daily Limit**: Restricts the maximum number of times an offer can be purchased within a real-time 24-hour cycle.
- **Stock Limit**: Establishes a finite inventory pool for an offer, simulating physical scarcity.
- **Restock Interval**: Refills the stock limit automatically after a defined interval of seconds has elapsed.

---

## Daily Limit: Global vs Per Player

The scope of daily limits is governed by the server configuration in `config/marketblocks/marketplace.toml`:

```toml
[Economy]
# If true, daily purchase limits are shared globally across the entire server (server pool).
# If false, daily limits apply per individual player (Default: false).
sharedDailyLimits = false
```

### Modes:
- **Per-Player Limits (`sharedDailyLimits = false` - Default)**:  
  Each player has an individual daily counter. If an offer has a daily limit of 5, Player A can buy 5, and Player B can also buy 5.
- **Global Server Limits (`sharedDailyLimits = true`)**:  
  All players on the server draw from a single shared daily quota. If an offer has a daily limit of 5, once 5 total purchases occur across the server, the item is sold out for everyone until the next daily reset.

> 💡 **Admin Tip:** To reset a specific player's daily limits for events or testing, use:  
> `/marketblocks admin resetlimits <player>` (or `/mb admin resetlimits <player>`).

---

## Dynamic Demand Pricing

Demand Pricing dynamically adjusts the payment price of an offer based on actual purchasing activity. Heavily bought items become progressively more expensive, while neglected items gradually cool down toward their base price.

### Key Parameters (Configurable in GUI or JSON):
- **`enabled`**: Toggles whether demand pricing is active for the specific offer.
- **`base_multiplier`**: The baseline price multiplier (typically `1.0`).
- **`demand_step`**: The increase added to the price multiplier upon each successful purchase (e.g., `0.05` adds +5% cost per buy).
- **`min_multiplier`**: The lowest possible multiplier the price can decay to (e.g., `0.5` sets a 50% price floor).
- **`max_multiplier`**: The highest possible multiplier the price can escalate to (e.g., `3.0` sets a 300% price ceiling).

### Price Calculation:
Whenever a player makes a purchase:
1. The base payment item count is multiplied by the current `priceMultiplier`.
2. The resulting count is rounded up (`Math.ceil`) to ensure at least 1 payment item is required (minimum 1).
3. The multiplier increments by `demand_step`, capped at `max_multiplier`.

---

## Runtime State & Upkeep

The server continuously evaluates active limits and pricing in the background:
- **Daily Resets**: At 00:00 (or after 24 hours of elapsed real time), daily purchase counters reset to zero.
- **Restock Cycles**: When `restock_seconds` elapse, `stockRemaining` is replenished back up to the configured stock limit.
- **Demand Cooling**: Over time without purchases, the demand multiplier slowly cools down backward toward `base_multiplier`.

---

## Best Practices for Server Admins

- **Targeted Scarcity**: Use global daily limits for rare items (such as Elytra, Nether Stars, or decorative trophies) and per-player limits for essential consumables.
- **Prevent Runaway Inflation**: Always set reasonable bounds (`min_multiplier` and `max_multiplier`) to prevent goods from becoming completely unobtainable or free.
- **Synchronized Economy**: You can view the top performing Marketplace items at any time via `/mb stats marketplace`.
