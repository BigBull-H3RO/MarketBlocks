# MarketBlocks Wiki

Welcome to the official **MarketBlocks** documentation!

MarketBlocks is a comprehensive economy and trading mod for **Minecraft 1.21.1 (NeoForge)**. It brings lively, immersive trading to your world through three core systems:

---

## 🌟 The Three Pillars of MarketBlocks

### 1. 🏪 SingleOfferShop (Player Shops)
Placeable shop blocks with dedicated single-offer trading:
- **Block Variants**: The two-block-tall **Trade Stand** (with glass showcase) and the compact **Market Crate** (with stacked or scattered multi-item display).
- **Player Ownership & Protection**: Safe against griefing and explosions. Only the owner (and registered co-owners) can configure or dismantle the shop. Server admins can bypass protection using Shift in Creative mode.
- **In-Game Customization**: Configure custom shop names, animated clerk NPCs (Villagers or custom Player skins), floating 3D items, hopper/chest automation, and stock alerts.
- **Transaction History**: View the last 100 customer purchases with player heads, item counts, and timestamps.
- ➡️ *Read more: [SingleOfferShop Overview](SingleOfferShop) & [Settings Guide](SingleOfferShop-Settings)*

### 2. 🌐 Central Marketplace (Server Economy Hub)
A server-wide trading hub organized into tabbed category pages:
- **Multiple Ways to Access**: Open anywhere via keybind (**O** by default), chat command (`/mb marketplace`), or right-click decorative market stalls linked in spawn cities (`/mb admin marketplace link`).
- **Live In-Game Editor**: Operators can create pages, add offers, reorder slots, and configure prices directly in-game using `/mb admin editmode true` — no JSON editing required!
- **Dynamic Pricing & Limits**: Simulate living economies with demand-based price curves, stock caps, restock timers, daily player purchase limits, and timed promotional sales.
- ➡️ *Read more: [Marketplace Guide](Marketplace)*

### 3. 🧑‍🌾 Trader NPCs (Autonomous Customers)
Autonomous Wandering Traders that roam the Overworld and actively visit player shops:
- **Living Economy**: Trader NPCs locate player shops, walk up to the counter, examine goods for 4–8 seconds, and buy items with their own budget!
- **Social Ranks**: Spawns as **Citizen** (everyday goods), **Wealthy** (equipment, potions, minerals), or **Noble** (rare treasures, bulk purchases).
- **Multiplayer Fair**: Each player has their own daily spawn cooldown; traders only appear when active shops are nearby.
- ➡️ *Read more: [Trader NPC Guide](Trader-NPC)*

---

## ⚡ Quick Navigation

- **Player Shops**:
  - [SingleOfferShop Overview](SingleOfferShop): Crafting recipes, block variants, ownership, protection, and buying.
  - [Settings Guide](SingleOfferShop-Settings): The 6 settings tabs (General, I/O, Villager, Visuals, Notifications, Access).
  - [Admin Shop Mode](SingleOfferShop-Admin-Shop-Mode): Creating infinite-supply server shops.
  - [Setup Examples](SingleOfferShop-Examples-and-Common-Setups): 5 ready-to-use trading setups.
- **Central Marketplace**:
  - [Marketplace Guide](Marketplace): Opening the hub, browsing, in-game editor, and troubleshooting FAQ.
  - [In-Game Management](Marketplace-In-Game-Management): Detailed editor walkthrough.
  - [Dynamic Pricing & Limits](Marketplace-Dynamic-Pricing-and-Limits): Demand curves and daily quotas.
  - [JSON Configuration Guide](Marketplace-JSON-Configuration-Guide): File-level configuration and backup restoration.
- **Wandering NPC Customers**:
  - [Trader NPC Guide](Trader-NPC): Spawning rules, social ranks, budgets, and behaviors.
- **Server & Admin Guides**:
  - [Commands & Permissions](Commands-and-Permissions): Complete command tree with `/mb` shorthand, permissions, and waypoints.
  - [Configuration Guide](Configuration-Guide): Overview of all `.toml` configuration files and server settings.
  - [Mod Compatibility](Mod-Compatibility): Out-of-the-box support for JourneyMap, Xaero's, Jade, JEI, and Land Claiming mods.

