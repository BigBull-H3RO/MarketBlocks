<p align="center"><img src=".idea/icon.png" alt="Logo" width="250"></p>

<div align="center">

# MarketBlocks

</div>

<p align="center">
<a href="https://www.curseforge.com/minecraft/mc-mods/marketblocks/files"><img src="https://cf.way2muchnoise.eu/versions/1214103(0280ff).svg?cachebuster=1" alt="Supported Versions"></a>
<a href="https://github.com/BigBull-H3RO/MarketBlocks/blob/main/LICENSE.txt"><img src="https://img.shields.io/badge/License-MIT_%26_ARR-0280ff?style=flat" alt="License: MIT & ARR"></a>
<a href="https://www.curseforge.com/minecraft/mc-mods/marketblocks"><img src="https://cf.way2muchnoise.eu/1214103.svg?" alt="CurseForge"></a>
<a href="https://modrinth.com/mod/marketblocks"><img src="https://img.shields.io/modrinth/dt/u43pMIKj?logo=modrinth&label=&suffix=%20&style=flat&color=242629&labelColor=5ca424&logoColor=1c1c1c" alt="Modrinth"></a>
<a href="https://www.curseforge.com/minecraft/mc-mods/marketblocks/files/all?page=1&pageSize=20"><img src="https://img.shields.io/curseforge/v/1214103?logo=adguard&label=&suffix=%20&style=flat&color=1c1c1c&labelColor=121212&logoColor=5ca424" alt="Version"></a>
</p>

**MarketBlocks** is a modern NeoForge economy and trading mod for **Minecraft 1.21.1** featuring two trading systems:

- 🏪 **SingleOfferShop**: Physical block-based shops with one dedicated offer per block, available as **Trade Stands** (two blocks tall) and **Market Crates** (single block).
- 🌐 **Marketplace**: A page-based, server-wide trading hub with an in-game editor, dynamic pricing curves, stock limits, and physical hub linking.

The mod is engineered from the ground up for **server-authoritative security**, **extensive visual customization**, and **multiplayer stability** — ideal for survival servers, SMPs, and large modpacks.

---

## **✨ Features**

### 🏪 **SingleOfferShop (Player & Admin Shops)**
- **Block Variants**: **Trade Stand** (two blocks tall with glass showcase) and **Market Crate** (single block with crate layouts).
- **Flexible Pricing**: Supports up to **2 payment item stacks** for **1 result item stack**.
- **Access Control**: Choose between **Everyone**, **Whitelist**, or **Blacklist** customer modes.
- **Ownership Model**: Primary owner with up to 10 co-owners with full management privileges.
- **Admin Shop Mode**: Server-controlled shops with infinite stock and voided payments (requires OP level 2 + edit mode).
- **Promotional Sales**: Configure timed discounts and sales events on shop offers.
- **Top 10 Leaderboards**: Track the most profitable shops across the server via `/mb stats shops`.

### 🌐 **Marketplace (Central Economy Hub)**
- **Universal Access**: Open instantly via keybind (**O**), chat command (`/mb marketplace open`), or right-clicking linked blocks in the world.
- **Categorized Tabs**: Clean tabbed interface to organize items into custom categories (Minerals, Produce, Tools, etc.).
- **Live In-Game Editor**: Operators can create pages, add/edit/reorder offers, and tune pricing live without restarting the server.
- **Dynamic Demand Pricing**: Realistic price scaling that increases costs under high demand and cools down over time.
- **Stock & Daily Limits**: Control scarcity with global or per-player daily limits, maximum stock pools, and restock timers.
- **Physical Market Stalls**: Link any decorative block or NPC counter in spawn hubs using `/mb admin marketplace link`.

