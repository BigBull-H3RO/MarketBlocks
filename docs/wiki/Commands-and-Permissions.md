# 📜 Commands & Permissions

This guide provides a comprehensive reference for all commands, interactive search tools, operator controls, and role-based permissions in MarketBlocks.

> 💡 **Tip:** Every command starting with `/marketblocks` can also be abbreviated using the alias **`/mb`** (e.g., `/mb search iron_ingot` or `/mb stats`).

---

## 👤 Player Commands

These commands can be executed by any player without special permissions:

| Command | Alias | Description |
|---|---|---|
| `/marketblocks marketplace` | `/mb marketplace` | Opens the central Marketplace GUI (requires an active player entity; equivalent to keybind **O**). |
| `/marketblocks search <item> [page]` | `/mb search <item> [page]` | Searches for SingleOfferShops and Marketplace hubs buying or selling the specified item. Supports tab-completion for any item ID. |
| `/marketblocks stats` | `/mb stats` | Displays both the Top 10 SingleOfferShops and Top 10 Marketplace offers by total sales. |
| `/marketblocks stats shops` | `/mb stats shops` | Displays the Top 10 SingleOfferShops by total sales volume. |
| `/marketblocks stats marketplace` | `/mb stats marketplace` | Displays the Top 10 Marketplace offers by total lifetime purchases. |

### Interactive Search Results
When using `/marketblocks search <item>`, the chat outputs an interactive, paginated list of all matching shops and marketplace hubs:

- **Status & Shop Name**: Shows whether the shop is `[OPEN]` (green) or `[CLOSED]` (red), along with the custom shop name (or generated ID) and owner.
- **Hover Offer Preview**: Hovering your mouse over any shop entry in chat reveals the complete trade offer (e.g. `2x Diamond -> 1x Netherite Ingot`), shop status, and owner.
- **[Waypoint] Button**: Clicking this automatically registers a waypoint in **JourneyMap** or **Xaero's Minimap/Worldmap**, or outputs formatted coordinates in chat if no minimap mod is installed.
- **[TP] Button**: Teleports the player directly in front of the shop, automatically oriented to face the shop block. By default, this requires operator level 2, but server administrators can allow all players to teleport by setting `allowNonOpTeleport = true` in `config/marketblocks/main.toml`.

---

## 🛡️ Operator & Admin Commands

All administrative commands require **Operator Permission Level 2** (`hasPermission(2)`).

```
/marketblocks admin <editmode|reload|resetlimits|marketplace|sale>
```

| Command | Purpose |
|---|---|
| `/marketblocks admin editmode` | Toggles edit mode on or off for the executing operator. |
| `/marketblocks admin editmode <true\|false> [targets]` | Explicitly sets edit mode for yourself or for specified target players (e.g. `@a` or player names). |
| `/marketblocks admin reload` | Hot-reloads all TOML configurations (`main.toml`, `trader.toml`) and syncs them to connected clients, plus `marketplace.json` and Trader economy files (`trader_item_values.json`, etc.). |
| `/marketblocks admin resetlimits <player>` | Clears the rolling 24-hour purchase limits for the specified player, allowing them to purchase daily-capped items again immediately. |
| `/marketblocks admin marketplace link [name] [tp_pos]` | Links the targeted block (within 5 blocks raycast) to the Marketplace as an in-world hub. Supports an optional display name and custom teleport landing coordinates (`tp_pos`). |
| `/marketblocks admin marketplace unlink [name]` | Unlinks the block you are currently looking at, or unlinks a registered hub by name (with tab-completion of registered names and coordinates). |
| `/marketblocks admin sale marketplace set <offer> <percent> <duration_minutes>` | Starts a timed discount (or surcharge if positive) on a Marketplace offer. Auto-completion displays offer name, page, and current price. |
| `/marketblocks admin sale marketplace remove <offer>` | Immediately removes an active sale discount from a Marketplace offer. |
| `/marketblocks admin sale shop set <shop> <percent> <duration_minutes>` | Starts a timed discount on an Admin Shop block. Auto-completion displays shop name and trade offer. |
| `/marketblocks admin sale shop remove <shop>` | Immediately removes an active sale discount from an Admin Shop block. |

