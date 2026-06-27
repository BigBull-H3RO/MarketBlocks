# Commands & Permissions

This overview groups the most important commands and permissions by role.

## Admin / Operator

All administrative commands require **operator level 2** (`hasPermission(2)`).

| Command | Purpose |
| --- | --- |
| `/marketblocks admin editmode [true\|false]` | Enables or disables global admin/edit mode for shops and marketplace. |
| `/marketblocks admin reload` | Reloads the Marketplace JSON configuration from disk. |
| `/marketblocks admin resetlimits <player>` | Resets daily purchase limits for a specific player. |
| `/marketblocks admin marketplace link <name>` | Links any looked-at block in the world to a specific marketplace page/name. |
| `/marketblocks admin marketplace unlink` | Unlinks the looked-at linked block. |
| `/marketblocks admin sale shop set|remove` | Configures or removes a temporary sale/discount on a SingleOfferShop. |
| `/marketblocks admin sale marketplace set|remove` | Configures or removes a temporary sale/discount on a Marketplace offer. |
| `/marketblocks admin trader value set|remove` | Sets or removes custom currency values for trader entities. |
| `/marketblocks admin trader blacklist add|remove` | Adds or removes trader entities from the blacklist. |

## All Players

| Command | Purpose |
| --- | --- |
| `/marketblocks shop list [page]` | Lists all SingleOfferShops. Supports filtering by `owner`, `name`, or `category`. Operators get a **[TP]** button to teleport to each shop. All players get a clickable **[Waypoint]** button. |
| `/marketblocks shop search <item> [page]` | Searches for SingleOfferShops buying or selling a specific item. |
| `/marketblocks shop stats` | Shows the Top 10 SingleOfferShops by total sales. |
| `/marketblocks marketplace open` | Opens the Marketplace GUI (requires the sender to be a player). |
| `/marketblocks marketplace list [page]` | Lists all active Marketplace offers in chat. |
| `/marketblocks marketplace stats` | Shows the Top 10 Marketplace offers by total sales. |

## Global Admin Mode

The global admin mode is a central toggle (`/marketblocks admin editmode true`) that controls several features across the mod:

- **Marketplace**: Enables the in-game editor for creating, editing, and deleting pages and offers.
- **SingleOfferShop**: Allows operators to access the **Admin Shop** toggle in the Access settings tab.
- When toggled, all currently open SingleOfferShop menus are automatically refreshed to reflect the new permission state.

## Shop-Level Permissions (SingleOfferShop)

Permissions within a SingleOfferShop are based on ownership, not permission nodes:

| Role | Offers Tab | Inventory Tab | Settings Tab | Log Tab |
| --- | --- | --- | --- | --- |
| **Primary Owner** | ✅ Full | ✅ Full | ✅ Full | ✅ Full (can clear) |
| **Co-Owner** | ✅ Full | ✅ Full | ✅ Full | ✅ View only |
| **Operator** (admin mode on) | ✅ Full | ✅ Full | ✅ Full | ✅ Full |
| **Non-Owner** | ✅ Buy only | ❌ | ❌ | ❌ |

> **Note:** The Inventory tab is always hidden when Admin Shop Mode is active on a shop, even for owners.

## Access Control

Shop owners can restrict who is allowed to purchase from their shop using the **Access Mode** system:

- **Everyone** (default): All players can buy.
- **Whitelist**: Only players on the access list can buy.
- **Blacklist**: All players except those on the access list can buy.

Owners and co-owners always bypass access restrictions. If the shop is **closed**, no one can purchase (except operators in admin mode).

## Marketplace Permissions

- **Opening**: Any player can open the Marketplace via the keybind (**O** by default), the `/marketblocks marketplace open` command, or by clicking any linked block in the world.
- **Buying**: Any player can buy from available offers, subject to limits.
- **Editing**: Requires operator level 2 **and** global admin mode to be enabled.

## Permission Notes

- MarketBlocks does not register custom permission nodes — all permission checks use the vanilla operator level system.
- For fine-grained permission control, use a server permission mod that can manage operator levels or command-level permissions.
- Grant critical commands (`reload`, `resetlimits`, `admin editmode`) only to trusted roles.
