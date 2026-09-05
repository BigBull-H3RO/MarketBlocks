# MarketBlocks Wiki

Welcome to the official MarketBlocks documentation.

MarketBlocks is a feature-rich trading and economy mod for **Minecraft 1.21.1 (NeoForge)** offering two unified trading architectures:

- **SingleOfferShop**: Block-based shops with one dedicated trade offer per shop, featuring block variants like the **Trade Stand** and **Market Crate**.
- **Marketplace**: A page-based, server-wide trading hub with an in-game editor, dynamic pricing curves, stock limits, and linked world stalls.

---

## Quick Start & Installation

1. Install **NeoForge for Minecraft 1.21.1** on your server and client.
2. Place the latest `marketblocks` JAR into your `mods` folder.
3. Start the server to generate default configurations under `config/marketblocks/`.
4. *(Optional)* Fine-tune economy or mechanics across the modular configs:
   - `main.toml`: First-join trade book, non-OP teleport permissions, map integration toggles.
   - `marketplace.toml`: Shared daily limits, purchase chat broadcasts.
   - `singleoffer/general.toml`: Blast resistance, survival placement limits, chest I/O extensions, tab visibility.
   - `singleoffer/tradestand.toml` & `singleoffer/marketcrate.toml`: Default values for newly placed shop blocks.
5. Craft a **Trade Stand** or **Market Crate** to launch your first player shop.
6. Open the server Marketplace using the **O** key or via `/marketblocks marketplace open` (or `/mb marketplace open`).

---

## Key Highlights

- 🏪 **SingleOfferShop**: Player-owned shops with primary & co-owners, whitelist/blacklist access control, visual clerk NPCs, floating item showcases, automated hopper I/O, redstone signals, and transaction history.
- 🌐 **Marketplace**: Centralized category pages with an in-game editor (`/mb admin editmode`), dynamic demand-based pricing, timed sale discounts, and linked physical market stalls.
- ⚡ **Streamlined Commands**: Complete command tree with convenient **`/mb`** shorthand alias (e.g., `/mb search <item>` or `/mb stats`).
- 🗺️ **Mod Integrations**: Native out-of-the-box support for **JourneyMap**, **Xaero's Minimap/Worldmap**, **Jade**, **JEI**, **FTB Chunks**, and **OpenPAC**.
- 🔒 **Server-Authoritative**: All trades, mutations, and permissions are validated server-side with atomic disk writes and automatic backups.

---

## Wiki Navigation

- **Marketplace**:
  - [In-Game Management](Marketplace-In-Game-Management)
  - [JSON Configuration Guide](Marketplace-JSON-Configuration-Guide)
  - [Dynamic Pricing & Limits](Marketplace-Dynamic-Pricing-and-Limits)
  - [Troubleshooting](Marketplace-Troubleshooting)
- **SingleOfferShop**:
  - [Setup & Roles](SingleOfferShop-Setup-and-Roles)
  - [Settings Overview](SingleOfferShop-Settings)
  - [Visual NPC](SingleOfferShop-Visual-NPC)
  - [Offer Item Visuals](SingleOfferShop-Offer-Item-Visuals)
  - [Notifications](SingleOfferShop-Notifications)
  - [Access Control](SingleOfferShop-Access-Control)
  - [Admin Shop Mode](SingleOfferShop-Admin-Shop-Mode)
  - [Examples & Common Setups](SingleOfferShop-Examples-and-Common-Setups)
  - [Advancements](SingleOfferShop-Advancements)
- **[Mod Compatibility & Integrations](Mod-Compatibility)**: Minimaps, Jade HUD, JEI panels, and Land Claiming protections.
- **[Commands & Permissions](Commands-and-Permissions)**: Complete command reference and role access matrix.
- **[Developer Info](Developer-Info)**: Technical architecture, saved data models, and addon entry points.
