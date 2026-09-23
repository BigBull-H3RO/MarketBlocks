# ⚙️ Configuration Guide

MarketBlocks provides modular, cleanly organized configuration files located inside your instance's `config/marketblocks/` directory.

Server administrators and modpack creators can fine-tune every gameplay mechanic, visual default, and economic parameter without touching code.

---

## 📁 Directory Structure

```
config/marketblocks/
├── main.toml                         (Global server rules, teleport permissions, map toggles)
├── client.toml                       (Client-side graphics & performance settings)
├── marketplace.toml                  (Central Marketplace broadcasts & shared limit pools)
├── trader/
│   ├── trader.toml                   (Wandering Trader customer spawning, AI limits & dynamic pricing)
│   ├── trader_item_values.json       (Custom item value benchmarks for NPC shoppers)
│   ├── trader_blacklist.json         (Items blacklisted from being purchased by NPCs)
│   └── trader_names.json             (Customer name pool for spawned NPCs)
└── singleoffer/
    ├── general.toml                  (Global shop block rules, explosion protection, limits, tabs)
    ├── tradestand.toml               (Placement defaults for Trade Stands)
    └── marketcrate.toml              (Placement defaults for Market Crates)
```

---

## 1. Global Server Settings (`main.toml`)

Controls server-wide features, integrations, and convenience mechanics:

### `[General Settings]`
- **`allowNonOpTeleport`** (Default: `false`): By default, only operators (OP level 2) can click the `[TP]` button in `/mb search` results to teleport directly to shops. Set to `true` to allow all survival players to use the teleport shortcut.
- **`giveTradeBookOnFirstJoin`** (Default: `true`): Automatically gives players an interactive MarketBlocks guidebook when joining the world or server for the first time.
- **`showShopIdWithName`** (Default: `true`): When `true`, displays the generated shop ID alongside custom shop names in chat and listings (e.g. `Diamond Palace (#A1F2)`). When `false`, displays only the custom name.

### `[Integrations]`
- **`enableJourneyMapCompat`** (Default: `true`): Enables real-time shop waypoint registration and map markers on **JourneyMap**.
- **`enableXaerosCompat`** (Default: `true`): Enables chat waypoint suggestions for **Xaero's Minimap & Worldmap**.

---

## 2. Central Marketplace (`marketplace.toml`)

Configures chat broadcasts and economy scopes for the central Marketplace system:

### `[Notifications]`
- **`buyerChatMessage`** (Default: `true`): Sends a private confirmation message to the purchasing player upon a successful trade in the Marketplace GUI.
- **`broadcastPurchaseToAll`** (Default: `false`): Broadcasts every marketplace purchase publicly to all connected players on the server.

### `[Economy]`
- **`sharedDailyLimits`** (Default: `false`):
  - When `false`, daily purchase limits apply to each player individually (e.g. every player can buy up to 5 diamonds per day).
  - When `true`, daily purchase limits are shared globally across the entire server as a communal pool (e.g. only 5 diamonds total can be purchased server-wide per day).

---

## 3. Player Shop Rules (`singleoffer/general.toml`)

Controls rules, limits, and server-side grief protections for **Trade Stands** and **Market Crates**:

### `[ShopMechanics]`
- **`shopBlastResistance`** (Default: `3600000.0`): Explosion resistance of shop blocks. The default is bedrock-level, completely preventing Creeper, TNT, or Wither griefing.
- **`maxShopsPerPlayer`** (Default: `10`): Maximum number of active shop blocks an individual player can place in Survival mode. Set to `-1` for unlimited placement.
- **`maxCoOwnersPerShop`** (Default: `10`): Maximum number of trusted co-owners a primary owner can register per shop.
- **`enableChestExtension`** (Default: `false`): Enables adjacent chest auto-pull/push. When disabled, the I/O tab is completely hidden from the shop interface.
- **`chestIoInterval`** (Default: `20` ticks / 1 second): Transfer frequency between adjacent storage chests and shop inventories.
- **`enableOutputWarning`** (Default: `true`): Displays a warning icon in the shop GUI when the earnings inventory is almost full.
- **`outputWarningPercent`** (Default: `90`%): Threshold percentage at which the earnings storage is considered almost full.
- **`notificationCooldownTicks`** (Default: `1200` ticks / 1 minute): Delay between repetitive out-of-stock or earnings-full notifications sent to owners.

