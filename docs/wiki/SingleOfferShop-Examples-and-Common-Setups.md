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

## 🤝 Example 3: Community Guild Store (Co-Owners)

A shop operated collaboratively by a team or faction sharing restocking responsibilities.

- **Ownership**: Primary Owner + 3 Guild Co-Owners
- **Access Mode**: Everyone
- **Notifications**: All alerts enabled (`Notify Co-Owners = true`)

### Setup Walkthrough:
1. Primary owner places the shop and sets up the item offer.
2. Go to **Settings -> Access**:
   - Add teammate usernames to the **Co-Owners** list.
3. Go to **Settings -> Notifications**:
   - Enable `Notify on Purchase`, `Notify on Out of Stock`, and `Notify on Output Full`.
   - Check `Notify Co-Owners` so all guild managers receive alerts.
4. Co-owners can now deposit fresh stock, collect earnings, or change prices at any time!

---

## 🔒 Example 4: VIP / Faction Outlet (Whitelist)

A members-only shop that sells discounted high-tier gear exclusively to registered players.

- **Access Mode**: Whitelist
- **Access List**: Registered member usernames
- **Visual Clerk**: Player Skin with custom name tag (`"VIP Armory"`)

### Setup Walkthrough:
1. Place the shop and deposit your merchandise.
2. Go to **Settings -> Access**:
   - Switch the Access Mode dropdown to **Whitelist**.
   - Type in the usernames of permitted buyers and click add.
3. Non-whitelisted visitors who try to purchase will receive an access denied notice in chat.

---

## ⚙️ Example 5: Automated Farm Vendor (Hopper I/O)

An automated shop connected directly to an automatic mob or crop farm via hoppers and chests.

- **Automation**: Back face inputs crop items; bottom face extracts emerald profits.
- **Redstone Pulse**: Emits a signal on every completed sale to drive a counter lamp.

### Setup Walkthrough:
1. Ensure `enableChestExtension = true` in `config/marketblocks/singleoffer/general.toml` (if using adjacent chest extraction).
2. Connect a hopper pointing into the **Back** of the shop block (from your farm).
3. Place a hopper or chest below the **Bottom** of the shop to collect incoming payments.
4. Go to **Settings -> I/O**:
   - Configure Back as `INPUT` and Bottom as `OUTPUT`.
5. Go to **Settings -> General**:
   - Toggle `Emit Redstone` to `ON`. Connect redstone dust behind the block to power note blocks, lamps, or transaction counters!

---

## 💡 Practical Management Tips

- **Check Shop Leaderboards**: Run `/marketblocks stats shops` (or `/mb stats shops`) to see which shops across the server have the highest sales volume.
- **Find Specific Items**: Need to see where an item is being sold? Run `/marketblocks search <item_id>` to find all matching player shops and marketplace hubs with instant waypoint coordinates!
- **Launch Sales on Admin Shops**: Run `/mb admin sale shop set <shop> <percent> <minutes>` to give your spawn shops weekend promotional discounts!
