# MarketBlocks Wiki

Welcome to the official **MarketBlocks** documentation!

MarketBlocks is a comprehensive economy and trading mod for **Minecraft 1.21.1+ (Fabric & NeoForge)**. It brings lively, immersive trading to your world through three core systems:

---

## 🌟 The Three Pillars of MarketBlocks

### 1. 🏪 SingleOfferShop (Player Shops)
Placeable shop blocks with dedicated single-offer trading:
- **Block Variants**: The two-block-tall **Trade Stand** (with glass showcase) and the compact **Market Crate** (displaying stock items inside).
- **Player Ownership & Protection**: Safe against griefing and explosions. Only the owner and registered co-owners can configure or dismantle the shop.
- **In-Game Customization**: Configure custom shop names, animated clerk NPCs (Villagers or custom Player skins), floating 3D items, hopper/chest automation, and stock alerts.
- **Transaction History**: View the last 100 customer purchases with player heads, item counts, and timestamps.
- ➡️ **Guides:** [Overview](SingleOfferShop) • [Settings Guide](SingleOfferShop-Settings) • [Admin Shop Mode](SingleOfferShop-Admin-Shop-Mode) • [Setup Examples](SingleOfferShop-Examples-and-Common-Setups)

### 2. 🌐 Central Marketplace (Server Economy Hub)
A server-wide trading hub organized into tabbed category pages:
- **Multiple Ways to Access**: Open anywhere via keybind (**O** by default), chat command (`/mb marketplace`), or right-click decorative market stalls linked in spawn cities (`/mb admin marketplace link`).
- **Live In-Game Editor**: Operators can create pages, add offers, reorder slots, and configure prices directly in-game using `/mb admin editmode true` — no JSON editing required!
- **Dynamic Pricing & Limits**: Simulate living economies with demand-based price curves, stock caps, restock timers, daily player purchase limits, and timed promotional sales.
- ➡️ **Guides:** [Marketplace Overview](Marketplace) • [In-Game Management](Marketplace-In-Game-Management) • [Dynamic Pricing & Limits](Marketplace-Dynamic-Pricing-and-Limits) • [JSON Configuration](Marketplace-JSON-Configuration-Guide)

### 3. 🧑‍🌾 Trader NPCs (Autonomous Customers)
Autonomous Wandering Traders that roam the Overworld and actively visit player shops:
- **Living Economy**: Trader NPCs locate player shops, walk up to the counter, examine goods for 4–8 seconds, and buy items with their own budget!
- **Social Ranks**: Spawns as **Citizen** (everyday goods), **Wealthy** (equipment, potions, minerals), or **Noble** (rare treasures, bulk purchases).
- **Active Shop Visits**: Traders only arrive when active, stocked player shops are nearby.
- ➡️ **Guide:** [Trader NPC Guide](Trader-NPC)

---

## 🛠️ Server Administration & Integrations

- ⚙️ **[Commands & Permissions](Commands-and-Permissions)**: Complete command tree (`/mb`), operator permissions, and interactive chat waypoints.
- 📁 **[Configuration Guide](Configuration-Guide)**: Overview of all `.toml` and `.json` configuration files and server settings.
- 🔌 **[Mod Compatibility](Mod-Compatibility)**: Out-of-the-box support for JourneyMap, Xaero's, Jade, JEI, and Land Claiming mods (FTB Chunks, OpenPAC).