### `[Notifications]`
- **`buyerChatMessage`** (Default: `true`): Sends a private chat confirmation to customers upon purchasing from a SingleOfferShop.
- **`broadcastPurchaseToAll`** (Default: `false`): Broadcasts SingleOfferShop purchases to the entire server.

### `[Tabs]`
Server administrators can globally disable specific configuration tabs across all shops on the server:
- **`villager`** (Default: `true`): Visual NPC clerk tab.
- **`visuals`** (Default: `true`): Floating item and crate layout showcase tab.
- **`notifications`** (Default: `true`): Notification preferences tab.

---

## 4. Block Defaults (`tradestand.toml` & `marketcrate.toml`)

When a player places a fresh shop block, its initial settings are inherited from these files. If a tab is disabled via `general.toml`, the settings defined here become the permanent server-wide rule:

### General & Villager Settings
| Setting | Trade Stand Default | Market Crate Default | Description |
|---|:---:|:---:|---|
| **`[General] emitRedstone`** | `false` | `false` | Emits a redstone pulse when a sale occurs. |
| **`[General] purchaseSound`** | `true` | `true` | Plays a ding sound upon successful purchase. |
| **`[General] isClosed`** | `false` | `false` | Whether newly placed shops start in a paused/closed state. |
| **`[Villager] enabled`** | `false` | `false` | Shows an animated NPC clerk behind the shop. |
| **`[Villager] profession`** | `NONE` | `NONE` | Default profession costume (`FARMER`, `LIBRARIAN`, etc.). |
| **`[Villager] purchaseParticles`** | `true` | `true` | Spawns green villager particles on trade. |
| **`[Villager] purchaseSounds`** | `true` | `true` | Plays villager affirmation sound on trade. |
| **`[Villager] paymentSlotSounds`**| `false` | `false` | Plays villager ambient sounds when items are placed in payment slots. |
| **`[Villager] usePlayerSkin`** | `false` | `false` | Renders the clerk using the shop owner's player skin. |

### Visual Showcase Settings
| Setting | Trade Stand Default | Market Crate Default | Description |
|---|:---:|:---:|---|
| **`visible`** | `true` | `true` | Renders the 3D item showcase. |
| **`fullbright`** | `false` | `false` | Renders showcase items at maximum brightness regardless of light level. |
| **`scale`** | `1.0` | `1.0` | Size scale of rendered items. |
| **`speed`** | `0.75` | — | Rotation speed (Trade Stand floating item). |
| **`heightOffset`** | `0.0` | — | Vertical offset (Trade Stand floating item). |
| **`bobbing`** | `false` | — | Gentle up/down bobbing motion (Trade Stand). |
| **`itemCount`** | — | `1` | Number of items rendered inside the crate (1–96). |
| **`layoutMode`** | — | `STACKED` | Arrangement style: `STACKED`, `GRID`, `CIRCLE`, or `RANDOM`. |
| **`dynamicFill`** | — | `false` | Dynamically adjusts rendered item count based on remaining stock. |
| **`rotation`** | — | `0.0` | Base orientation angle inside the crate (0–360°). |
| **`spacingXZ` / `spacingY`** | — | `0.0` | Horizontal and vertical item spacing in the crate. |
| **`chaosRotation`** | — | `0.0` | Random angular jitter applied to rendered items. |

### Chest Automation & Owner Notifications
- **`[IO]`**: Defines default automated hopper/chest behavior (`allowIo = false`, `autoIo = false`, `redstoneControl = IGNORED`).
- **`[Notifications]`**: Defines default alerts for owners (`notifyPurchase = true`, `notifyOutOfStock = true`, `notifyOutputFull = true`, `notifyCoOwners = false`).

---

## 5. Trader NPC Configuration (`trader/`)

The autonomous Wandering Trader customer system is configured via `trader.toml` and three hot-reloadable JSON data files inside `config/marketblocks/trader/`:

### A. General Rules & Spawning (`trader.toml`)

#### `[Spawning]`
- **`enabled`** (Default: `true`): Master toggle for autonomous customer spawning.
- **`spawnCooldownTicks`** (Default: `24000` / 1 Minecraft day): Cooldown in ticks per player before another customer can visit their shops.
- **`spawnChancePercent`** (Default: `25`%): Chance rolled every 60 seconds (1200 ticks) to spawn a visitor once a player's cooldown is complete.
- **`preferDaytime`** (Default: `true`): Restricts trader visits to daylight hours (tick 0 to 12000).
- **`maxPerDimension`** (Default: `4`): Maximum number of simultaneous active Trader NPCs per dimension.
- **`shopDetectionRadius`** (Default: `64` blocks): Radius checked around an active player for open shops with offers before attempting a spawn.

