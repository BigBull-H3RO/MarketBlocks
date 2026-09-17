<p align="center">
  <img src="docs/assets/icon.png" alt="MarketBlocks Logo" width="220">
</p>

<div align="center">

# MarketBlocks

**The Complete Economy, Physical Shop & Autonomous Trader NPC Suite for Minecraft 1.21.1 (NeoForge)**

[![Minecraft 1.21.1](https://img.shields.io/badge/Minecraft-1.21.1-brightgreen.svg?style=flat&logo=minecraft)](https://www.curseforge.com/minecraft/mc-mods/marketblocks)
[![NeoForge](https://img.shields.io/badge/Modloader-NeoForge-orange.svg?style=flat)](https://neoforged.net/)
[![Wiki](https://img.shields.io/badge/Documentation-GitHub_Wiki-blue.svg?style=flat&logo=github)](https://github.com/BigBull-H3RO/MarketBlocks/wiki)
[![License: MIT & ARR](https://img.shields.io/badge/License-MIT_%26_ARR-0280ff.svg?style=flat)](https://github.com/BigBull-H3RO/MarketBlocks/blob/main/LICENSE)
[![CurseForge](https://cf.way2muchnoise.eu/1214103.svg)](https://www.curseforge.com/minecraft/mc-mods/marketblocks)
[![Modrinth](https://img.shields.io/modrinth/dt/u43pMIKj?logo=modrinth&label=&suffix=%20&style=flat&color=242629&labelColor=5ca424&logoColor=1c1c1c)](https://modrinth.com/mod/marketblocks)

</div>

---

**MarketBlocks** delivers a complete, server-authoritative economy framework designed for SMPs, survival servers, and modpacks. It seamlessly bridges physical player commerce, global server trading, and lifelike NPC customers into one unified experience:

1. 🏪 **SingleOfferShop**: Dedicated block-based shops (**Trade Stand** & **Market Crate**) with custom visual clerks, floating showcases, and automated hopper/pipe routing.
2. 🌐 **Marketplace**: A page-based server trading hub featuring dynamic demand pricing curves, physical stall linking, and a live in-game admin editor.
3. 🚶 **Autonomous Trader NPCs**: Wandering Trader customers that actively visit player shops, inspect wares, negotiate across counters ("Smart Reach"), and buy items according to their rank and budget.

---

## **🚀 Quick Start Guide**

### 1. Crafting Recipes

| **Trade Stand** | **Market Crate** |
|:---:|:---:|
| ![Trade Stand Recipe](docs/images/recipes/trade_stand.png)<br>*(2 Blocks Tall, Glass Showcase)* | ![Market Crate Recipe](docs/images/recipes/market_crate.png)<br>*(Single Block, Dynamic Stock Display)* |

<details>
<summary>📋 <b>Click to expand recipe ingredients (Text Breakdown)</b></summary>

- **Trade Stand**: 4× Any Planks, 1× Smooth Stone Slab, 2× Iron Ingot, 1× Emerald, 1× Any Wooden Sign
- **Market Crate**: 4× Any Planks, 1× Emerald, 1× Chest, 2× Barrels, 1× Any Wooden Sign

</details>

### 2. Setting Up Your First Trade
1. **Place the Block**: Place your Trade Stand or Market Crate down. You automatically become the shop owner.
2. **Open the GUI**: Right-click the shop to open the interface.
3. **Configure the Offer**:
   - Drag the item you want to sell into the **Offer Slot**.
   - Place up to 2 required payment items in the **Payment Slots** (e.g. 5 Diamonds + 1 Gold Ingot).
   - Place inventory stock into the shop's storage.
4. **Customize via Tabs**: Use the top tabs to add Co-Owners, change the visual clerk (Villager profession or custom Player Skin), toggle notifications, or connect hoppers.

### 3. Accessing the Central Marketplace
- Press **`O`** (default keybind) or type **`/mb marketplace`** to browse server offers.
- Visit designated market stalls in your server's spawn hub — operators can link any decorative block or NPC counter directly to the Marketplace!

---

## **✨ Key Features**

### 🏪 **SingleOfferShop (Player & Admin Shops)**
- **Two Unique Block Styles**:
  - **Trade Stand**: Elegant two-block counter with animated glass display case.
  - **Market Crate**: Compact single-block rustic crate featuring **dynamically draining stock rendering** that visually reflects how full the crate is.
- **Flexible 2-to-1 Pricing**: Accept up to **two distinct payment item stacks** for one result item stack.
- **Customer Access Modes**: Restrict trades to **Everyone**, a specific **Whitelist**, or block troublemakers with a **Blacklist**.
- **Co-Ownership & Security**: Register up to **10 trusted co-owners**. Unbreakable by visitors, immune to explosions, and protected by **Admin Shift-Break** checks.
- **Safe Bulk Buying**: Customers can hold **Shift** while clicking *Buy* to instantly purchase the maximum affordable quantity without multiple clicks.
- **Transaction History**: Detailed in-GUI log records customer names, timestamps, purchase quantities, and trader ranks.

### 🚶 **Autonomous Trader NPCs (AI Customers)**
- **Lifelike Market Activity**: Wandering traders periodically spawn near active, stocked player shops and wander through player shopping districts.
- **Three Distinct Customer Ranks**:
  - **Citizen**: Everyday shoppers with modest budgets.
  - **Wealthy**: High-tier merchants buying in larger volumes (monocle accessory).
  - **Noble**: Aristocrats with deep pockets looking for luxury items (glasses & velvet top hat).
- **Smart Counter Reach**: NPCs pathfind naturally, stopping 1.5–2.5 blocks in front of counters and interacting over slabs, fences, and tables.
- **Deliberation & Authenticity**: Traders pause to inspect items, shake their heads if overpriced, celebrate purchases with happy particles, and carry their bought goods visibly in their arms.
- **Rage Mode Easter Egg**: Annoying or attacking a customer trader will provoke immediate, humorous consequences!

### 🌐 **Marketplace (Central Economy Hub)**
- **Universal & Physical Access**: Open via keybind (**`O`**), command (`/mb marketplace`), or right-clicking linked decorative blocks.
- **Live In-Game Editor**: Operators can enable Edit Mode (`/mb admin editmode true`) to create categories, add offers, adjust prices, and reorder items directly in the GUI without touching JSON files.
- **Dynamic Demand Curves**: Real-time price scaling automatically raises item prices under heavy buying pressure and cools down over time.
- **Scarcity & Daily Limits**: Set global stock caps or personal daily purchase limits per player with configurable restock timers.
- **Promotional Discounts**: Schedule timed sales events on marketplace offers or admin shops with automatic countdowns.

### 🎨 **Visual Customization & Clerk NPCs**
- **Floating Item Showcases**: Showcase items float, spin, and bob above the shop counter with customizable scale, rotation, and fullbright glow.
- **Clerk NPCs Behind the Counter**: Station a visual clerk behind your shop:
  - **Villager Model**: Choose from 15 selectable professions and biomes.
  - **Player Model**: Render any player's skin by typing their Minecraft username!

### ⚙️ **Hopper, Pipe & Redstone Automation**
- **Sided Directional I/O**: Configure independent input (restock) and output (payout) sides for hoppers, pipes, and AE2/Refined Storage cables.
- **Redstone Pulse on Trade**: Emits a configurable redstone signal every time a trade is completed.
- **Comparator Support**: Standard comparators read shop storage fill level to drive external warning lamps or sorting circuits.

---

## **🗺️ Integrations & Mod Compatibility**

MarketBlocks integrates smoothly with popular modpack staples out of the box:

- **JourneyMap**: Real-time shop and market stall markers placed directly on your map. Interactive chat search results (`/mb search <item>`) generate instant waypoints!
- **Xaero's Minimap & Worldmap**: Chat search results provide clickable **[Waypoint]** and **[TP]** coordinates for Xaero's map systems.
- **Just Enough Items (JEI)**: Native JEI integration registers shop GUI tabs as *Extra Areas*, preventing JEI item panels from overlapping buttons.
- **Jade & WTHIT**: Looking at any shop block displays live trade icons, prices, owner name, open/closed status, and out-of-stock warnings.
- **FTB Chunks**: Native `ftbchunks:interact_whitelist` tag support allows visitors to trade inside claimed territory without extra configuration.
- **Open Parties and Claims (OpenPAC)**: Compatible via `forcedBlockProtectionExceptionList` in `openpartiesandclaims-server.toml`.

---

## **💻 Essential Commands**

> 💡 **Tip:** Every command starting with `/marketblocks` can also be run using the short **`/mb`** alias!

### Player Commands
| Command | Shorthand | Description |
|---|---|---|
| `/marketblocks marketplace` | `/mb marketplace` | Opens the central Marketplace GUI (Default Keybind: **`O`**). |
| `/marketblocks search <item> [page]` | `/mb search <item>` | Finds player shops and market offers with **[Waypoint]** and **[TP]** buttons. |
| `/marketblocks stats` | `/mb stats` | Displays top 10 SingleOfferShops and top 10 Marketplace offers. |
| `/marketblocks stats shops` | `/mb stats shops` | Displays top player and admin shops by total sales volume. |
| `/marketblocks stats marketplace` | `/mb stats marketplace` | Displays top Marketplace offers by sales volume. |

### Operator Commands (`OP Level 2`)
| Command | Description |
|---|---|
| `/mb admin editmode [true\|false]` | Unlocks the live in-game Marketplace editor and the Admin Shop toggle in shop GUIs. |
| `/mb admin marketplace link [name] [tp_pos]` | Links the looked-at block/counter to the Marketplace with optional waypoint title and teleport landing spot. |
| `/mb admin marketplace unlink [name]` | Removes marketplace link from the targeted block or by link name. |
| `/mb admin reload` | Hot-reloads `marketplace.json` and trader configurations from disk without restarting the server. |
| `/mb admin resetlimits <player>` | Resets daily purchase limits for the specified player. |
| `/mb admin sale marketplace set <offer> <%> <min>` | Starts a timed discount on a Marketplace offer. |
| `/mb admin sale shop set <shop> <%> <min>` | Starts a timed discount on an Admin Shop block. |

---

## **⚙️ Modular Configuration**

All configuration files are organized cleanly in `config/marketblocks/`:

- **`main.toml`**: First-join trade book, non-OP teleport permissions (`allowNonOpTeleport`), map integration toggles.
- **`client.toml`**: Client-side graphics (e.g. `enableShopItemRendering` for low-end hardware FPS boosts).
- **`marketplace.toml`**: Restock intervals, shared daily limits, and purchase sound alerts.
- **`trader/trader.toml`**: NPC customer spawn timers, rank spawn weights, shopping budgets, and reach tolerances.
- **`trader/*.json`**: Custom item values (`trader_item_values.json`), purchase blacklists (`trader_blacklist.json`), and custom customer names (`trader_names.json`) — hot-reloadable with `/mb admin reload`!
- **`singleoffer/general.toml`**: Bedrock-grade blast resistance, player shop limits, chest extensions, and GUI tab permissions.
- **`singleoffer/tradestand.toml` & `singleoffer/marketcrate.toml`**: Default visuals, NPC models, and alert thresholds for newly placed shops.

---

## **📚 Comprehensive Documentation**

Looking for detailed guides, permission breakdowns, or automation tutorials? Visit our **[Official GitHub Wiki](https://github.com/BigBull-H3RO/MarketBlocks/wiki)**:

- 📖 **[Wiki Home](https://github.com/BigBull-H3RO/MarketBlocks/wiki)** — Full documentation overview.
- 🏪 **[SingleOfferShop Player Guide](https://github.com/BigBull-H3RO/MarketBlocks/wiki/SingleOfferShop)** — Crafting, placement, trade setups, and security.
- ⚙️ **[SingleOfferShop GUI Settings](https://github.com/BigBull-H3RO/MarketBlocks/wiki/SingleOfferShop-Settings)** — Deep dive into all 6 GUI tabs (General, I/O, Villager, Visuals, Notifications, Access).
- 🌐 **[Central Marketplace Guide](https://github.com/BigBull-H3RO/MarketBlocks/wiki/Marketplace)** — In-Game Edit Mode, category management, dynamic curves, and block linking.
- 🚶 **[Trader NPC Customer System](https://github.com/BigBull-H3RO/MarketBlocks/wiki/Trader-NPC)** — Spawning mechanics, ranks, counter reach, and behavior.
- 🛠️ **[Configuration Reference](https://github.com/BigBull-H3RO/MarketBlocks/wiki/Configuration-Guide)** — Complete breakdown of all `.toml` files and server settings.
- 💻 **[Commands & Permissions](https://github.com/BigBull-H3RO/MarketBlocks/wiki/Commands-and-Permissions)** — Comprehensive list of player and operator commands.
- 🧩 **[Mod Compatibility](https://github.com/BigBull-H3RO/MarketBlocks/wiki/Mod-Compatibility)** — Map waypoints, JEI areas, Jade tooltips, and chunk claim setup.

---

## **⚖️ License & Credits**

MarketBlocks utilizes a dual licensing model:
- **Code**: The mod source code is licensed under the **MIT License**. See [`LICENSE`](LICENSE).
- **Assets**: All textures, 3D models, audio, and branding assets are **All Rights Reserved** and may not be redistributed without permission. See [`LICENSE_ASSETS.txt`](LICENSE_ASSETS.txt).

---

<div align="center">

### 💬 Community & Support

**Found a bug or have a suggestion?**<br>
Open an issue on our [GitHub Issue Tracker](https://github.com/BigBull-H3RO/MarketBlocks/issues)

**Download Releases & Updates**<br>
[CurseForge](https://www.curseforge.com/minecraft/mc-mods/marketblocks) • [Modrinth](https://modrinth.com/mod/marketblocks)

</div>
