# Marketplace: Dynamic Pricing & Limits

The Marketplace in MarketBlocks features a server-authoritative economics engine that combines **scarcity controls** (stock pools & daily limits) with an **algorithmic market temperature model** (supply & demand pricing).

---

## 🛑 Purchase Limits (Scarcity Controls)

Each Marketplace trade offer can combine three independent limiting parameters configured via the in-game editor (Limits modal) or JSON:

| Parameter | Unit | In-Game Default | Description |
|---|:---:|:---:|---|
| **`daily_limit`** | Purchases / Day | *Unlimited* | Maximum purchases allowed within a 24-hour cycle. Resets at midnight. |
| **`stock_limit`** | Items in Pool | *Unlimited* | Maximum available inventory pool before the offer becomes temporarily out of stock. |
| **`restock_seconds`** | Seconds | *Disabled* | Timer interval in seconds. Once elapsed, the available stock is restored back to the `stock_limit`. |

> ℹ️ Any value left blank in the GUI (or set to `<= 0` in JSON) is treated as unlimited / disabled.

---

## 🌐 Daily Limit Scope: Global vs. Per-Player

In `config/marketblocks/marketplace.toml`, administrators configure whether daily limits apply individually or server-wide:

```toml
[Economy]
# false = Limits apply per individual player (Default)
# true  = Limits are shared globally across the entire server
sharedDailyLimits = false
```

> [!TIP]
> Administrators can reset a player's daily purchase counters at any time using:
> ```
> /mb admin resetlimits <player>
> ```

---

## 📈 The Dynamic Demand Pricing Engine

MarketBlocks uses a continuous **Market Temperature Model** (ranging from `-1.0` to `+1.0`) with real-world exponential decay:

```
Cold Market (Discounts)           Neutral Zone               Hot Market (Surges)
      [-1.0  ..  -0.2]          [-0.2  ..  +0.2]              [+0.2  ..  +1.0]
◄───────────────────────────────┼──────────────┼───────────────────────────────►
 Price approaches minMultiplier   Base Price (1.0x)    Price approaches maxMultiplier
```

### 1. Market Zones
- **Neutral Zone (`-0.2` to `+0.2`)**: The market is stable. Offers sell at their standard base price (`1.0x` $\times$ `base_multiplier`).
- **Surge / Hot Zone (`+0.2` to `+1.0`)**: High purchase frequency heats up the market. Prices scale proportionally up toward `max_multiplier`.
- **Discount / Cold Zone (`-1.0` to `-0.2`)**: Long periods without purchases cool down the market. Prices scale downward toward `min_multiplier`.

---

### 2. Volatility Settings (`volatility`)

The volatility setting determines how fast prices heat up on purchases and how quickly they cool down over time:

| Volatility | Heat per Purchase | Real-Time Cooling Half-Life | Recommended Use Case |
|:---:|:---:|:---:|---|
| **`slow`** | `+0.02` | **~7 days** | High-volume staples (Cobblestone, Logs, Iron, Food) |
| **`normal`** | `+0.05` | **~3 days** | Mid-tier commodities, potions, enchanted gear, tools |
| **`fast`** | `+0.10` | **~1 day** | High-value exotics (Netherite, Elytra, Beacon, Totems) |

---

### 3. Multiplier Parameters

Configured via the **Pricing Editor** modal or JSON:

- **`enabled`**: Toggles dynamic pricing on or off for this specific offer.
- **`base_multiplier`**: Baseline price multiplier (default: `1.0`).
- **`min_multiplier`**: Price floor (minimum factor, default: `0.25` = 25% minimum cost).
- **`max_multiplier`**: Price ceiling (maximum factor, default: `4.0` = 400% maximum cost).

*(Prices are always rounded up to whole items, with a minimum of 1 item).*

---

## ⚙️ Server Runtime Upkeep & Synchronization

The server evaluates active limits and pricing in a lightweight 1-second heartbeat loop (every 20 ticks):

1. **Restock Timers**: Depleted stock refills back up to `stock_limit` once `restock_seconds` have elapsed.
2. **Midnight Reset**: Daily purchase records reset automatically at each new game day.
3. **Exponential Cooling**: Real-world elapsed time decays market temperature back toward discounts when items sit unbought.
4. **Live Synchronization**: Any price or stock changes are pushed in real-time to all players currently browsing the Marketplace GUI.


