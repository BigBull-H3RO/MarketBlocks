# SingleOfferShop: Setup & Roles

## Basic Setup

1. Craft a **Trade Stand** or **Market Crate** and place it in the world.
2. Open the shop — you are automatically set as the **primary owner**.
3. Configure the offer:
   - Set the **result item** (what the customer receives)
   - Set at least one **payment item** (what the customer pays; up to 2 different payment stacks)
4. Activate the offer and perform a test purchase.
5. Optionally customize your shop in the **Settings** tab:
   - Give your shop a [name](SingleOfferShop-Settings) (up to 32 characters)
   - Enable a [Visual NPC](SingleOfferShop-Visual-NPC) (Villager or Player skin)
   - Configure [offer item visuals](SingleOfferShop-Offer-Item-Visuals) (floating item display)
   - Set up [notifications](SingleOfferShop-Notifications) for stock alerts
   - Configure [access control](SingleOfferShop-Access-Control) (whitelist/blacklist)
   - Set up [I/O and redstone](SingleOfferShop-Settings) for automation

## Block Variants

| Block | Description |
| --- | --- |
| **Trade Stand** | Two blocks tall with an optional showcase top. Items float above the stand. |
| **Market Crate** | Single block. Supports multi-item rendering with layout modes (stacked or scattered). |

Both variants share the same block entity logic — all features work identically regardless of the block type.

## Purchasing

- **Single purchase**: Click the offer slot to buy one unit.
- **Bulk purchase (Shift-click)**: Hold Shift and click to buy as many units as possible in a single action. The maximum is automatically calculated based on:
  - Your available payment items
  - The shop's input stock (unless admin shop)
  - The shop's output capacity (unless admin shop)
  - Your inventory space

## Role Model

### Primary Owner

- Full shop management: all tabs accessible
- Set when the first player opens a freshly placed shop
- Can manage co-owners, clear the transaction log, and delete offers

### Co-Owners

- Extended management rights (similar to primary owner)
- Can access Offers, Inventory, Settings, and Log tabs
- Cannot clear the transaction log or manage other co-owners
- Maximum co-owners per shop: **10** (configurable via `maxCoOwnersPerShop`, range 0–100)

### Non-Owners

- Can buy from the shop based on access mode and shop state
- Cannot access Inventory, Settings, or Log tabs

## Tab Access (Summary)

| Tab | Primary Owner | Co-Owner | Operator (admin on) | Non-Owner |
| --- | --- | --- | --- | --- |
| **Offers** | ✅ Full | ✅ Full | ✅ Full | ✅ Buy only |
| **Inventory** | ✅ Full | ✅ Full | ✅ Full | ❌ |
| **Settings** | ✅ Full | ✅ Full | ✅ Full | ❌ |
| **Log** | ✅ Full (clear) | ✅ View | ✅ Full | ❌ |

> **Note:** The Inventory tab is **always hidden** when Admin Shop Mode is active, even for owners. See [Admin Shop Mode](SingleOfferShop-Admin-Shop-Mode).

## Transaction Log

Every purchase is recorded in the transaction log:

- **Buyer** name and UUID
- **Paid items** and **bought items** with exact counts
- **Timestamp** (epoch seconds)
- **Purchase type**: Single or Shift (bulk)
- **Smart stacking**: Identical purchases by the same buyer within 20 seconds are automatically merged into a single entry with an aggregation counter

The log stores up to **100 entries** per shop. The primary owner can clear the log.

## Notes

- All critical actions are validated server-side.
- For role issues, verify ownership state first.
- Shop data persists in the block entity NBT; transaction logs are stored separately as SavedData.