---

## 🔧 Global Admin Mode (`editmode`)

Global Admin Mode (`/marketblocks admin editmode true`) provides in-game management tools:

1. **Marketplace In-Game Editor**: Unlocks visual editing buttons in the Marketplace GUI to create new pages, add/edit/delete offers, reorder items, and configure pricing/limits live.
2. **Admin Shop Mode**: Unlocks the **Admin Shop** toggle switch in the Access tab of SingleOfferShops. Admin shops feature infinite stock and require no storage chests.
3. **Owner Bypass**: Operators in edit mode can open, inspect, restock, or edit the settings of any player-owned shop block.
4. **Live Synchronization**: Toggling edit mode automatically broadcasts changes to all connected players who currently have a shop or marketplace menu open.

---

## 🔒 Shop Breaking & Protection Rules

Shop blocks (Trade Stands and Market Crates) feature built-in server-side grief protection:

| Player Status | Survival / Adventure Mode | Creative Mode (Without Shift) | Creative Mode (With Shift) |
|---|:---:|:---:|:---:|
| **Primary Owner** | ✅ Normal Break | ❌ Blocked *(Accidental break hint)* | ✅ Dismantled safely |
| **Non-Owner / Visitor** | ❌ Blocked *(Owner only)* | ❌ Blocked *(Owner only)* | ❌ Blocked *(Owner only)* |
| **Operator (Non-Owner)** | ❌ Blocked *(Owner only)* | ❌ Blocked *(Admin break hint)* | 🛡️ **Admin Bypass Authorized** |
| **Admin Shop** | ❌ Blocked *(Server protected)* | ❌ Blocked *(Admin shop hint)* | 🛡️ **Admin Bypass Authorized** |

### Key Protection Features:
- **Explosion Immunity**: All shop blocks have bedrock-tier blast resistance (`3,600,000.0`), preventing damage from Creepers, TNT cannons, and Withers.
- **Accidental Break Protection**: In Creative mode, blocks break in a single click. To prevent players and admins from accidentally one-shotting shops, **holding Shift while breaking is strictly required**. Left-clicking without Shift cancels the break and sends a reminder message.
- **Safe Inventory Drops**: When a shop is dismantled by its owner (or an authorized admin), all stored stock and collected profits drop safely at the player's feet.

---

## 👥 Shop Block Roles & Permissions

Permissions on individual shop blocks are managed via the shop's ownership and access control settings:

| Role | Offers Tab | Inventory Tab | Settings Tab | Log Tab |
|---|:---:|:---:|:---:|:---:|
| **Primary Owner** (Creator) | ✅ Full | ✅ Full | ✅ Full | ✅ View & Clear |
| **Co-Owner** (up to 10 registered) | ✅ Full | ✅ Full | ✅ Full | ✅ View only |
| **Operator** (Edit mode active) | ✅ Full | ✅ Full | ✅ Full | ✅ Full |
| **Customer / Visitor** | ✅ Buy only | ❌ Denied | ❌ Denied | ❌ Denied |

> ℹ️ For details on setting up co-owners, whitelists, blacklists, and customer rules, see the [SingleOfferShop Guide](SingleOfferShop) and [SingleOfferShop Settings](SingleOfferShop-Settings).

---

## ⚡ Internal Click Commands

MarketBlocks registers an internal command tree used exclusively by interactive chat click-actions:

- **`/marketblocks internal waypoint <x> <y> <z> <dim> <name>`**: Dispatched when clicking **[Waypoint]** in search results. Sends waypoint packets to JourneyMap or Xaero's Minimap.
- **`/marketblocks internal tp <dim> <x> <y> <z> [yaw pitch]`**: Dispatched when clicking **[TP]** in search results. Validates that the player has OP level 2 or that `allowNonOpTeleport = true` in `config/marketblocks/main.toml`. Automatically faces the player toward the front of the shop.
