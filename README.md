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

**MarketBlocks** is a NeoForge mod for **Minecraft 1.21.1** that provides a server-authoritative trading system with two shop types:

- **SingleOfferShop** (block-based, one active offer per shop, comes in Trade Stand and Market Crate variants)
- **Marketplace** (blockless, page-based, centrally managed offer system)

The mod focuses on **secure transactions**, **extensive customization**, **clear permission rules**, and **reliable persistence**—ideal for survival servers, SMPs, and modded multiplayer worlds.

## **✨ Features**

### ✅ **SingleOfferShop**

- Block variants: **Trade Stand** (two blocks tall) and **Market Crate** (single block).
- Supports up to **2 payment stacks** and **1 result stack**.
- **Access Control:** Restrict buyers via Whitelist or Blacklist.
- **Ownership:** Primary owner and up to 10 co-owners with clear UI role handling.
- Optional **Admin Shop Mode** (no stock required, unlimited supply, server-side validated).
- **Temporary Sales:** Configure timed discounts and price changes for your shop offers.
- **Top 10 Statistics:** View the top performing shops by sales numbers.

### ✅ **Marketplace**

- Blockless, centralized market system with pages and multiple offers.
- Open via keybind (**O**), command, or by interacting with any linked block in the world.
- In-game editor for creating and managing offers (Admin only).
- **Temporary Sales:** Set up limited-time sales and discounts on marketplace offers.
- **Top 10 Statistics:** Keep track of the most popular marketplace offers.
- JSON persistence with backup/restore strategy.

### ✅ **Rich Visuals & NPCs**

- **Offer Item Rendering:** Display the offered item floating, spinning, or stacked above the shop. Features Dynamic Fill Level to visually indicate stock.
- **Visual NPCs:** Display an interactive Villager (with 15 professions) or a Player Skin above your shop.
- Configurable particle and sound feedback on purchases.

### ✅ **Secure server-side transactions**

- Server-side validation of item, count, and components.
- Deterministic purchase logic with **Shift-click bulk buying**.
- Protection against client-side manipulation.

### ✅ **Limits, Restock & Demand Pricing (Marketplace)**

- Daily limits (global or per-player), stock limits, and restock intervals.
- **Demand Pricing:** Dynamic price calculation via a flexible multiplier system based on player demand and decay over time.

### ✅ **Automation & Redstone**

- **Auto I/O:** Configurable pull/push of items through adjacent inventories with redstone control.
- Optional redstone pulse emission on successful purchase.
- Read shop fill level using a Comparator.

### ✅ **Notifications & QoL**

- **Offline Notifications:** Get notified on login if your shop is out of stock or its output is full.
- Persistent shop transaction log with smart stacking.
- Comprehensive Advancement tree to guide players through features.
- Shop Directory (`/marketblocks shop list`) to view all active shops with filtering by name, owner, or category.
- **Waypoint Integration:** Seamlessly create waypoints to shops using **JourneyMap** or **Xaero's Minimap** directly from chat!

---

> All transaction logic and permission checks are handled server-side.

---

## **🛡️ Compatibility (Claiming & Protection Mods)**

MarketBlocks is designed to work in secure multiplayer environments. By default, claiming mods block interactions with blocks in claimed chunks, which would prevent players from buying items from your shops. We've ensured seamless compatibility:

- **FTB Chunks:** ✅ Fully supported out of the box! We natively include the `ftbchunks:interact_whitelist` data tag, so your shops are always interactable (clickable) in claimed chunks without letting others break them.
- **Open Parties and Claims (OpenPac):** ⚠️ Requires server config adjustment. OpenPac doesn't use tags for whitelisting. Server Admins must manually add the shop blocks to their config file.
  - **How to fix:** Open your server's `openpartiesandclaims-server.toml` file and add the shop blocks to the `forcedBlockProtectionExceptionList` like this:

    ```toml
    forcedBlockProtectionExceptionList = ["interact$marketblocks:trade_stand", "interact$marketblocks:trade_stand_top", "interact$marketblocks:marketcrate"]
    ```

