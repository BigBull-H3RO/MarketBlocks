# Commands & Permissions

This overview groups the most important commands and permissions by role.

## Admin / Operator

All administrative commands require **operator level 2** (`hasPermission(2)`).

| Command | Purpose |
| --- | --- |
| `/marketblocks adminmode` | Toggles global admin/edit mode (on/off). |
| `/marketblocks adminmode [true\|false]` | Enables or disables global admin/edit mode explicitly. |
| `/marketblocks marketplace` | Opens the Marketplace GUI (requires the sender to be a player). |
| `/marketblocks marketplace reload` | Reloads the Marketplace JSON configuration from disk. |
| `/marketblocks marketplace resetlimits <player>` | Resets daily purchase limits for a specific player. |

## All Players

| Command | Purpose |
| --- | --- |
| `/marketblocks list` | Lists all registered shops on the server with name, owner, and open/closed status. Operators see a clickable **[TP]** button to teleport to each shop. Non-operators see coordinates instead. |

## Global Admin Mode

The global admin mode is a central toggle that controls several features across the mod:

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

- **Opening**: Any player can open the Marketplace via the keybind (**O** by default) or the `/marketblocks marketplace` command (requires OP).
- **Buying**: Any player can buy from available offers, subject to limits.
- **Editing**: Requires operator level 2 **and** global admin mode to be enabled.

## Permission Notes

- MarketBlocks does not register custom permission nodes — all permission checks use the vanilla operator level system.
- For fine-grained permission control, use a server permission mod that can manage operator levels or command-level permissions.
- Grant critical commands (`reload`, `resetlimits`, `adminmode`) only to trusted roles.
