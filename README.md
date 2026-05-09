<p align="center"><img src=".idea/icon.png" alt="Logo" width="250"></p>

<div align="center">

# MarketBlocks

</div>

<p align="center">
<a href="https://www.curseforge.com/minecraft/mc-mods/marketblocks/files"><img src="https://cf.way2muchnoise.eu/versions/1214103(0280ff).svg?cachebuster=1" alt="Supported Versions"></a>
<a href="https://github.com/BigBull-H3RO/MarketBlocks/blob/main/LICENSE"><img src="https://img.shields.io/github/license/BigBull-H3RO/MarketBlocks?style=flat&color=0280ff" alt="License"></a>
<a href="https://www.curseforge.com/minecraft/mc-mods/marketblocks"><img src="https://cf.way2muchnoise.eu/1214103.svg?" alt="CurseForge"></a>
<a href="https://modrinth.com/mod/marketblocks"><img src="https://img.shields.io/modrinth/dt/u43pMIKj?logo=modrinth&label=&suffix=%20&style=flat&color=242629&labelColor=5ca424&logoColor=1c1c1c" alt="Modrinth"></a>
<a href="https://www.curseforge.com/minecraft/mc-mods/marketblocks/files/all?page=1&pageSize=20"><img src="https://img.shields.io/curseforge/v/1214103?logo=adguard&label=&suffix=%20&style=flat&color=1c1c1c&labelColor=121212&logoColor=5ca424" alt="Version"></a>
</p>

**MarketBlocks** is a NeoForge mod for **Minecraft 1.21.1** that provides a server-authoritative trading system with two shop types:

- **SingleOfferShop** (block-based, one active offer per shop)
- **Marketplace** (blockless, page-based offer system)

The mod focuses on **secure transactions**, **clear permission rules**, and **reliable persistence**—ideal for survival servers, SMPs, and modded multiplayer worlds.

## **✨ Features**
✅ **SingleOfferShop (formerly SmallShop)**
- Block-based shop with one active offer per shop.
- Supports up to **2 payment stacks** and **1 result stack**.
- Additional co-owners with clear UI role handling.
- Optional **Admin Shop Mode** (no stock required, server-side validated).

✅ **Marketplace (formerly ServerShop)**
- Blockless, centralized market system with pages and multiple offers.
- Open via keybind (**O**) or command.
- Server-side sync with snapshot + runtime view states.
- JSON persistence with backup/restore strategy.

✅ **Secure server-side transactions**
- Server-side validation of item, count, and components.
- Deterministic purchase logic (including shift/bulk buys).
- Protection against client-side manipulation.

✅ **Limits, Restock & Demand Pricing (Marketplace)**
- Daily limits (global or per-player), stock limits, and restock intervals.
- Dynamic price calculation via multiplier system.
- Automatic runtime upkeep via server ticks.

✅ **Chest I/O extension (experimental)**
- Optional automatic pull/push of items through adjacent inventories.
- Enabled separately via config.

✅ **Transaction log & QoL**
- Persistent shop transaction log for SingleOfferShop.
- Optional redstone pulse on successful purchase.
- Configurable visual/audio shop feedback.

---

> All transaction logic and permission checks are handled server-side.

---

## **⚙️ Configuration**
Key switches are available in the common config, including:

- `enableDoubleChestSupport`
- `enableChestIoExtensionExperimental`
- `offerUpdateInterval`
- `chestIoInterval`
- `marketplaceGlobalDailyLimit`
- `marketblocksAdminModeEnabled`
- `visualNpcRenderViewDistance`

Additional fine-tuning options exist for SingleOfferShop, Marketplace, and visual/NPC behavior.

---

## **📝 Commands**
Main commands are grouped under **`/marketblocks`**:

| Command | Permission | Description |
| --- | --- | --- |
| **`/marketblocks adminmode [true|false]`** | `admin` | Enables/disables global admin/edit mode. |
| **`/marketblocks marketplace`** | `admin` | Opens the marketplace. |
| **`/marketblocks marketplace reload`** | `admin` | Reloads marketplace configuration from disk. |
| **`/marketblocks marketplace resetlimits <player>`** | `admin` | Resets daily limits for the specified player. |

---

## **🛠 Quick Workflow**
1. Place a **SingleOfferShop** and manage it as owner.
2. Create an offer (result + up to 2 payments).
3. Optionally configure input/output and Chest I/O.
4. Open the **Marketplace** for centralized offer management.

This allows local player shops and a global market system to run side by side.

---

<div align="center">

#### 📢 **Found a bug? Have a suggestion?**

Report issues to the [Issue Tracker](https://github.com/BigBull-H3RO/MarketBlocks/issues)

#### 💡 Find out more about MarketBlocks on our [Curseforge](https://www.curseforge.com/minecraft/mc-mods/marketblocks) or [Modrinth](https://modrinth.com/mod/marketblocks) Page
</div> 