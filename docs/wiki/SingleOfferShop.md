# SingleOfferShop: Player Shops Guide

The **SingleOfferShop** is a player-owned shop block designed for reliable, grief-proof trading in survival multiplayer. Each shop block showcases and sells **one specific offer** with automated stock tracking, customer access control, and transaction logging.

---

## 🔨 Crafting Recipes

Both shop blocks are craftable in survival mode at a standard Crafting Table:

| **Trade Stand** | **Market Crate** |
|:---:|:---:|
| ![Trade Stand Recipe](../images/recipes/trade_stand.png)<br>*(2 Blocks Tall, Glass Showcase)* | ![Market Crate Recipe](../images/recipes/market_crate.png)<br>*(Single Block, Dynamic Stock Display)* |

<details>
<summary>📋 <b>Click to expand recipe ingredients (Text Breakdown)</b></summary>

- **Trade Stand**: 4× Any Planks, 1× Smooth Stone Slab, 2× Iron Ingot, 1× Emerald, 1× Any Wooden Sign
- **Market Crate**: 4× Any Planks, 1× Emerald, 1× Chest, 2× Barrels, 1× Any Wooden Sign

</details>

---

## 🔒 Ownership & Shop Protection

MarketBlocks provides built-in, server-enforced protection for every placed shop:

### Claiming Ownership
- When you place a shop block and **right-click it for the first time**, you automatically become its **Primary Owner**.
- The primary owner has full authority over all tabs, settings, co-owners, and transaction logs.

### Anti-Grief Protection
- **Unbreakable by Strangers**: Non-owners cannot break or mine your shop block.
- **Explosion-Proof**: Shops have bedrock-tier explosion resistance by default, preventing TNT or Creeper griefing.
- **Dismantling**: Only the Primary Owner can break the shop to pick it up. All stored stock and collected profits drop safely at your feet.
- **Server Admin Creative-Bypass**: Server operators (OP level 2) playing in Creative mode can dismantle any shop by **holding Shift while breaking**. Without Shift, accidental breaking is prevented.

### Co-Owners
The primary owner can register up to **10 Co-Owners** via the **Access** settings tab:
- Co-owners can restock input items, collect profits, modify prices, and adjust visuals.
- Co-owners cannot remove the primary owner or clear the transaction log.

---

## 📦 Setting Up Your First Offer

1. **Open the Shop**: Right-click your placed Trade Stand or Market Crate.
2. **Set the Offer** (in the *Offers* tab):
   - **Payment Slots (Left)**: Place up to 2 items that the customer must pay (e.g., 2 Diamonds + 1 Gold Ingot).
   - **Result Slot (Right)**: Place the item the customer receives (e.g., 1 Netherite Upgrade Smithing Template).
3. **Fill Stock** (in the *Inventory* tab):
   - **Input Storage**: Put the items you are selling into the input slots.
   - **Output Storage**: This is where payments from customers are safely stored until you collect them.
4. **Pause / Open Toggle**:
   - Next to your shop name at the top of the GUI is a dedicated **Status Button**.
   - Click it to toggle between **Active (Green)** and **Paused (Orange)**. When paused, customers cannot buy from the shop while you restock or redecorate.

---

## 🛒 How Customers Buy

- **Single Purchase**: Click the large offer button or result slot to buy **1 unit**.
- **Bulk Purchase (Shift-Click)**: Hold `Shift` and click the offer to buy as many units as possible in a single transaction. The maximum amount is automatically calculated based on:
  - Customer's available payment items
  - Shop's remaining input stock
  - Shop's available output storage space
  - Customer's free inventory slots

---

## 📜 Transaction Log

Every trade is recorded in the shop's **Log** tab:
- Displays customer names, player heads (with skin layers), exact payment and purchased items, and relative timestamps (e.g., *"5m ago"*).
- **Smart Stacking**: Successive purchases made by the same customer within 20 seconds are automatically combined into a single entry with an aggregation counter (e.g., `x5`).
- The log stores up to **100 entries**. The Primary Owner can clear the history at any time using the trash button.

---

## ⚙️ Advanced Customization

Ready to customize your shop with animated clerks, floating items, or hopper automation?
➡️ Check out the [SingleOfferShop Settings Guide](SingleOfferShop-Settings)!

