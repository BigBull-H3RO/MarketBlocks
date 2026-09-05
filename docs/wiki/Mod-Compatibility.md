# Mod Compatibility & Integrations

MarketBlocks is built to integrate smoothly into modpacks and multiplayer servers. Whether you are running a public SMP server with land claims, a rich RPG modpack with minimaps, or technical HUD overlays, MarketBlocks provides dedicated integrations.

---

## 🗺️ Minimap & Waypoint Integrations

Finding player shops and marketplace hubs in large worlds is seamless.

### Interactive Chat Waypoints
Whenever players run `/marketblocks search <item>` (or `/mb search <item>`), every matching shop entry in chat includes an interactive **[Waypoint]** button:
- **JourneyMap**: Clicking creates a named waypoint in your JourneyMap manager with dimension and coordinates.
- **Xaero's Minimap & Worldmap**: Clicking creates a waypoint directly on your HUD minimap and world map.
- **Vanilla / No Map Mod**: Clicking prints the exact X, Y, Z coordinates and dimension in chat with clear formatting.

### Live JourneyMap World Markers
When **JourneyMap** is installed on the client and enabled in `marketblocks-client.toml` (`enableJourneyMapCompat = true`):
- **Player Shops**: Any placed Trade Stand or Market Crate automatically displays a shop marker on your full-screen and mini map. Markers automatically update when shops are broken or renamed.
- **Marketplace Hubs**: Any world block linked to the Marketplace via `/mb admin marketplace link` automatically receives a marketplace map marker.

---

## 🧭 Just Enough Items (JEI)

MarketBlocks includes a native **JEI Plugin**:
- **Automatic GUI Bounds Exclusion**: The custom side tabs on **SingleOfferShops** (General, I/O, NPC, Visuals, Notifications, Access, Log) and the category/editor tabs in the **Marketplace GUI** are registered as JEI *Extra Areas*.
- **No Overlapping**: JEI's item grid automatically shifts aside, ensuring JEI search results never cover or block clicks on your shop tabs and action buttons.

---

## 🖥️ Jade / WTHIT (HUD Overlays)

MarketBlocks provides rich data providers for **Jade**:

### Shop Blocks (Trade Stands & Market Crates)
When looking at any shop block in the world, the Jade overlay displays:
- **Status**: Clearly shows whether the shop is **Open**, **Closed**, or an **Admin Shop**.
- **Shop & Owner Name**: Displays the custom shop title and owner name.
- **Active Offer**: Displays live item icons and quantities for both the item being sold and the required payment(s) (including secondary currency if configured).
- **Stock Warnings**: Real-time server-synced alerts if the shop is **Out of Stock** or if the owner's **Output is Full**.

### Wandering Shop Buyers
If you encounter a wandering **Shop Buyer NPC**, looking at them with Jade displays their current remaining shopping **Budget**.

---

## 🛡️ Land Claiming & Grief Protection

In multiplayer servers, players typically place shops inside claimed bases or town plots. By default, land protection mods prevent non-members from interacting with blocks. MarketBlocks ensures visitors can freely trade with your shops without compromising base security:

### FTB Chunks
✅ **Supported out of the box!**  
MarketBlocks includes the `ftbchunks:interact_whitelist` data tag for all shop blocks (`marketblocks:trade_stand`, `marketblocks:trade_stand_top`, `marketblocks:marketcrate`). Visitors can right-click shops to buy goods inside claimed chunks, but cannot break the blocks, open linked storage, or modify shop settings.

### Open Parties and Claims (OpenPAC)
⚠️ **Requires server configuration:**  
OpenPAC requires server administrators to register exceptions in the server config:
1. Open your server's `config/openpartiesandclaims-server.toml` file.
2. Locate `forcedBlockProtectionExceptionList`.
3. Add the MarketBlocks interact definitions:
   ```toml
   forcedBlockProtectionExceptionList = [
       "interact$marketblocks:trade_stand",
       "interact$marketblocks:trade_stand_top",
       "interact$marketblocks:marketcrate"
   ]
   ```
4. Restart the server or run `/openpac reload`. Players can now safely buy from shops in claimed plots!

---

## ⚙️ Configuration Toggles

All mod integrations can be toggled on or off in configuration files:
- **`marketblocks-client.toml`**:
  - `enableJourneyMapCompat` (default: `true`)
  - `enableXaerosCompat` (default: `true`)
- **`marketblocks-server.toml`**:
  - `allowNonOpTeleport` (default: `false`): Allows players without OP to click **[TP]** in chat search results.
