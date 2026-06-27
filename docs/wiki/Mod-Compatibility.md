# Mod Compatibility & Integrations

MarketBlocks is engineered to integrate seamlessly into rich modded multiplayer environments. Whether you are running a large public server with land claiming or a heavily modded pack with advanced minimaps and HUDs, MarketBlocks plays nicely with the rest of your ecosystem.

## 🗺️ Waypoint Integration (Minimaps)

Navigating to player shops in sprawling multiplayer worlds has never been easier. MarketBlocks natively interfaces with the most popular minimap and world map mods to let players set waypoints directly from chat!

### How it Works:
Whenever a player runs `/marketblocks shop list` or `/marketblocks shop search <item>`, each shop entry in chat includes a clickable **[Waypoint]** button. Clicking this button instantly sends the precise coordinates and dimension to your installed minimap mod.

### Supported Minimap Mods:
- **JourneyMap**: Instantly creates a named waypoint in your JourneyMap manager.
- **Xaero's Minimap**: Automatically registers a custom waypoint directly on your minimap and world map HUD.

> **Note:** If you do not have either minimap mod installed, clicking the button will still print the exact X, Y, Z coordinates and dimension in chat for easy manual navigation.

---

## 🛡️ Land Claiming & Protection Mods

In multiplayer servers, players naturally place their shops inside their claimed bases or town plots to prevent griefing. By default, land protection mods block all block right-clicks (interactions) by non-members, which would stop external customers from browsing or buying from your shops. 

We have built-in solutions to ensure your shops remain perfectly interactable without compromising your base security:

### FTB Chunks
✅ **Fully Supported Out of the Box!**
MarketBlocks natively bundles the `ftbchunks:interact_whitelist` data tag for all shop block variants (`trade_stand`, `marketcrate`). Visitors can right-click your shops to buy items inside your claimed chunks, but they cannot break the blocks or access your internal storage.

### Open Parties and Claims (OpenPac)
⚠️ **Requires Server Config Adjustment.**
OpenPac does not utilize data tags for whitelisting block interactions. Instead, server administrators must explicitly register the shop blocks in the OpenPac server configuration file.

#### How to Configure OpenPac:
1. Open your server's `openpartiesandclaims-server.toml` configuration file.
2. Locate the `forcedBlockProtectionExceptionList` setting.
3. Add the MarketBlocks interact definitions as shown below:
```toml
forcedBlockProtectionExceptionList = [
    "interact$marketblocks:trade_stand",
    "interact$marketblocks:trade_stand_top",
    "interact$marketblocks:marketcrate"
]
```
4. Save the file and restart the server. Players can now safely buy from shops in OpenPac claims!

---

## 🖥️ HUD & Info Mods

### Jade
MarketBlocks features dedicated compatibility with **Jade** (and WTHIT/HWYLA derivatives). When looking at any SingleOfferShop or linked block in the world, the Jade overlay displays rich, real-time information:

- **Shop Status**: Clearly indicates whether the shop is currently **Open** or **Closed**.
- **Active Offer**: Shows the exact payment item(s) required and the resulting item being sold directly on your HUD.
- **Ownership**: Displays the name of the shop owner.
