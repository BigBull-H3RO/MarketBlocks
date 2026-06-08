# MarketBlocks Wiki

Welcome to the official MarketBlocks wiki.

MarketBlocks is a NeoForge mod for **Minecraft 1.21.1** with two trading systems:

- **SingleOfferShop** (block-based, one active offer per shop, features block variants like Trade Stand and Market Crate)
- **Marketplace** (page-based, centrally managed, accessible via keybind or command)

## Quick Start / Installation

1. Install **NeoForge for Minecraft 1.21.1** on server and client.
2. Put the latest `marketblocks` JAR into the `mods` folder.
3. Start the server so configuration files are created.
4. Optionally adjust global settings in `marketblocks-common.toml` (e.g., admin mode, blast resistance, tab visibility, default values).
5. Craft a Trade Stand or Market Crate and place it to start your first shop.
6. Open the Marketplace with the **O** key or via `/marketblocks marketplace`.

## Versions & Requirements

- **Minecraft:** 1.21.1
- **Modloader:** NeoForge
- **Mod:** MarketBlocks

> Important: This wiki uses the current names: **Marketplace** and **SingleOfferShop**.

## Key Features

- **SingleOfferShop**: Block-based shops with owner system, co-owners, access control (whitelist/blacklist), visual NPCs, offer item rendering, notifications, I/O automation, redstone integration, transaction logs, and advancements
- **Marketplace**: JSON-driven page-and-offer system with in-game editor, demand-based pricing, stock/daily limits, and restocking
- **Server-authoritative**: All transactions and mutations are validated server-side
- **Highly configurable**: 50+ config options including tab visibility toggles and default values for new shops

## Wiki Navigation

- **Marketplace**: [JSON configuration](Marketplace-JSON-Configuration-Guide), [in-game management](Marketplace-In-Game-Management), [limits & pricing](Marketplace-Dynamic-Pricing-and-Limits), [troubleshooting](Marketplace-Troubleshooting)
- **SingleOfferShop**: [setup & roles](SingleOfferShop-Setup-and-Roles), [settings overview](SingleOfferShop-Settings), [visual NPC](SingleOfferShop-Visual-NPC), [offer item visuals](SingleOfferShop-Offer-Item-Visuals), [notifications](SingleOfferShop-Notifications), [access control](SingleOfferShop-Access-Control), [admin shop mode](SingleOfferShop-Admin-Shop-Mode), [examples](SingleOfferShop-Examples-and-Common-Setups), [advancements](SingleOfferShop-Advancements)
- **[Commands & Permissions](Commands-and-Permissions)**: Commands and permissions by role
- **[Developer Info](Developer-Info)**: Technical entry points for addons and integrations
