# SingleOfferShop: Access Control

The **Access** settings tab gives shop owners fine-grained control over customer permissions, ownership hierarchies, and administrative status.

---

## Shop Status: Open vs. Closed

In the **General** settings tab, owners can toggle their shop between Open and Closed:

- **Open** (default): Normal trading operations based on the active access mode.
- **Closed**: All customer purchases are blocked. Owners and co-owners can still manage stock and settings.
- **Admin Bypass**: Server operators with global edit mode enabled (`/mb admin editmode true`) can trade even if a shop is marked closed.

---

## Access Modes

The access mode determines which non-owner players are permitted to purchase from the shop:

| Mode | Behavior |
|---|---|
| **Everyone** (default) | Any player on the server can purchase goods from this shop. |
| **Whitelist** | Only players explicitly registered on the shop's access list can purchase. |
| **Blacklist** | All players can purchase **except** those registered on the blacklist. |

### Access Evaluation Order
When a customer interacts with the shop, the server validates access strictly in this order:
1. **Is the shop closed?** ➔ Block purchase (unless operator in edit mode).
2. **Is the player the primary owner or a co-owner?** ➔ Always permit purchase.
3. **Is the access mode set to `Everyone`?** ➔ Permit purchase.
4. **Is the access mode set to `Whitelist`?** ➔ Permit only if the player's name/UUID is on the list.
5. **Is the access mode set to `Blacklist`?** ➔ Permit only if the player's name/UUID is **not** on the list.

---

## Ownership Hierarchy

### Primary Owner
- The first player to open a freshly placed shop block becomes its **Primary Owner**.
- The primary owner has exclusive authority to:
  - Add or remove co-owners.
  - Clear the transaction log history.
  - Break the shop block to retrieve all stored contents.

### Co-Owners
- The primary owner can register up to **10 co-owners** (configurable in `config/marketblocks/singleoffer/general.toml` via `maxCoOwnersPerShop`).
- Co-owners have full operational access: stocking input items, collecting payment earnings, changing prices, modifying visuals, and viewing transaction logs.
- Co-owners cannot remove the primary owner or clear the log history.

---

## Admin Shop Toggle

Server administrators with OP level 2 and active edit mode (`/mb admin editmode true`) will see an **Admin Shop** toggle in the Access tab. Converting a shop to an Admin Shop grants it infinite stock and removes inventory management requirements. See [Admin Shop Mode](SingleOfferShop-Admin-Shop-Mode) for full details.