#### `[Behavior]`
- **`despawnTicks`** (Default: `48000` / 40 minutes): Maximum lifespan of a Trader NPC before departing (matches vanilla Wandering Traders).
- **`maxShopsPerVisit`** (Default: `5`): Maximum number of distinct shops a trader will visit during their shopping trip before despawning.
- **`minBudget`** (Default: `16`) & **`maxBudget`** (Default: `128`): Global clamp boundaries for generated trader coin budgets.
- **`allowAdminShops`** (Default: `true`): Whether NPC shoppers are permitted to purchase from infinite Admin Shops.
- **`namesEnabled`** (Default: `true`): Assigns randomized names from `trader_names.json` to visiting NPCs.

#### `[DynamicPricing]`
- **`enabled`** (Default: `true`): Enables NPC market saturation dynamics (supply and demand).
- **`decayRatePerHour`** (Default: `0.05` / 5% per hour): Rate at which market saturation decays back to neutral over time.
- **`minMultiplier`** (Default: `0.25` / 25% floor): Lowest price multiplier when an item is heavily saturated by NPC sales.
- **`maxMultiplier`** (Default: `1.5` / 150% ceiling): Highest price multiplier when an item is in high demand.
- **`saturationPerUnit`** (Default: `0.005`): Saturation added per item unit purchased by an NPC (200 units = 1.0 saturation).
- **`craftingBonusPerStep`** (Default: `0.10` / +10% per step): Price bonus added per crafting, smelting, or stonecutting step for processed goods.

#### `[EasterEggs]`
- **`rageModeEnabled`** (Default: `true`): Enables the Easter Egg where provoked traders defend themselves.

---

### B. Custom Item Values (`trader_item_values.json`)
Defines baseline coin values used by NPC customers to evaluate item worth and compare prices against their budgets:

```json
{
  "minecraft:netherite_ingot": 100.0,
  "minecraft:diamond": 15.0,
  "minecraft:emerald": 10.0,
  "minecraft:gold_ingot": 5.0,
  "minecraft:iron_ingot": 2.0,
  "minecraft:copper_ingot": 0.5,
  "minecraft:totem_of_undying": 150.0,
  "minecraft:golden_apple": 15.0
}
```

> 💡 **Automatic Recipe Calculation**: Items not listed in this file have their values computed automatically by traversing crafting, cooking, and stonecutting recipes, with an added crafting bonus (`craftingBonusPerStep`). Explicit entries in this JSON file always take precedence.

---

### C. Blacklist (`trader_blacklist.json`)
A simple JSON array of item IDs that NPC shoppers will **never** buy under any circumstance:

```json
[
  "minecraft:bedrock",
  "minecraft:barrier",
  "minecraft:dirt",
  "minecraft:cobblestone",
  "minecraft:poisonous_potato"
]
```
Use this to prevent players from setting up joke shops (e.g. selling 1 Dirt for 64 Emeralds) to farm customer visits.

---

### D. Customer Names Pool (`trader_names.json`)
A JSON array of names assigned to visiting Trader NPCs:

```json
[
  "Gilbert",
  "Martha",
  "Ezra",
  "Finn",
  "Iris",
  "Hugo",
  "Clara",
  "Jasper"
]
```
When a trader makes a purchase, their name and rank are recorded in the shop's transaction history (e.g. *"Jasper (Wealthy) bought 2x Diamond"*).

> 🔄 **Hot-Reload**: Run **`/mb admin reload`** to apply changes to `trader_item_values.json`, `trader_blacklist.json`, and `trader_names.json` immediately without restarting the server!

---

## 6. Client Graphics & Performance (`client.toml`)

Client-side visual options located on individual player machines:

- **`enableShopItemRendering`** (Default: `true`): Performance master switch for low-end PCs. Setting this to `false` disables all floating 3D items, crate contents, and front recipe showcases to maximize FPS in dense shopping districts.

---

## 7. Central Marketplace Data (`<world>/marketblocks/`)

World-specific data saved inside the active world save folder:

- **`marketplace.json`**: Stores all category tabs, offer definitions, price curves, stock caps, and daily limits.
- **`marketplace.json.bak`**: Automatically maintained backup file updated on every save.
- **Live Reload**: Run **`/mb admin reload`** to reload changes made to `marketplace.json` on the fly.
