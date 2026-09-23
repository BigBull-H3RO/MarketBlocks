# SingleOfferShop: Examples & Common Setups

Here are practical, step-by-step setup guides for the most common trading configurations in MarketBlocks.

---

## 🛒 Example 1: Standard Player Shop (Survival SMP)

A player-run trading stall selling farmed or mined goods for currency.

- **Shop Type**: Trade Stand or Market Crate
- **Access Mode**: Everyone
- **Visual Clerk**: Villager with Farmer profession
- **Showcase**: Floating wheat/bread, slow rotation, bobbing enabled
- **Notifications**: "Out of Stock" enabled so the owner gets alerted when supplies run low.
- **I/O**: Manual restocking by the owner.

### Setup Walkthrough:
1. Place down a **Trade Stand** or **Market Crate**.
2. Right-click to open. Set the desired offer (e.g., 1 Emerald ➔ 16 Bread).
3. Place stacks of bread into the shop's **Input Inventory**.
4. Open the **Settings** tab:
   - In **General**: Name the shop (e.g., `"Sunrise Bakery"`).
   - In **Villager**: Enable the visual NPC and select `Farmer`.
   - In **Notifications**: Enable `Notify on Out of Stock`.
5. Your shop is immediately open for customers!

---

## 🏛️ Example 2: Spawn Hub Station (Server Admin Shop)

A server-controlled admin shop offering unlimited tools or starter gear for newly joined players.

- **Shop Type**: Trade Stand
- **Admin Shop Mode**: Enabled (infinite supply, payments are voided)
- **Access Mode**: Everyone
- **Visual Clerk**: Player skin or Cleric villager
- **Shop Title**: `"Starter Tools"`

### Setup Walkthrough:
1. Enable global edit mode via chat: `/mb admin editmode true`.
2. Place down a **Trade Stand**.
3. Set the trade recipe (e.g., 3 Iron Ingots ➔ 1 Iron Pickaxe).
4. Go to **Settings -> Access**:
   - Toggle **Admin Shop Mode** to `ON`.
   - *(Notice that the Inventory tab disappears since stock is infinite!)*
5. Go to **Settings -> General**: Set the shop name to `"Starter Tools"`.
6. Go to **Settings -> Visuals**: Enable `Fullbright` to make the pickaxe shine in spawn lighting.

---

## ⚙️ Example 3: Automated Farm Vendor (Hopper & Redstone I/O)

An automated shop connected directly to an automatic mob or crop farm via hoppers and chests.

- **Automation**: Back face inputs crop items; bottom face extracts emerald profits.
- **Redstone Pulse**: Emits a signal on every completed sale to drive a counter lamp.

### Setup Walkthrough:
1. Connect a hopper pointing into the **Back** of the shop block (from your farm).
2. Place a hopper or chest below the **Bottom** of the shop to collect incoming payments.
3. Go to **Settings -> I/O**:
   - Configure Back as `INPUT` and Bottom as `OUTPUT`.
4. Go to **Settings -> General**:
   - Toggle `Emit Redstone` to `ON`. Connect redstone dust behind the block to power note blocks, lamps, or transaction counters!
5. *(Optional)* If `enableChestExtension = true` is set in `config/marketblocks/singleoffer/general.toml`, you can also place chests directly adjacent to the shop for automatic item pulling and profit pushing without hoppers!

---

## ⚡ Quick Configuration Setups

- **🤝 Shared Guild Store (Co-Owners)**: Want teammates to help manage your shop? Go to **Settings -> Access**, type in usernames in the **Co-Owners** list, and enable `Notify Co-Owners` under **Settings -> Notifications** so all partners receive stock and sale alerts.
- **🔒 VIP / Faction Outlet (Whitelist)**: To restrict sales exclusively to trusted clan members, switch the Access Mode dropdown in **Settings -> Access** to **Whitelist** and register permitted usernames. Non-whitelisted players will be unable to purchase.

---

## 💡 Practical Management Tips

- **Check Shop Leaderboards**: Run `/marketblocks stats shops` (or `/mb stats shops`) to see which shops across the server have the highest sales volume.
- **Find Specific Items**: Need to see where an item is being sold? Run `/marketblocks search <item_id>` to find all matching player shops and marketplace hubs with instant waypoint coordinates!
- **Launch Sales on Admin Shops**: Run `/mb admin sale shop set <shop> <percent> <minutes>` to give your spawn shops weekend promotional discounts!
