# SingleOfferShop: Admin Shop Mode

## Purpose

Admin Shop Mode is designed for **server-controlled shops** that provide unlimited stock without classic inventory management. These are ideal for spawn shops, starter areas, or any shop where the server controls the supply.

## Requirements

To enable Admin Shop Mode on a shop, **both** conditions must be met:

1. **Global admin mode** must be enabled: either via `/marketblocks adminmode true` or by setting `marketblocksAdminModeEnabled = true` in the config.
2. The player must have **operator rights** (permission level 2).

The Admin Shop toggle is located in the **Access** category of the Settings tab, but it is only visible when both conditions are met.

## Behavior

With Admin Shop Mode enabled on a specific shop:

| Aspect | Normal Shop | Admin Shop |
| --- | --- | --- |
| **Input stock** | Required for sales | Not required — unlimited supply |
| **Output capacity** | Must have space | Not checked — payment items vanish |
| **Inventory tab** | Accessible to owners | Hidden (inaccessible) |
| **Owner requirement** | Must be set by a player | Operators act as owners |

### Operator Privileges

When global admin mode is active:

- **Operators are treated as owners** of all shops — they can access all tabs and modify any settings.
- Operators bypass **access restrictions** (whitelist, blacklist, closed status).
- This allows server admins to manage any shop on the server without needing to be added as a co-owner.

## Redstone and I/O

Admin Shop Mode does not affect redstone or I/O behavior:

- Redstone pulses still fire on purchase (if enabled).
- I/O settings still apply, though the input/output inventories are bypassed for purchases.

## Recommendations

- Use only for clearly defined server shops (spawn shops, quest rewards, etc.).
- Regularly review prices and payment items administratively.
- Consider using the **Closed** status (in General settings) to temporarily disable an admin shop without removing the admin flag.
- Use the `/marketblocks list` command to get an overview of all shops and their status.

## Configuration

| Config Key | Default | Description |
| --- | --- | --- |
| `marketblocksAdminModeEnabled` | `false` | Global admin mode — also controls Marketplace edit access |
