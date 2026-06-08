# SingleOfferShop: Access Control

The **Access** settings tab lets shop owners control who can buy from their shop and manage shop ownership. This includes the ability to close the shop entirely, restrict access to specific players, and enable the Admin Shop mode.

## Shop Status: Open / Closed

In the **General** settings tab, owners can set their shop to **Closed**:

- **Open** (default): The shop is available for purchases based on the access mode.
- **Closed**: No one can purchase from the shop, regardless of access mode. Owners and co-owners can still manage the shop.
- Operators with admin mode enabled bypass the closed status.

The shop's open/closed status is visible in the `/marketblocks list` command.

## Access Modes

The access mode determines which non-owner players are allowed to buy from the shop. This is configured in the **Access** settings tab.

| Mode | Description |
| --- | --- |
| **Everyone** | All players can purchase (default) |
| **Whitelist** | Only players on the access list can purchase |
| **Blacklist** | All players **except** those on the access list can purchase |

### How the Access List Works

- The access list is a set of player names/UUIDs managed through the Access settings tab.
- **Owners and co-owners always bypass access restrictions** — they can always buy from their own shop.
- The access mode can be cycled through the three options in the settings GUI.

### Access Check Priority

When a player tries to buy, the following checks are applied in order:

1. **Is the shop closed?** → Deny (unless operator with admin mode).
2. **Is the buyer an owner or co-owner?** → Allow.
3. **Access Mode = EVERYONE?** → Allow.
4. **Access Mode = WHITELIST?** → Allow only if the player is on the list.
5. **Access Mode = BLACKLIST?** → Allow only if the player is **not** on the list.

## Ownership System

### Primary Owner

- The first player to interact with a newly placed shop becomes the **primary owner**.
- The primary owner has full control over the shop, including the ability to:
  - Clear the transaction log
  - Manage co-owners
  - Delete the offer
  - Change all settings

### Co-Owners

- The primary owner can add additional co-owners to help manage the shop.
- Co-owners have the same permissions as the primary owner, **except** they cannot clear the log or manage other co-owners.
- The maximum number of co-owners is configurable (default: 10, range: 0–100).

| Config Key | Default | Description |
| --- | --- | --- |
| `maxCoOwnersPerShop` | `10` | Maximum number of co-owners per shop |

## Admin Shop Mode

The Admin Shop toggle is located in the **Access** settings tab but is only visible when:

1. Global admin mode is enabled (`marketblocksAdminModeEnabled = true`), **and**
2. The player has operator rights.

See [Admin Shop Mode](SingleOfferShop-Admin-Shop-Mode) for full details.

## Operator Behavior in Admin Mode

When global admin mode is enabled:

- **Operators are treated as owners of all shops** — they can access all tabs and modify settings.
- Operators bypass all access restrictions (closed status, whitelist/blacklist).
- This is designed for server administration and moderation.

## Configuration

| Config Key | Default | Description |
| --- | --- | --- |
| `shopTabAccessEnabled` | `true` | Whether the Access settings tab is visible to players |
| `marketblocksAdminModeEnabled` | `false` | Global admin mode (controlled by `/marketblocks adminmode`) |
| `maxCoOwnersPerShop` | `10` | Maximum number of co-owners per shop (0–100) |
