# SingleOfferShop: Settings Guide

The **Settings** tab in the SingleOfferShop GUI lets owners customize every detail of their shop — from automated hopper routing and animated clerks to floating showcases and customer permissions.

The settings menu is divided into **6 sub-tabs**:

---

## 1. ⚙️ General Tab

Controls basic shop behavior and sensory feedback:

- **Shop Name**: Enter a custom name (up to 32 characters). This title appears at the top of the GUI, in chat notifications, and in `/mb search` results.
- **Status (Pause / Resume)**: Next to the name field is the shop status button:
  - 🟢 **Active**: Open for business.
  - 🟠 **Paused**: Temporarily closes the shop to customers while you adjust prices or restock.
- **Emit Redstone**: When enabled, the shop block emits a redstone pulse upon every completed sale. You can also attach a **Redstone Comparator** to read the shop's stock fullness level.
- **Purchase XP Sound**: Plays a satisfying experience ding sound when a customer buys an item. Pitch scales dynamically with purchase size.

---

## 2. 🔄 I/O (Automation) Tab

Enables automation with hoppers, pipes, and adjacent chests:

- **Sided Directional Routing**: Configure each block face (**Top**, **Bottom**, **Sides**, **Back**) independently:
  - `INPUT`: Accepts stock to replenish the offer result item.
  - `OUTPUT`: Extracts collected payment earnings.
  - `DISABLED`: Closes that side to hoppers and pipes.
- **Chest Extension (Auto Pull & Push)**: If enabled on your server, the shop can automatically pull restock items from adjacent input chests and push collected currency into adjacent profit chests without needing hoppers!
- **Redstone Control Mode**:
  - `Ignored`: Automation is always active.
  - `Active on High`: Automation only runs when powered by a redstone signal.
  - `Active on Low`: Automation halts when powered by redstone.

---

## 3. 🧑‍🌾 Villager / Clerk Tab

Brings your shop to life with an animated clerk standing behind the counter:

- **Enable Clerk**: Toggles the clerk model on or off.
- **Villager Mode**: Select from **15 villager professions** (Farmer, Librarian, Armorer, Cleric, Weaponsmith, etc.) to give your shop the perfect themed look.
- **Player Skin Mode**:
  - Check **Use Player Skin** and enter any Minecraft username.
  - The clerk will render the player's official skin (supporting slim/classic models and jacket/hat layers).
- **Custom Name Tag**: Displays a floating name tag above the clerk's head.
- **Feedback Effects**:
  - **Purchase Particles**: Emits emerald-green happy villager particles on trade.
  - **Purchase Sounds**: Plays villager affirmation voice lines when goods are bought.

---

## 4. 🎨 Visuals Tab

Gives you full creative control over how your goods are displayed in the world:

### Floating Item Showcase (Trade Stand & Market Crate)
- **Visible**: Toggles the floating 3D item rendering.
- **Fullbright**: Makes the item glow at full brightness, ignoring shadows in dim markets or caves.
- **Scale (0.5x – 1.5x)**: Adjusts item size.
- **Rotation Speed (0.0x – 1.5x)**: Controls rotation speed (set to `0.0` for a static display).
- **Height Offset (-0.25 to +0.25)**: Moves the item up or down relative to the counter surface.
- **Bobbing Motion**: Toggles a gentle floating up-and-down oscillation.

### Market Crate Bulk Layouts (Market Crate only)
- **Stacked Mode**: Neatly arranged in uniform rows.
- **Loose Mode**: Artistically scattered like fresh produce at a rustic bazaar.
- **Dynamic Stock Depletion**: The visual items resting inside the crate visibly decrease as customers purchase stock, giving passersby an intuitive at-a-glance stock meter!

---

## 5. 🔔 Notifications Tab

Stay informed about your business even when exploring far from base:

- **Notify on Purchase**: Sends you a chat message whenever a customer buys an item.
- **Notify on Out of Stock**: Sends an alert when your input inventory runs out of goods.
- **Notify on Output Full**: Sends a warning when your earnings chest is full and cannot accept more payments.
- **Offline Notifications**: If sales happen or stock runs out while you are offline, MarketBlocks queues the alerts and delivers them with coordinates upon your next login!
- **Notify Co-Owners**: When checked, broadcasts all active alerts to all registered shop co-owners.

---

## 6. 🔒 Access Tab

Control who is allowed to buy from your shop and manage your store team:

- **Customer Access Modes**:
  - **Everyone** (Default): Any player on the server can buy.
  - **Whitelist**: Only players explicitly added to your access list can purchase.
  - **Blacklist**: All players can purchase *except* those on the blacklist.
- **Co-Owner Management**:
  - Add up to **10 trusted friends** by username as co-owners.
  - Co-owners can restock, collect profits, modify prices, and adjust settings.
- **Admin Shop Mode Toggle**:
  - *(Only visible to server operators with active edit mode `/mb admin editmode true`).*
  - Converts the block into a server-owned shop with infinite stock and void earnings. See [Admin Shop Mode](SingleOfferShop-Admin-Shop-Mode).

