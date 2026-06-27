# MarketBlocks – Changelog & Release Template

This document serves as the master changelog for MarketBlocks as well as the design template for all future releases on CurseForge, Modrinth, and GitHub Releases.

---

## 📋 Release Notes Template (For Future Releases)

```markdown
# MarketBlocks vX.Y.Z-1.21.1

A brief overview of what this update focuses on (e.g., major feature additions, performance tweaks, or bug fixes).

### 🚀 New Features
- **[Feature Name]**: Description of the new functionality or command.
- **[Feature Name]**: Description of the new block or item.

### ⚙️ Improvements & Balancing
- **[Component]**: Description of the improvement or config change.

### 🐛 Bug Fixes
- **[Component]**: Fixed an issue where [Problem Description].

### 🛡️ Compatibility & Technical
- **[Mod Name / API]**: Description of compatibility updates or API changes.
```

---

## 📦 Releases

# MarketBlocks v1.0.0-1.21.1

**Initial Stable Release for Minecraft 1.21.1 (NeoForge)**

Welcome to the official 1.0 release of **MarketBlocks**! This mod delivers a robust, server-authoritative trading economy built for survival servers, SMPs, and modded multiplayer networks.

### 🚀 Key Features
- **SingleOfferShops**: Placeable **Trade Stands** and **Market Crates** with powerful access control (whitelist/blacklist), up to 10 co-owners, and admin shop modes.
- **Server Marketplace**: Blockless, centralized trading hub accessible via keybind (`O`), command (`/marketblocks marketplace open`), or linked physical blocks.
- **Visual NPCs & Items**: Interactive Villager/Player NPCs and floating/spinning items with dynamic fill level indicators.
- **Advanced Economics**: Built-in demand pricing multipliers, daily purchase limits (global or per-player), stock limits, and automated restocking intervals.
- **Redstone & Automation**: Fully controllable Auto-I/O via hoppers/pipes with redstone pulse emission and comparator fill-level reading.
- **Extensive Commands**: Inspect top performing shops (`/marketblocks shop stats`), search items (`/marketblocks shop search`), configure temporary sales (`/marketblocks admin sale`), and manage player limits (`/marketblocks admin resetlimits`).

### 🛡️ Mod Compatibility
- **JourneyMap & Xaero's Minimap**: Clickable `[Waypoint]` buttons in chat directories to instantly set minimap waypoints to shops.
- **Jade HUD**: Real-time HUD overlay showing shop open/closed status, current price, and owner.
- **FTB Chunks**: Fully compatible out of the box via `ftbchunks:interact_whitelist` tags.
- **OpenPac**: Documented support for `openpartiesandclaims-server.toml` exception lists.
