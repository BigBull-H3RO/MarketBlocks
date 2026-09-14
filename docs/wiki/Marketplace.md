# Marketplace: Server Economy Hub Guide

The **Marketplace** is the centralized, server-wide trading hub of MarketBlocks. Unlike individual player-owned shop blocks, the Marketplace serves as the server's primary currency and trading system.

---

## 🚪 Three Ways to Open the Marketplace

Players can access the Marketplace through three different methods depending on server style:

1. **Keybind (`O`)**: Press **O** (default, rebindable in Options -> Controls) to open the Marketplace instantly from anywhere.
2. **Chat Command**: Type **`/marketblocks marketplace open`** (or the convenient shorthand **`/mb marketplace open`**).
3. **Linked World Blocks**: Right-click decorative market stalls, NPC counters, or custom blocks in spawn hubs that have been linked by server administrators.

---

## 🛒 How Players Shop

The Marketplace is organized into clean, tabbed category pages (e.g. *Minerals*, *Farming*, *Tools*, *Exotics*):
- **Browsing**: Click the category tabs at the top to navigate between pages.
- **Buying 1 Unit**: Left-click any trade offer card to purchase one unit.
- **Bulk Buying (Shift-Click)**: Hold `Shift` and left-click an offer to purchase as many units as your inventory and wallet permit.
- **Stock & Daily Limits**: Some offers have finite stock that recharges over time, or daily purchase quotas per player.

---

## 🛠️ In-Game Management (For Server Administrators)

You can create pages, add items, and adjust prices directly inside the game without ever touching JSON files!

### Step 1: Enable Edit Mode (Important!)
Before any editing buttons appear, an operator (OP level 2) must activate global edit mode:
```
/mb admin editmode true
```
*(Run `/mb admin editmode false` when finished to return to normal customer view).*

### Step 2: Adding & Managing Pages
Once edit mode is active, page control buttons appear at the top:
- **Add Page**: Click the **`+`** button next to the category tabs to create a new page. Enter a category title and pick an icon item.
- **Reorder / Delete Page**: Click the page settings icon to rename, change icons, move tabs left/right, or delete a page.

### Step 3: Adding & Editing Offers
With edit mode active, an **"Add Offer"** button appears:
1. Click **Add Offer**.
2. **Set Offered Item**: Drag or click the item you want to sell into the result slot.
3. **Set Payment Items**: Place up to 2 payment items and quantities that customers must pay (e.g., 5 Emeralds).
4. **Configure Economics**:
   - **Daily Limit**: Maximum units an individual player can buy per real-world day (set to `0` for unlimited).
   - **Stock Limit & Restock Timer**: Maximum server pool stock and recharge interval in minutes.
   - **Dynamic Pricing**: Toggle supply/demand price scaling that dynamically raises prices when demand spikes and cools down over time.
5. Click **Save**. The offer is instantly live for all players!

---

## 🔗 Linking World Stalls (`/mb admin marketplace link`)

To create immersive RPG market hubs where players must physically visit stalls to shop:

1. Look directly at the block you want to turn into a marketplace access point (e.g. a barrel, counter, or decorative stall).
2. Run the link command:
   ```
   /mb admin marketplace link [Display Name] [tp_pos]
   ```
   *Example:* `/mb admin marketplace link "Spawn Grand Bazaar"`
3. Any player who right-clicks that block will now open the server Marketplace!
4. **Unlinking**: Look at the block and run `/mb admin marketplace unlink`, or specify the name: `/mb admin marketplace unlink "Spawn Grand Bazaar"`.

---

## 🏷️ Launching Promotional Sales

Admins can start timed sales with percentage discounts on any Marketplace offer:
```
/mb admin sale marketplace set <offer> <percent> <duration_minutes>
```
*Example:* `/mb admin sale marketplace set "Diamond Pickaxe" 20 60` (applies a 20% discount for 1 hour). Tab completion automatically lists all available offers!
- To cancel a sale early: `/mb admin sale marketplace remove <offer>`.

---

## ❓ Troubleshooting & Common Questions

- **"Why don't I see the buttons to add pages or offers?"**  
  You must activate Edit Mode first! Run `/mb admin editmode true` while having OP level 2.
- **"Why won't an offer let me buy?"**  
  Check if you have the exact payment items in your inventory, or if the offer has reached its daily limit or run out of stock.
- **"The O key doesn't open the menu."**  
  Check **Options -> Controls -> Key Binds** to verify that the `O` key is not conflicting with another mod. You can always use `/mb marketplace open`.
- **"Where are marketplace offers saved?"**  
  Offers are stored server-side at `<world>/marketblocks/marketplace.json`. MarketBlocks creates an automatic `.bak` backup file on every save!

