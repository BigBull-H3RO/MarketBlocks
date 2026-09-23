# 🧭 Trader NPCs: Autonomous Customer Guide

**Trader NPCs** are autonomous Wandering Traders that travel the Overworld seeking out player-owned shops. They inspect showcased goods, spend coin budgets, and generate sales for your shop network even when other players are offline.

---

## 🌍 Spawning & Despawning Lifecycles

MarketBlocks uses an anti-grief, multiplayer-fair lifecycle to ensure every player with an active shop receives visitors without overloading the world.

### Spawning Conditions
- **Shop Proximity Check**: A Trader NPC only spawns if an active player is within **64 blocks of an open shop with an active offer**. Cooldowns are never wasted while you are deep underground mining, battling in structures, or exploring oceans.
- **Per-Player Daily Cooldown**: Each player has an independent **24,000-tick cooldown (1 Minecraft day)**. Visitors cannot be "stolen" by nearby neighbors.
- **Spawn Interval & Chance**: Every 60 seconds (1200 ticks), the spawner checks players whose cooldown has elapsed and rolls a **25% chance** to spawn a visitor.
- **Daytime & Surface Ground**: Traders spawn during daylight hours on solid, safe ground (12 to 28 blocks from the player) with clear headroom.
- **Density & Suppression Limits**: A default maximum of **4 traders per dimension** can exist simultaneously. If an active trader is already within 80 blocks of a player, no second trader will arrive.

### Despawning Conditions
A Trader NPC remains in the world until one of the following occurs:
- **Shopping Tour Complete**: Each trader rolls a goal to visit between **1 and 5 distinct shops** (`maxShopsPerVisit`). Once their tour concludes, they depart.
- **Budget Exhaustion**: If a trader runs out of money, they finish shopping and leave immediately.
- **Time Limit**: Traders have a maximum lifespan of **40 minutes** (48,000 ticks, identical to vanilla Wandering Traders).
- **Departure**: When departing, the trader calmly walks away and despawns out of player sight.

---

## 👑 Social Ranks & Buying Power

Every Trader NPC rolls a social rank upon spawning. This determines their coin budget, tolerance for profit markups, and purchase volume:

| Rank | Spawn Chance | Budget (Coins) | Purchase Volume | Pricing Tolerance & Standards |
|---|:---:|:---:|:---:|---|
| **Citizen** | 70% | 32 – 128 | **1 unit** | Accepts up to **15% markup** over base value. Focuses on everyday basics. |
| **Wealthy** | 25% | 256 – 1024 | **1 – 3 units** | Accepts up to **5% markup**. Higher budgets for mid-to-high tier goods. |
| **Noble** | 5% | 1024 – 8192 | **1 – 5 units (Bulk)** | Buys **strictly at or below** base value. Completely ignores items valued under 5 coins. |

> [!NOTE]
> The exact quantity purchased depends on the trader's remaining budget: a Wealthy or Noble customer will buy multiple units if their allowed budget can cover the total cost.

---

## 🎯 Trader Interests & Shop Categories

When configuring a [SingleOfferShop](SingleOfferShop-Settings), you can assign a **Shop Category**. This setting directly controls which Trader NPCs spend their full budget at your shop!

Each visiting trader rolls one of 5 **Interest Categories**:

| Interest Category | Preferred Shop Categories |
|---|---|
| **Farmer** | Food & Potions, Blocks, Misc |
| **Alchemist** | Food & Potions, Valuables |
| **Blacksmith** | Weapons & Armor, Tools, Blocks |
| **Valuables** | Valuables |
| **General** | All categories |

### The Budget Impact:
- **Matching Category**: The trader unlocks **100% of their budget** for purchases at that shop.
- **Non-Matching Category (or `None`)**: The trader restricts spending to **20% of their budget** (`allowedBudget = budget * 0.20`), limiting themselves to small convenience purchases.
- **Noble Item Filtering**: Regardless of category, Nobles refuse to purchase ordinary building blocks or basic tools, focusing solely on equipment, potions, valuables, or high-tier items.

---

## 💬 Inspecting & Interacting with Traders

You can right-click any visiting Trader NPC at any time to inspect them:

- **Rank & Interest Display**: The trader prints their profile to your chat (e.g. `[Citizen - Farmer]`), allowing you to immediately see what goods they are shopping for.
- **Contextual Dialogue**: The trader will share ambient remarks reflecting their current task — whether searching for nearby storefronts, browsing, or expressing satisfaction after a purchase.
- **Interaction Cooldown**: A built-in 1.5-second anti-spam cooldown prevents chat clutter.

---

## 🛍️ Counter Reach & Shopping Behavior

Traders navigate storefronts with lifelike behaviors:

- **Theken-Toleranz ("Smart Reach")**: Build realistic counters, slabs, or display fences! If the block in front of the shop is occupied by a table or counter, the trader stands comfortably on the customer side (up to 2.5 blocks away) and trades across the counter (up to 3.5 blocks total reach).
- **Successful Trade**: The trader plays an affirmation sound (`WANDERING_TRADER_YES`), emits green villager particles, and visibly inspects the purchased item before continuing their tour.
- **Transaction History**: Every purchase is recorded in the shop's transaction log (e.g. *"Jonathan (Noble) bought 2x Diamond"*).
- **Window Shopping & Rejection**: If a shop is out of stock, closed, or overpriced, the trader browses the counter briefly, shakes their head with a refusal sound (`WANDERING_TRADER_NO`), marks the shop as visited, and moves on.

---

## ⚙️ Server Economy & Configuration

All Trader NPC mechanics can be tuned in `config/marketblocks/trader/`:

- **`trader.toml`**: Configure spawn cooldowns, detection radius, dimension limits, rank budgets, and feature toggles.
- **`trader_item_values.json`**: Define baseline coin values for vanilla or custom modded items. Items not explicitly listed are automatically calculated from their crafting, smelting, or stonecutting recipes (+10% crafting step bonus).
- **`trader_blacklist.json`**: Prevent unwanted items (dirt, junk, joke items) from ever being purchased by NPCs.
- **`trader_names.json`**: Customize the pool of names assigned to visiting merchants.
- **Dynamic Market Saturation**: Every unit sold to an NPC incrementally increases market saturation for that item. Heavily saturated goods temporarily yield lower payouts from NPCs until demand decays back to baseline over time.

> 🔄 **Hot-Reload**: Run **`/mb admin reload`** to apply changes to JSON value and blacklist tables immediately without restarting the server.

---

## 🤫 Rumors & Mysteries

> [!TIP]
> **A Word of Caution to Impatient Shopkeepers**  
> Wandering merchants take pride in peaceful trade. However, local rumors say that repeatedly provoking, crowding, or harassing a merchant in rapid succession might test their patience. Should a merchant feel threatened, they may unsheathe a weapon to defend their dignity! Server administrators can configure this feature via `[EasterEggs] rageModeEnabled` in `trader.toml`.
