# Configuration Guide

MarketBlocks provides modular, cleanly organized configuration files located inside your Minecraft instance's `config/marketblocks/` directory.

Server administrators and modpack creators can fine-tune every aspect of the mod without needing to code.

---

## 📁 Directory Structure

```
config/marketblocks/
├── main.toml                         (General settings, teleport rules, map toggles)
├── client.toml                       (Client-side rendering & performance options)
├── marketplace.toml                  (Central marketplace rules & chat broadcasts)
├── trader/
│   ├── trader.toml                   (Wandering Trader customer NPC rules & limits)
│   ├── trader_item_values.json       (Custom item value benchmarks for NPC shoppers)
│   ├── trader_blacklist.json         (Items blacklisted from being purchased by NPCs)
│   └── trader_names.json             (Customer name pool for spawned NPCs)
└── singleoffer/
    ├── general.toml                  (Global shop block rules, explosion protection, limits)
    ├── tradestand.toml               (Default settings for newly placed Trade Stands)
    └── marketcrate.toml              (Default settings for newly placed Market Crates)
```

---

## 1. Global Server Settings (`main.toml`)

- **`allowNonOpTeleport`** (Default: `false`): By default, only operators with level 2 can click the `[TP]` button in `/mb search` results to teleport to shops. Set to `true` to allow all survival players to teleport directly to player shops.
- **`enableFirstJoinBook`** (Default: `true`): Whether players receive a MarketBlocks guidebook upon joining the world for the first time.
- **`enableJourneyMap` / `enableXaeros`** (Default: `true`): Toggles real-time shop markers on JourneyMap and Xaero's Minimap/Worldmap.

---

## 2. Player Shop Rules (`singleoffer/general.toml`)

Controls rules and protections for player-owned **Trade Stands** and **Market Crates**:

- **`shopBlastResistance`** (Default: `3600000.0`): Explosion resistance of shop blocks. The default is bedrock-level, completely preventing Creeper, TNT, or Wither griefing.
- **`maxShopsPerPlayerSurvival`** (Default: `10`): Maximum number of shop blocks an individual player can place in Survival mode. Set to `-1` for unlimited placement.
- **`maxCoOwnersPerShop`** (Default: `10`): Maximum number of trusted co-owners a primary owner can register per shop.
- **`enableChestExtension`** (Default: `false`): Enables adjacent chest auto-pull/push. When disabled, hides the I/O tab from players.
- **`chestIoInterval`** (Default: `20` ticks / 1 second): Transfer speed between adjacent storage chests and shop inventories.
- **`enableOutputWarning`** (Default: `true`) & **`outputWarningPercent`** (Default: `90`%): Displays a visual warning icon when earnings storage is almost full.
- **`notificationCooldownTicks`** (Default: `1200` ticks / 1 minute): Minimum delay between repetitive out-of-stock or output-full alerts.
- **`[Tabs]`**: Server administrators can globally hide specific GUI tabs across all shops on the server:
  - `villager = true` (Animated clerks)
  - `visuals = true` (Floating item showcases)
  - `notifications = true` (Chat alerts)

---

## 3. Block Defaults (`tradestand.toml` & `marketcrate.toml`)

When a player crafts and places a fresh Trade Stand or Market Crate, its initial settings are inherited from these files. Furthermore, if a tab is disabled via `general.toml`, the values defined here become the permanently enforced rule for all shops:

- **General**: Default shop state (`isClosed = false`), redstone pulse (`emitRedstone = false`), XP ding sound (`purchaseXpSound = false`).
- **Villager**: Default clerk state (`enabled = false`), default profession (`NONE`), player skin mode (`usePlayerSkin = false`), trade particles and voice lines.
- **Visuals**: Floating item visibility (`visible = true`), fullbright (`false`), scale (`1.0`), rotation speed (`0.75`), height offset (`0.0`), bobbing animation (`false`).
- **Notifications**: Default alerts for purchases, out of stock, output full, and co-owner broadcasts.

---

## 4. Trader NPC Configuration (`trader/`)

The autonomous Wandering Trader customer system is configured via a combination of `trader.toml` and three flexible JSON data files inside `config/marketblocks/trader/`:

### A. General Rules (`trader.toml`)
- **`[Spawning] enabled`** (Default: `true`): Master toggle for autonomous customer spawning.
- **`[Spawning] spawnCooldownTicks`** (Default: `24000` / 1 Minecraft day): Cooldown in ticks per player before another customer visits their shops.
- **`[Spawning] spawnChancePercent`** (Default: `25`%): Chance every 60 seconds (1200 ticks) to spawn a trader for an eligible player after cooldown.
- **`[Spawning] preferDaytime`** (Default: `true`): Restricts trader visits to daylight hours (tick 0 to 12000).
- **`[Spawning] maxPerDimension`** (Default: `4`): Maximum number of active Trader NPCs allowed simultaneously in the Overworld.
- **`[Spawning] shopDetectionRadius`** (Default: `64` blocks): Radius around an active player checked for open shops with offers before attempting a spawn.
- **`[RageMode] enabled`** (Default: `true`): Enables the Easter Egg where spam-clicked traders become hostile and draw enchanted swords.

### B. Custom Item Values (`trader_item_values.json`)
Defines the reference value (score) for items when evaluated by NPC customers. Traders use these values to compare requested payment prices against their rank budgets (`CITIZEN`, `WEALTHY`, `NOBLE`):
```json
{
  "minecraft:diamond": 15.0,
  "minecraft:emerald": 10.0,
  "minecraft:gold_ingot": 5.0,
  "minecraft:iron_ingot": 2.0,
  "minecraft:netherite_ingot": 100.0,
  "minecraft:elytra": 200.0
}
```
> 💡 **Auto-Calculation**: If an item is not listed here, MarketBlocks automatically computes its value based on crafting ingredients and vanilla tags. Custom entries in this file always take priority.

### C. Blacklist (`trader_blacklist.json`)
A simple JSON array of item IDs that NPC shoppers will **never** buy under any circumstance:
```json
[
  "minecraft:bedrock",
  "minecraft:barrier",
  "minecraft:dirt",
  "minecraft:poisonous_potato"
]
```
Use this to prevent players from setting up joke shops (e.g. selling 1 Dirt for 64 Emeralds) to farm customer visits.

### D. Customer Names Pool (`trader_names.json`)
A JSON array of names used when generating visiting Trader NPCs:
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
When a trader makes a purchase, their assigned name and social rank are recorded in the shop's transaction history (e.g. *"Jasper (Wealthy)"*). Server owners can easily customize this file with themed names, fantasy characters, or community donor names!

> 🔄 **Live Hot-Reload**: Run **`/mb admin reload`** in-game or from server console to reload all three JSON files immediately without restarting the server!

---

## 5. Client Graphics & Performance (`client.toml`)

- **`enableShopItemRendering`** (Default: `true`): Low-end PC master switch. Setting this to `false` disables all floating 3D items, crate contents, and front recipe showcases for maximum client FPS in dense shopping malls.

---

## 6. Central Marketplace Data (`<world>/marketblocks/`)

- **`marketplace.json`**: Stores all category tabs, offer definitions, price curves, stock caps, and daily limits.
- **`marketplace.json.bak`**: Automatically created backup copy updated on every save to ensure zero data loss.
- **Live Reload**: Run `/mb admin reload` to reload changes made to `marketplace.json` without restarting the server.
