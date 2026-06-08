# SingleOfferShop: Examples / Common Setups

## Example 1: Player Shop (Survival)

A standard survival shop where a player sells items they have gathered or crafted.

- **Owner**: The player who placed the shop
- **Access Mode**: Everyone (default)
- **Admin Shop**: Off
- **NPC**: Enabled with a Farmer profession
- **Offer Item Visuals**: Visible, scale 0.75, bobbing enabled
- **Notifications**: Out of Stock enabled, so the owner gets alerted when restocking is needed
- **I/O**: Disabled (player manually restocks)

**Setup Steps:**
1. Place a Trade Stand or Market Crate.
2. Set the offer (e.g., 1 Diamond → 16 Bread).
3. Fill the input inventory with Bread.
4. Optionally name the shop (e.g., "Baker's Stand").
5. Enable the Visual NPC and set a profession.

---

## Example 2: Spawn Purchase Station (Admin Shop)

A server-managed shop at spawn that sells essential items for a fixed price.

- **Owner**: Server operator
- **Admin Shop**: Enabled (unlimited stock, no output management needed)
- **Access Mode**: Everyone
- **NPC**: Enabled with Cleric profession
- **Shop Name**: "Starter Supplies"
- **Notifications**: Disabled (admin shops don't run out of stock)
- **Offer Item Visuals**: Visible, fullbright on, larger scale for visibility

**Setup Steps:**
1. Enable global admin mode: `/marketblocks adminmode true`.
2. Place a Trade Stand.
3. Set the offer (e.g., 4 Iron Ingots → 1 Iron Pickaxe).
4. Go to Settings → Access and enable **Admin Shop Mode**.
5. Customize the NPC and visuals as desired.

---

## Example 3: Community Trading Point (Co-Owner)

A shop managed by multiple players, sharing restocking responsibilities.

- **Owner**: Primary owner + 2-3 co-owners
- **Access Mode**: Everyone
- **Admin Shop**: Off
- **Notifications**: Purchase, Out of Stock, and Output Full enabled, with **Notify Co-Owners** enabled
- **I/O**: Hopper input from below, auto I/O enabled for automated restocking

**Setup Steps:**
1. Primary owner places the shop and sets up the offer.
2. Go to Settings → Access and add co-owners by name.
3. Enable all notification types and turn on co-owner notifications.
4. Optionally configure I/O for automation.
5. Use the transaction log to monitor demand trends.

---

## Example 4: Exclusive VIP Shop (Whitelist)

A shop that only sells to specific players, such as a guild shop or premium store.

- **Owner**: Guild leader
- **Access Mode**: **Whitelist** — only listed players can purchase
- **Access List**: Guild members added by name
- **NPC**: Enabled with Weaponsmith profession and a custom name ("Guild Armory")
- **Offer Item Visuals**: Multi-item display (count: 4, stacked layout)

**Setup Steps:**
1. Place the shop and set up the offer.
2. Go to Settings → Access.
3. Set Access Mode to **Whitelist**.
4. Add guild members to the access list.
5. Only those players can now purchase from the shop.

---

## Example 5: Automated Farm Shop (I/O + Redstone)

A fully automated shop connected to a farm via hoppers.

- **Owner**: Farm owner
- **I/O Configuration**:
  - Back side: INPUT (hopper feeds items from farm)
  - Bottom side: OUTPUT (collected payments drop into a chest below)
  - Redstone Control: REQUIRE_SIGNAL (only process I/O when powered)
- **Auto I/O**: Enabled (automatically pulls from adjacent chests)
- **Redstone Emission**: Enabled (sends a pulse on each purchase for counters/displays)
- **Offer Item Visuals**: Dynamic Fill Level enabled (customers can see stock level)

**Setup Steps:**
1. Place the shop with a hopper behind it (feeding from the farm).
2. Place a chest below the shop for collected payments.
3. Go to Settings → I/O:
   - Set Back to INPUT
   - Set Bottom to OUTPUT
   - Enable Auto I/O
4. Go to Settings → General: Enable Redstone Emission.
5. Go to Settings → Visuals: Enable Dynamic Fill Level.

---

## Setup Tips

- Start with simple, stable offers first.
- With high demand, monitor the transaction log and adjust stock levels.
- Use notifications to stay informed about shop status without having to visit.
- For shops with multiple admins, use co-owners and enable co-owner notifications.
- Use the `/marketblocks list` command to get a quick overview of all shops and their status.
- Use Dynamic Fill Level in visuals to give customers a visual indicator of stock availability.
- When testing a new shop, use the Closed status to prevent purchases until the offer is finalized.
