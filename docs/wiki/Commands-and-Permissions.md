# Commands & Permissions

This overview details all commands, permissions, and role hierarchies available in MarketBlocks.

> 💡 **Tip:** Every command starting with `/marketblocks` can also be abbreviated with the alias **`/mb`** (e.g., `/mb search iron_ingot` or `/mb stats`).

---

## Player Commands

These commands can be executed by any player without special permissions:

| Command | Alias | Description |
|---|---|---|
| `/marketblocks marketplace open` | `/mb marketplace open` | Opens the central Marketplace GUI (equivalent to the default keybind **O**). |
| `/marketblocks search <item> [page]` | `/mb search <item> [page]` | Searches for SingleOfferShops and Marketplace offers selling or buying the specified item. |
| `/marketblocks stats` | `/mb stats` | Displays the Top 10 SingleOfferShops and Top 10 Marketplace offers by total sales. |
| `/marketblocks stats shops` | `/mb stats shops` | Displays the Top 10 player and admin shops by total sales volume. |
| `/marketblocks stats marketplace` | `/mb stats marketplace` | Displays the Top 10 Marketplace offers by total lifetime purchases. |

### Interactive Search Results
When using `/marketblocks search <item>`, the chat outputs a paginated list of all matching shops and marketplace hubs:
- **Shop / Hub Name & Owner**: Displays custom shop names or owners.
- **Coordinates & Dimension**: Where the physical shop block is located.
- **[Waypoint] Button**: Clicking this automatically registers a waypoint in **JourneyMap**, **Xaero's Minimap/Worldmap**, or outputs formatted coordinates in chat if no map mod is present.
- **[TP] Button**: Teleports directly in front of the shop. By default, this requires operator level 2, but server administrators can allow all players to teleport by setting `allowNonOpTeleport = true` in `marketblocks-server.toml`.

---

## Admin & Operator Commands

All administrative commands require **operator level 2** (`hasPermission(2)`).

```
/marketblocks admin <editmode|reload|resetlimits|marketplace|sale>
```

| Command | Purpose |
|---|---|
| `/marketblocks admin editmode [true\|false]` | Toggles or sets global edit mode. Enables the in-game editor for the Marketplace and unlocks the Admin Shop toggle in shop blocks. |
| `/marketblocks admin reload` | Reloads the Marketplace JSON configuration (`marketplace.json`) and Trader economy files from disk without restarting the server. |
| `/marketblocks admin resetlimits <player>` | Clears daily purchase limits for the specified player, allowing them to purchase daily-capped items again immediately. |
| `/marketblocks admin marketplace link [name] [tp_pos]` | Links the block you are currently looking at to the Marketplace. Supports an optional display name and custom teleport arrival coordinates (`tp_pos`). |
| `/marketblocks admin marketplace unlink [name]` | Unlinks the block you are currently looking at, or unlinks a specific registered block link by name (with auto-completion). |
| `/marketblocks admin sale marketplace set <offer> <percent> <duration_minutes>` | Starts a timed discount on a Marketplace offer. Auto-completion displays item name, page, and current price. |
| `/marketblocks admin sale marketplace remove <offer>` | Immediately removes an active sale discount from a Marketplace offer. |
| `/marketblocks admin sale shop set <shop> <percent> <duration_minutes>` | Starts a timed discount on an Admin Shop block. Auto-completion displays shop name, position, and price. |
| `/marketblocks admin sale shop remove <shop>` | Immediately removes an active sale discount from an Admin Shop block. |

---

## Global Admin Mode (`editmode`)

Global Admin Mode (`/marketblocks admin editmode true`) is a central administrative toggle:

1. **Marketplace In-Game Editor**: Unlocks visual editing buttons in the Marketplace GUI to create new pages, add/edit/delete offers, reorder items, and configure pricing/limits live.
2. **Admin Shop Mode**: Allows operators to convert any placed SingleOfferShop (Trade Stand or Market Crate) into an **Admin Shop** via the Access settings tab. Admin shops have infinite stock and require no input inventory.
3. **Owner Bypass**: Operators in edit mode can open and modify the settings and inventory of any player's shop block.
4. **Live Synchronization**: Toggling edit mode instantly refreshes the interface for all currently connected players who have a shop or marketplace menu open.

---

## SingleOfferShop Permissions & Roles

Permissions on individual shop blocks (Trade Stands & Market Crates) are governed by the shop's internal ownership system, rather than permission nodes:

| Role | Offers Tab | Inventory Tab | Settings Tab | Log Tab |
|---|:---:|:---:|:---:|:---:|
| **Primary Owner** (creator) | ✅ Full | ✅ Full | ✅ Full | ✅ View & Clear |
| **Co-Owner** (up to 10 added) | ✅ Full | ✅ Full | ✅ Full | ✅ View only |
| **Operator** (edit mode active) | ✅ Full | ✅ Full | ✅ Full | ✅ Full |
| **Customer / Visitor** | ✅ Buy only | ❌ Denied | ❌ Denied | ❌ Denied |

> ℹ️ **Note on Admin Shops:** When a shop is set to Admin Shop Mode, the **Inventory Tab** is completely hidden for all players (including owners), because items are created and consumed infinitely without physical storage.

### Access Control Settings
Shop owners can control who is allowed to purchase from their shop via the **Access** tab:
- **Everyone** (default): All players on the server can purchase.
- **Whitelist**: Only players explicitly added to the access list can purchase.
- **Blacklist**: All players can purchase except those added to the access list.
- **Closed Shop**: Owners can temporarily close their shop in the General tab. When closed, nobody can buy (except operators in edit mode).

---

## Internal System Commands

MarketBlocks registers an internal command tree used exclusively by interactive chat click-events:

- `/marketblocks internal waypoint <x> <y> <z> <dim> <name>`: Dispatched when clicking **[Waypoint]** in search results. Sends waypoints to JourneyMap or Xaero's Minimap.
- `/marketblocks internal tp <dim> <x> <y> <z> [yaw pitch]`: Dispatched when clicking **[TP]** in search results. Validates permissions and teleports the player directly in front of the target shop block facing the shop.

> 🔒 Players cannot abuse `/marketblocks internal tp` to teleport around the world without permission: the server validates that the player has OP level 2 or that `allowNonOpTeleport` is explicitly set to `true` in the server configuration.
