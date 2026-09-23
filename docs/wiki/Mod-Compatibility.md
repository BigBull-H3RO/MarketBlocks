# 🔌 Mod Compatibility & Integrations

Overview of built-in integrations, HUD overlays, map waypoints, and land-claim protections provided by MarketBlocks.

---

## 🗺️ Minimaps & Waypoints

### JourneyMap
- **Live Map Markers**: All placed Trade Stands, Market Crates, and linked Marketplace Hubs automatically display map markers on the minimap and full-screen map. Markers update in real time when shops are placed, renamed, or dismantled.
- **One-Click Waypoints**: Clicking **[Waypoint]** in `/mb search` results automatically creates a permanent, named waypoint in your JourneyMap manager.
- **Config**: `enableJourneyMapCompat = true` in `config/marketblocks/main.toml`.

### Xaero's Minimap & Worldmap
- **Chat Waypoints**: Clicking **[Waypoint]** in `/mb search` results outputs a clickable Xaero waypoint link directly into chat.
- **Config**: `enableXaerosCompat = true` in `config/marketblocks/main.toml`.

### Vanilla / No Map Mod Installed
- Clicking **[Waypoint]** prints formatted X, Y, Z coordinates and dimension into chat.

---

## 🧭 Just Enough Items (JEI)

MarketBlocks includes a native JEI plugin (`MarketBlocksJeiPlugin`):
- **GUI Extra Areas**: Registers the custom side tabs on **SingleOfferShops** (General, I/O, NPC, Visuals, Notifications, Access, Log) and the category/pagination tabs in the **Marketplace GUI** as exclusion zones.
- **No Overlapping**: JEI's item grid automatically shifts aside so search results never overlap or block clicks on shop tabs and action buttons.

---

## 🖥️ Jade / WTHIT (HUD Overlays)

MarketBlocks provides native server data providers for **Jade**:

### SingleOfferShop Blocks (Trade Stands & Market Crates)
Looking at a shop block displays:
- **Status**: `Open` (green), `Closed` (red), or `Admin Shop` (purple).
- **Shop & Owner Name**: Displays the custom shop title and owner name.
- **Active Offer**: Live item icons and quantities for the item being sold and the required payment(s).
- **Stock Warnings**: Displays `Out of Stock` or `Output Full` alerts in real time.

### Trader NPCs (Shop Buyers)
Looking at a wandering Trader NPC displays their current remaining **Coin Budget**.

---

## 🛡️ Land Claiming & Claim Protections

Allows visitors to trade at shops placed inside protected territory without granting full block breaking or chest access permissions:

### FTB Chunks
- **Supported out of the box** via the included `ftbchunks:interact_whitelist` block tag (`trade_stand`, `trade_stand_top`, `marketcrate`).
- Visitors can freely open and purchase from shops inside claimed chunks without server configuration.

### Open Parties and Claims (OpenPAC)
Requires adding the shop blocks to `forcedBlockProtectionExceptionList` in `config/openpartiesandclaims-server.toml`:

```toml
forcedBlockProtectionExceptionList = [
    "interact$marketblocks:trade_stand",
    "interact$marketblocks:trade_stand_top",
    "interact$marketblocks:marketcrate"
]
```
Apply in-game with `/openpac reload` or a server restart.