### 🎨 **Rich Visuals & Animated NPCs**
- **Floating Item Showcases**: Render offered goods floating, spinning, or bobbing above shops with fullbright, scale, and offset controls.
- **Visual Clerk NPCs**: Station an animated **Villager** (with 15 selectable professions) or a **Player Model** (using any player's skin) behind the counter.
- **Stock Indicator**: Market Crates feature dynamic stock rendering that visibly drains as customers purchase goods.
- **Celebration Feedback**: Happy villager particles and audio feedback on successful transactions.

### ⚙️ **Automation & Redstone Integration**
- **Sided Hopper / Pipe I/O**: Configure input and output directions for automated inventory routing.
- **Redstone Signals**: Emit a redstone pulse upon completed transactions to drive lamps, note blocks, or trade counters.
- **Comparator Output**: Read the fill level of shop storage with standard redstone comparators.

### 🔒 **Security & Performance**
- **Server-Authoritative Validation**: All transaction amounts, items, and data components are strictly verified server-side to prevent dupe or cheat exploits.
- **Shift-Click Bulk Buying**: Safely purchase maximum allowed quantities in a single click with automated limit checks.
- **Offline Notifications**: Receive login alerts if your shop ran out of stock or filled its payment storage while you were offline.
- **Resilient Storage**: Atomic disk writes with automatic `.bak` backups prevent data corruption.

---

## **🗺️ Integrations & Mod Compatibility**

MarketBlocks is designed to cooperate seamlessly with popular modpack staples:

- **Minimaps & Worldmaps**: Interactive chat search results (`/mb search <item>`) generate instant waypoints in **JourneyMap** and **Xaero's Minimap/Worldmap** (with clean chat fallback coordinates).
- **JourneyMap Live Markers**: Placed shops and linked marketplace hubs automatically display custom map markers on JourneyMap!
- **Just Enough Items (JEI)**: Native JEI plugin registers custom GUI tabs as *Extra Areas*, ensuring JEI item panels never overlap or block shop buttons.
- **Jade / WTHIT**: Looking at any shop block displays live item trade icons, prices, owner name, open/closed status, and out-of-stock warnings.
- **FTB Chunks**: Native `ftbchunks:interact_whitelist` tag support allows visitors to trade inside claimed chunks out of the box.
- **Open Parties and Claims (OpenPAC)**: Add `"interact$marketblocks:trade_stand"`, `"interact$marketblocks:trade_stand_top"`, and `"interact$marketblocks:marketcrate"` to `forcedBlockProtectionExceptionList` in `openpartiesandclaims-server.toml`.

---

## **💻 Commands & Shorthand Alias**

> 💡 **Tip:** Every command starting with `/marketblocks` can also be run using the shorthand **`/mb`** alias!

### Player Commands
| Command | Alias | Description |
|---|---|---|
| `/marketblocks marketplace open` | `/mb marketplace open` | Opens the central Marketplace GUI (keybind: **O**). |
| `/marketblocks search <item> [page]` | `/mb search <item> [page]` | Searches for player shops and marketplace offers with **[Waypoint]** and **[TP]** buttons. |
| `/marketblocks stats` | `/mb stats` | Displays top 10 SingleOfferShops and top 10 Marketplace offers. |
| `/marketblocks stats shops` | `/mb stats shops` | Displays the Top 10 player and admin shops by total sales. |
| `/marketblocks stats marketplace` | `/mb stats marketplace` | Displays the Top 10 Marketplace offers by total sales. |

### Operator & Admin Commands (`OP Level 2`)
| Command | Description |
|---|---|
| `/mb admin editmode [true\|false]` | Toggles global edit mode (unlocks in-game Marketplace editor & Admin Shop toggle). |
| `/mb admin reload` | Hot-reloads `marketplace.json` and trader configurations from disk without server restart. |
| `/mb admin resetlimits <player>` | Resets daily purchase limits for the specified player. |
| `/mb admin marketplace link [name] [tp_pos]` | Links the looked-at block to the Marketplace with optional title and teleport landing coordinates. |
| `/mb admin marketplace unlink [name]` | Removes link from looked-at block or by registered link name. |
| `/mb admin sale marketplace set <offer> <%> <min>` | Starts a timed discount on a Marketplace offer. |
| `/mb admin sale marketplace remove <offer>` | Cancels an active discount on a Marketplace offer. |
| `/mb admin sale shop set <shop> <%> <min>` | Starts a timed discount on an Admin Shop block. |
| `/mb admin sale shop remove <shop>` | Cancels an active discount on an Admin Shop block. |

---

## **⚙️ Configuration**

MarketBlocks features modular configuration files located inside `config/marketblocks/`:

- **`main.toml`**: First-join trade book, non-OP teleport permissions (`allowNonOpTeleport`), map compatibility toggles.
- **`client.toml`**: Client-side rendering options (e.g. `enableShopItemRendering` for FPS boost on low-end hardware).
- **`marketplace.toml`**: Purchase notifications and daily limit scope (`sharedDailyLimits = true/false`).
- **`singleoffer/general.toml`**: Bedrock-grade blast resistance (`shopBlastResistance`), max survival shops per player, chest I/O extensions, and GUI tab visibility.
- **`singleoffer/tradestand.toml` & `singleoffer/marketcrate.toml`**: Default visuals, NPC settings, and notifications for newly placed shop blocks.

---

## **📚 Documentation**

For complete guides, configuration tutorials, and developer documentation, visit the **[MarketBlocks Wiki](https://github.com/BigBull-H3RO/MarketBlocks/wiki)**!

---

## **⚖️ License**

This project is licensed under a dual model:
- **Code**: The source code is licensed under the **MIT License**. See [`LICENSE.txt`](LICENSE.txt).
- **Assets**: All textures, models, and audio files are **All Rights Reserved** and may not be redistributed without permission. See [`LICENSE_ASSETS.txt`](LICENSE_ASSETS.txt).

---

<div align="center">

#### 📢 **Found a bug or have a suggestion?**
Report issues on our [GitHub Issue Tracker](https://github.com/BigBull-H3RO/MarketBlocks/issues)

#### 💡 Discover more on [CurseForge](https://www.curseforge.com/minecraft/mc-mods/marketblocks) or [Modrinth](https://modrinth.com/mod/marketblocks)

</div>
