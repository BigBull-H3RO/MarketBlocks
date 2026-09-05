# SingleOfferShop: Admin Shop Mode

## Purpose

**Admin Shop Mode** is designed for server-controlled economy shops that supply unlimited items without requiring physical inventory management. These are ideal for server spawn malls, starter markets, quest turn-ins, or currency sinks where the server controls trade.

---

## Enabling Admin Shop Mode

To convert a player shop block (Trade Stand or Market Crate) into an Admin Shop, **two conditions** must be met:

1. **Global Edit Mode must be enabled**: Run `/marketblocks admin editmode true` (or `/mb admin editmode true`).
2. The player must have **Operator rights** (permission level 2).

When both conditions are met, an **Admin Shop** toggle switch becomes visible in the **Access** sub-tab of the shop's Settings menu.

---

## Behavior Comparison

| Feature | Normal Player Shop | Admin Shop |
|---|---|---|
| **Input Inventory (Stock)** | Requires physical items to sell | **Unlimited** — Items are generated on purchase |
| **Output Inventory (Earnings)** | Must have free space to accept payments | **Infinite / Void** — Payments are consumed into the void |
| **Inventory Tab** | Accessible to owners/co-owners | **Completely Hidden** for all players |
| **Out of Stock Warnings** | Triggers when input slots are empty | **Never** triggers (always stocked) |
| **Ownership** | Primary Owner + up to 10 Co-Owners | Operators act as owners |

---

## Operator Privileges in Edit Mode

When global edit mode is active (`/mb admin editmode true`):
- Operators can open and manage any player shop on the server.
- Operators bypass all access restrictions (whitelists, blacklists, and closed status).
- Operators can toggle Admin Shop status on or off at any time.

---

## Admin Shop Sales & Discounts

Just like the Marketplace, Admin Shops support timed sales! Operators can start promotional events or weekend discounts on specific admin shops using chat commands:

- **Start a Timed Sale**:
  ```
  /marketblocks admin sale shop set <shop> <percent> <duration_minutes>
  ```
  *Example:* `/mb admin sale shop set "Spawn Bakery" 25 120` (applies a 25% discount for 2 hours).  
  *Tab completion automatically suggests all available admin shops!*

- **Cancel a Sale**:
  ```
  /marketblocks admin sale shop remove <shop>
  ```

---

## Searching and Navigating to Admin Shops

- **Leaderboards**: View the top performing shops using `/marketblocks stats shops` (or `/mb stats shops`).
- **Item Search**: Players can search for items sold by admin shops using `/marketblocks search <item>` (or `/mb search <item>`). Search results in chat include interactive **[Waypoint]** and **[TP]** buttons.