- **Waypoints & HUD:** Fully supports **JourneyMap**, **Xaero's Minimap**, and **Jade** for rich tooltips and easy shop navigation.

---

## **⚙️ Configuration**

MarketBlocks is highly configurable via `marketblocks-common.toml`. Key options include:

- Over **50+ config options** for fine-tuning.
- Server admins can enable or disable individual settings tabs in the UI.
- Define default values for newly placed shops (e.g., default NPC profession, item scale).
- Global limits, admin mode toggles, and performance settings (like max render distance for NPCs).
- Configure blast resistance for shop blocks to protect them from explosions.

---

## **📝 Commands**

Main commands are grouped under **`/marketblocks`**:

| Command | Permission | Description |
| --- | --- | --- |
| **`/marketblocks shop list [page]`** | `All Players` | Lists all SingleOfferShops. Supports filtering by `owner`, `name`, or `category`. Operators get a [TP] button. All players get a [Waypoint] button. |
| **`/marketblocks shop search <item> [page]`** | `All Players` | Searches for shops buying or selling a specific item. |
| **`/marketblocks shop stats`** | `All Players` | Shows the Top 10 SingleOfferShops by total sales. |
| **`/marketblocks marketplace open`** | `All Players` | Opens the Marketplace GUI. |
| **`/marketblocks marketplace list [page]`** | `All Players` | Lists all active Marketplace offers in chat. |
| **`/marketblocks marketplace stats`** | `All Players` | Shows the Top 10 Marketplace offers by total sales. |
| **`/marketblocks admin editmode [true / false]`** | `OP Level 2` | Enables/disables global admin/edit mode for shops and marketplace. |
| **`/marketblocks admin reload`** | `OP Level 2` | Reloads marketplace configuration from disk. |
| **`/marketblocks admin resetlimits <player>`** | `OP Level 2` | Resets daily limits for the specified player. |
| **`/marketblocks admin marketplace link <name>`** | `OP Level 2` | Links any looked-at block in the world to a specific marketplace page/name. Use `unlink` to remove. |
| **`/marketblocks admin sale shop set / remove`** | `OP Level 2` | Configures or removes a temporary sale/discount on a SingleOfferShop. |
| **`/marketblocks admin sale marketplace set / remove`** | `OP Level 2` | Configures or removes a temporary sale/discount on a Marketplace offer. |
| **`/marketblocks admin trader value set / remove`** | `OP Level 2` | Sets or removes custom currency values for trader entities. |
| **`/marketblocks admin trader blacklist add / remove`** | `OP Level 2` | Adds or removes trader entities from the blacklist. |

---

## **📚 Documentation**

For detailed guides on setting up shops, configuring visuals, managing the marketplace, and more, please visit the **[MarketBlocks Wiki](https://github.com/BigBull-H3RO/MarketBlocks/wiki)**!

---

## **⚖️ License**

This project is licensed under a split licensing model:

- **Code:** The source code of **MarketBlocks** is licensed under the **MIT License**. See the [`LICENSE.txt`](LICENSE.txt) file for the full license text.
- **Assets:** All graphical assets (textures, models, icons) and sound files are **strictly All Rights Reserved** and may not be reused without permission. See the [`LICENSE_ASSETS.txt`](LICENSE_ASSETS.txt) file for details.

---

<div align="center">

#### 📢 **Found a bug? Have a suggestion?**

Report issues to the [Issue Tracker](https://github.com/BigBull-H3RO/MarketBlocks/issues)

#### 💡 Find out more about MarketBlocks on our [Curseforge](https://www.curseforge.com/minecraft/mc-mods/marketblocks) or [Modrinth](https://modrinth.com/mod/marketblocks) Page

</div>
